package com.spring.demo.external.oauth.kakao.service;

import com.spring.demo.domain.member.constant.MemberType;
import com.spring.demo.domain.member.constant.Role;
import com.spring.demo.domain.member.entity.Member;
import com.spring.demo.domain.member.repository.MemberRepository;
import com.spring.demo.external.oauth.kakao.client.KakaoUserInformClient;
import com.spring.demo.external.oauth.kakao.dto.KakaoTokenDTO;
import com.spring.demo.external.oauth.kakao.dto.KakaoUserProfileDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class KakaoLoginServiceImpl {

    private final MemberRepository memberRepository;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String userInfoUri;

    private final PasswordEncoder passwordEncoder;

    public Member getKakaoMember(KakaoTokenDTO kakaoTokenDTO) {

//        KakaoTokenDTO kakaoTokenResponse = kakaoUserInformClient.kakaoCallback(code);
        System.out.println("카카오 멤버 찾기" + kakaoTokenDTO);
        String kakaoAccessToken = kakaoTokenDTO.getAccessToken(); // 액세스 토큰 추출 로직 구현 필요

        KakaoUserProfileDTO kakaoUserInfo = getUserInfo(kakaoAccessToken);

        System.out.println(kakaoUserInfo);
        String encryptedPassword = passwordEncoder.encode(MemberType.KAKAO + kakaoUserInfo.getId());

        return memberRepository.findByEmail(MemberType.KAKAO + kakaoUserInfo.getId())
                .orElseGet(() -> {
                    Member newUser = Member.builder()
                            .email(MemberType.KAKAO + kakaoUserInfo.getId())
                            .password(encryptedPassword)
                            .nickname(kakaoUserInfo.getProperties().getNickname())
                            .profile_path(kakaoUserInfo.getProperties().getProfileImageUrl())
                            .role(Role.USER)
                            .memberType(MemberType.KAKAO)
                            .build();

                    // 필요한 정보 세팅...
                    return memberRepository.save(newUser);
                });
    }

    private KakaoUserProfileDTO getUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<KakaoUserProfileDTO> response = restTemplate.exchange(
                userInfoUri, HttpMethod.GET, entity, KakaoUserProfileDTO.class);

        return response.getBody();
    }
}
