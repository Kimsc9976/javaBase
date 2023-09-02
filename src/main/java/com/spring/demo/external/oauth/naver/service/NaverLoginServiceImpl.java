package com.spring.demo.external.oauth.naver.service;

import com.spring.demo.domain.member.constant.MemberType;
import com.spring.demo.domain.member.constant.Role;
import com.spring.demo.domain.member.entity.Member;
import com.spring.demo.domain.member.repository.MemberRepository;
import com.spring.demo.external.oauth.kakao.dto.KakaoTokenDTO;
import com.spring.demo.external.oauth.kakao.dto.KakaoUserProfileDTO;
import com.spring.demo.external.oauth.naver.dto.NaverTokenDTO;
import com.spring.demo.external.oauth.naver.dto.NaverUserProfileDTO;
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
public class NaverLoginServiceImpl {

    private final MemberRepository memberRepository;

    @Value("${spring.security.oauth2.client.provider.naver.user-info-uri}")
    private String userInfoUri;

    private final PasswordEncoder passwordEncoder;

    public Member getNaverMember(NaverTokenDTO naverTokenDTO) {

//        KakaoTokenDTO kakaoTokenResponse = kakaoUserInformClient.kakaoCallback(code);
        System.out.println("네이버 멤버 찾기" + naverTokenDTO);
        String naverAccessToken = naverTokenDTO.getAccessToken(); // 액세스 토큰 추출 로직 구현 필요

        NaverUserProfileDTO naverUserInfo = getUserInfo(naverAccessToken);


        String encryptedPassword = passwordEncoder.encode(MemberType.NAVER + naverUserInfo.getResponse().getId());

        return memberRepository.findByEmail(MemberType.NAVER + naverUserInfo.getResponse().getId())
                .orElseGet(() -> {
                    Member newUser = Member.builder()
                            .email(MemberType.NAVER + naverUserInfo.getResponse().getId())
                            .password(encryptedPassword)
                            .nickname(naverUserInfo.getResponse().getNickname())
                            .profile_path(naverUserInfo.getResponse().getProfileImageUrl())
                            .role(Role.USER)
                            .memberType(MemberType.NAVER)
                            .build();

                    // 필요한 정보 세팅...
                    return memberRepository.save(newUser);
                });
    }

    private NaverUserProfileDTO getUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<NaverUserProfileDTO> response = restTemplate.exchange(
                userInfoUri, HttpMethod.GET, entity, NaverUserProfileDTO.class);

        return response.getBody();
    }

}
