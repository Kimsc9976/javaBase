package com.spring.demo.api.member.service;

import com.spring.demo.api.common.dto.BaseResponseDTO;
import com.spring.demo.domain.member.constant.MemberType;
import com.spring.demo.domain.member.dto.RequestDTO.MemberChangeProfileDTO;
import com.spring.demo.domain.member.dto.RequestDTO.MemberJoinDTO;

import com.spring.demo.domain.member.dto.RequestDTO.MemberLoginDTO;
import com.spring.demo.domain.member.dto.ResponseDTO.MemberMyInfoDTO;
import com.spring.demo.domain.member.dto.ResponseDTO.MemberMyInfoProfileDTO;
import com.spring.demo.domain.member.entity.Member;
import com.spring.demo.domain.member.exception.ApiException;
import com.spring.demo.domain.member.repository.MemberRepository;
import com.spring.demo.global.jwt.dto.TokenDTO;
import com.spring.demo.global.jwt.service.TokenProvider;
import com.spring.demo.global.redis.dto.AuthDTO;
import com.spring.demo.global.redis.service.AuthService;
import com.spring.demo.global.redis.service.JwtProvideService;
import com.spring.demo.global.s3.S3UploadService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.spring.demo.domain.member.exception.ErrorCode.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final JwtProvideService jwtProvideService;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    private final EmailService emailService;
    private final AuthService authService;
    private final S3UploadService s3UploadService;

    @Override
    public BaseResponseDTO<String> signUp(MemberJoinDTO memberInfo)
            throws RuntimeException {
        /*
        example
         memberInfo = {
            email : example@example.com
            name : myName
            nickname : myNickname
            password : password
         }
        */
        if(memberRepository.findByEmail(memberInfo.getEmail()).isPresent()){
            throw new ApiException(REGISTER_DUPLICATED_EMAIL);
        }

        String encryptedPassword = passwordEncoder.encode(memberInfo.getPassword());

        Member member = memberInfo.toEntity(encryptedPassword, MemberType.SOCIAL);

        memberRepository.save(member);

        return new BaseResponseDTO<>("회원가입이 완료되었습니다.", 200);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseDTO<String> verifyMember(String email, String type) {
        log.info("===== 사용자 인증 진행 =============");
        if (!isValidEmail(email)) {
            throw new ApiException(INVALID_EMAIL_FORMAT);
        }

        if(Objects.equals(type, "certification") && memberRepository.findByEmail(email).isPresent()){
            throw new ApiException(REGISTER_DUPLICATED_EMAIL);
        }
        log.info("===== 사용자 인증 완료 =============");
        return new BaseResponseDTO<>("사용 가능한 Email 입니다.", 200);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseDTO<String> verifyAuthNum(AuthDTO authInfo) {
        /*
        example
         authNumber = {
            email : example@email.com;
            authNum : authNum;
            type : "certification";
         }
        */
        log.info("===== 로그인 인증번호 조회 시작 =============");
        String email = authInfo.getEmail();
        // 인증 코드 조회
        String authCode = authService.getAuthCode(email);
        if (!Objects.equals(authCode, authInfo.getAuthNum())){
            throw new ApiException(EMAIL_INPUT_ERROR);
        }

        log.info("===== 이메일 인증 완료. =============");
        return new BaseResponseDTO<>("이메일 인증이 완료되었습니다.", 200);
    }

    @Override
    public BaseResponseDTO<TokenDTO> signIn(MemberLoginDTO memberInfo, HttpServletResponse httpResponse) throws RuntimeException { // 이후 내껄로 수정해야함
        /*
        example
         memberInfo = {
            email : example@example.com
            password : password
         }
        */

        // 1. Login ID/PW 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = memberInfo.toAuthentication();
        // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        try {
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

            // 3. 인증 정보를 기반으로 JWT 토큰 생성
            TokenDTO tokenDTO = tokenProvider.generateTokenDTO(authentication);

            String accessToken = tokenDTO.getAccessToken();
            String refreshToken = tokenDTO.getRefreshToken();

            // 4. User 객체에서 username 을 가져옵니다.
            String email = authentication.getName();

            // 5. email 을 사용하여 DB 에서 Member 객체를 검색합니다.
            Optional<Member> optionalMember = memberRepository.findByEmail(email);
            if (optionalMember.isPresent()) {
                Member member = optionalMember.get();

                // refreshToken 을 업데이트하고 데이터베이스에 저장합니다.
                member.updateMemberToken(refreshToken);
                memberRepository.save(member);
            } else {
                // DB에 해당 이메일을 가진 Member 가 없는 경우의 처리
                throw new ApiException(NO_MEMBER_ERROR);

            }

            // 6. 토큰 정보를 Header 로 등록
            tokenProvider.setHeaderAccessToken(httpResponse, accessToken);
            tokenProvider.setHeaderRefreshToken(httpResponse, refreshToken);

            return new BaseResponseDTO<TokenDTO>("로그인이 완료되었습니다.", 200, tokenDTO);
        }catch (Exception e){
            throw new ApiException(LOGIN_INFO_ERROR);
        }
    }

    @Override
    public BaseResponseDTO<String> signOut(String email, String access_token) {
        // 토큰 탐색
        Optional<Member> optionalMember = memberRepository.findByEmail(email);

        if (optionalMember.isPresent()){
            Member member = optionalMember.get();
            member.updateMemberToken(null);
            memberRepository.save(member);
            return new BaseResponseDTO<>("로그아웃이 완료되었습니다.", 200);
        }

        throw new ApiException(NO_MEMBER_ERROR);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseDTO<MemberMyInfoDTO> getInfo(String email) {

        Member _member = getMember(email, memberRepository);

//        Member _member = getMemberBySecurity();

        MemberMyInfoDTO member = MemberMyInfoDTO.of(_member);


        return new BaseResponseDTO<MemberMyInfoDTO>(member.getNickname() + "의 간략 정보입니다.", 200, member);
//        return new BaseResponseDTO("$s 님의 정보를 제공해 드립니다", 200, member);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseDTO<MemberMyInfoProfileDTO> getMyData(String email) {

//        Member _member = getMemberBySecurity();
        Member _member = getMember(email, memberRepository);
        MemberMyInfoProfileDTO member = MemberMyInfoProfileDTO.of(_member);
        log.info("Member info: " + member);

        return new BaseResponseDTO<MemberMyInfoProfileDTO>(member.getNickname() + "의 상세 정보입니다.", 200, member);
    }


    @Override
    @Transactional
    public BaseResponseDTO<String> changePassword(String email, String newPW) {
//        Member member = getMemberBySecurity();
        Member member = getMember(email, memberRepository);
        log.info("===== " + member.getEmail() + " 비밀번호 변경 진행 =============");
        if (!isValidPassword(newPW)) {
            throw new ApiException(INVALID_PASSWORD_FORMAT);
        }

        String encryptedPassword = passwordEncoder.encode(newPW);

        member.updatePassword(encryptedPassword);
        memberRepository.save(member);
        log.info("===== " +member.getEmail()+ " 비밀번호 변경 완료 =============");
        return new BaseResponseDTO<>("비밀번호 변경이 완료되었습니다.", 200);
    }

    @Override
    @Transactional
    public BaseResponseDTO<String> changeProfile(String email, MemberChangeProfileDTO memberChangeProfileDTO, MultipartFile multipartFile) throws IOException {
        Member member = getMember(email, memberRepository);
//        Member member = getMemberBySecurity();

        String newEmail = memberChangeProfileDTO.getNewEmail();
        String nickname = memberChangeProfileDTO.getNickname();
        if (!isValidEmail(newEmail)) {
            throw new ApiException(INVALID_EMAIL_FORMAT);
        }

        // S3 파일 저장
        String postCategory = "profile";
        String imageUrl;
        // 프로필 사진 변경->이전 사진 삭제(이전 사진이 있을 경우만)
        String originProfilePath = member.getProfilePath();
        if (originProfilePath.length() > 0){
            int s3DomainLastIndex = originProfilePath.indexOf(".com/") + 5;
            if (s3DomainLastIndex > 0) {
                String pathWithFilename = originProfilePath.substring(s3DomainLastIndex);
                s3UploadService.deleteS3File(pathWithFilename);
            }
        }
        try {
            imageUrl = s3UploadService.S3Upload(multipartFile, postCategory);
        } catch (IOException e) {
            throw new ApiException(FILE_TYPE_ERROR);
        }
        member.updateProfile(newEmail, nickname, imageUrl);
        memberRepository.save(member);
        return new BaseResponseDTO<>("회원정보 변경이 완료되었습니다.", 200);

    }

    @Override
    @Transactional
    public BaseResponseDTO<String> withdrawalUser(String email, String access_token) {
        // 토큰 탐색
        Member member = getMember(email, memberRepository);
//        Member member = getMemberBySecurity();
        log.info("===== " +member.getEmail()+ " 회원 탈퇴 진행 =============");
//        Member member = getMember(email, memberRepository);
        member.withdrawMember();

        Date expiresIn = tokenProvider.parseClaims(access_token).getExpiration();
        jwtProvideService.addToBlackList(access_token, expiresIn);

        memberRepository.save(member);
        log.info("===== "+ member.getEmail() + " 회원 탈퇴 완료 =============");
        return new BaseResponseDTO<>("회원 탈퇴가 완료되었습니다.", 200);
    }


    public static boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        // 8자리 이상, 대문자 포함, 특수문자 하나 이상 포함
        String regex = "^(?=.*[A-Z])(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(password).matches();
    }

}
