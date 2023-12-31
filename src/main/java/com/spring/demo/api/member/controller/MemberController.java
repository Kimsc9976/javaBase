package com.spring.demo.api.member.controller;

import com.spring.demo.api.common.dto.BaseResponseDTO;
import com.spring.demo.api.member.service.EmailService;
import com.spring.demo.api.member.service.MemberService;
import com.spring.demo.domain.member.dto.RequestDTO.MemberChangePasswordDTO;
import com.spring.demo.domain.member.dto.RequestDTO.MemberChangeProfileDTO;
import com.spring.demo.domain.member.dto.ResponseDTO.MemberMyInfoDTO;
import com.spring.demo.domain.member.dto.RequestDTO.MemberJoinDTO;
import com.spring.demo.domain.member.dto.RequestDTO.MemberLoginDTO;
import com.spring.demo.domain.member.dto.ResponseDTO.MemberMyInfoProfileDTO;
import com.spring.demo.domain.member.entity.Member;
import com.spring.demo.domain.member.exception.ApiException;
import com.spring.demo.global.jwt.dto.TokenDTO;
import com.spring.demo.global.jwt.service.TokenProvider;
import com.spring.demo.global.redis.dto.AuthDTO;
import com.spring.demo.utils.urlutils.SecurePathUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.spring.demo.domain.member.exception.ErrorCode.*;

@Controller
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Tag(name = "Member 컨트롤러", description = "Member Controller API Document")
public class MemberController {

    private final MemberService memberService;
    private final EmailService emailService;
    private final TokenProvider tokenProvider;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "회원가입을 진행합니다")
    public ResponseEntity<? extends BaseResponseDTO<String>> signUp(
            @RequestBody MemberJoinDTO memberInfo
    ) {
        BaseResponseDTO<String> response = memberService.signUp(memberInfo);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


    @GetMapping("/sendmail")
    @Operation(summary = "이메일 중복확인 + 이메일[이메일 전송]", description = " 이메일 확인 후, 메일 전송을 진행합니다.. type : 1. certification [회원 가입] \n 2. find [임시 비밀번호 발급]")
    public ResponseEntity<? extends BaseResponseDTO<String>> sendMail(
            @RequestParam String email,
            @RequestParam String type
    )
            throws MessagingException {
        if (!"find".equals(type) && !"certification".equals(type)) { throw new ApiException(NOT_VALID_TYPE4SEND_MAIL);}
        BaseResponseDTO<String> _response = memberService.verifyMember(email, type);
        BaseResponseDTO<String> response = emailService.sendMail(email, type);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/verify-authnum")
    @Operation(summary = "이메일 인증[이메일 확인]", description = "인증코드 조회를 진행합니다.")
    public ResponseEntity<? extends BaseResponseDTO<String>> verifyAuthNum(
            @RequestBody AuthDTO verificationCode
    ){
        BaseResponseDTO<String> response = memberService.verifyAuthNum(verificationCode);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/signin")
    @Operation(summary = "로그인", description = "Id, PW로 로그인 진행")
    public ResponseEntity<? extends BaseResponseDTO<TokenDTO>> signIn(
            @RequestBody MemberLoginDTO memberInfo,
            HttpServletResponse httpResponse
    ){
        System.out.println(SecurePathUtil.encryptAndEncode(String.valueOf(126)));
        BaseResponseDTO<TokenDTO> response = memberService.signIn(memberInfo, httpResponse);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/inform")
    @Operation(summary = "사용자 정보", description = "[프로필용]Access Token 을 통해 사용자의 간략한 정보를 받아옴")
    public  ResponseEntity<? extends BaseResponseDTO<MemberMyInfoDTO>> getInfo(){

        String email = getEmail();

        BaseResponseDTO<MemberMyInfoDTO> response = memberService.getInfo(email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/mypage")
    @Operation(summary = "사용자 정보", description = "[마이페이지용]Access Token 을 통해 사용자의 상세한 정보를 받아옴")
    public  ResponseEntity<? extends BaseResponseDTO<MemberMyInfoProfileDTO>> getMyData(){

        String email = getEmail();

        BaseResponseDTO<MemberMyInfoProfileDTO> response = memberService.getMyData(email);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/changepw")
    @Operation(summary = "비밀번호 변경", description = "비밀번호 변경을 진행합니다, 동일하게 대소문자 8자리 특수기호를 필요로 합니다.")
    public  ResponseEntity<? extends BaseResponseDTO<String>> changePassword(
            @RequestBody MemberChangePasswordDTO memberInfo
    ){

        String email = getEmail();
        String newPW = memberInfo.getPassword();

        BaseResponseDTO<String> response = memberService.changePassword(email, newPW);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping(value = "/changeprofile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "회원정보 변경", description = "회원의 이메일, 닉네임, 프로필 사진을 변경합니다")
    public  ResponseEntity<? extends BaseResponseDTO<String>> changeprofile(
            @RequestPart("memberInfo") MemberChangeProfileDTO memberInfo,
            @RequestPart(value = "file", required = false) MultipartFile multipartFile
    ) throws IOException {

        String email = getEmail();

        BaseResponseDTO<String> response = memberService.changeProfile(email, memberInfo, multipartFile);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/signout")
    @Operation(summary = "로그아웃", description = "로그아웃을 진행합니다..")
    public  ResponseEntity<? extends BaseResponseDTO<String>> signOut(
            HttpServletRequest request
    ){

        String email = getEmail();
        String bearer_access_token = tokenProvider.getHeaderToken(request, "Access");
        String access_token = extractTokenFromHeader(bearer_access_token);

        BaseResponseDTO<String> response = memberService.signOut(email, access_token);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/withdraw")
    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 진행합니다.")
    public  ResponseEntity<? extends BaseResponseDTO<String>> withdrawalUser(
            HttpServletRequest request
    ){

        String email = getEmail();
        String bearer_access_token = tokenProvider.getHeaderToken(request, "Access");
        String access_token = extractTokenFromHeader(bearer_access_token);

        BaseResponseDTO<String> response = memberService.withdrawalUser(email, access_token);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


    private String getEmail(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        throw new ApiException(NO_MEMBER_ERROR);
    }
    private static final String BEARER_PREFIX = "Bearer ";

    private String extractTokenFromHeader(String headerValue) {
        if (headerValue == null) {
            throw new ApiException(NO_TOKEN_HEADER);
        }

        if (headerValue.trim().toLowerCase().startsWith(BEARER_PREFIX.toLowerCase())) {
            return headerValue.trim().substring(BEARER_PREFIX.length()).trim();
        }

        return headerValue.trim();
    }

}
