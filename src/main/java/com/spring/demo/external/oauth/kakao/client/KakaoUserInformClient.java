package com.spring.demo.external.oauth.kakao.client;

import com.spring.demo.api.member.dto.BaseResponseDTO;
import com.spring.demo.domain.member.entity.Member;
import com.spring.demo.external.oauth.kakao.dto.KakaoTokenDTO;
import com.spring.demo.external.oauth.kakao.dto.KakaoUserProfileDTO;
import com.spring.demo.external.oauth.kakao.service.KakaoLoginServiceImpl;
import com.spring.demo.external.oauth.service.LoginService;
import com.spring.demo.global.jwt.dto.TokenDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Objects;

@Slf4j
@Controller
@RequiredArgsConstructor
@PropertySource("classpath:application-oauth.properties")
public class KakaoUserInformClient {

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String tokenProviderUri;

    private final KakaoLoginServiceImpl kakaoLoginService;
    private final LoginService loginService;
    @GetMapping("/oauth/kakao")
    public @ResponseBody BaseResponseDTO<TokenDTO> kakaoCallback(@RequestParam String code){

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);

        // POST 방식으로 Http 요청한다. 그리고 response 변수의 응답 받는다.
        ResponseEntity<KakaoTokenDTO> response = restTemplate.exchange(
                tokenProviderUri,
                HttpMethod.POST,
                kakaoTokenRequest,
                KakaoTokenDTO.class
        );

        Member member = kakaoLoginService.getKakaoMember(Objects.requireNonNull(response.getBody()));

        return loginService.SocialLogin(member);
    }


}
