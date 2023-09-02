package com.spring.demo.external.oauth.naver.client;

import com.spring.demo.api.member.dto.BaseResponseDTO;
import com.spring.demo.domain.member.entity.Member;
import com.spring.demo.external.oauth.naver.dto.NaverTokenDTO;
import com.spring.demo.external.oauth.naver.service.NaverLoginServiceImpl;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Slf4j
@Controller
@RequiredArgsConstructor
@PropertySource("classpath:application-oauth.properties")
public class NaverUserInformClient {
    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.naver.token-uri}")
    private String tokenProviderUri;

    private final NaverLoginServiceImpl naverLoginService;
    private final LoginService loginService;

    @GetMapping("/oauth/naver")
    public @ResponseBody BaseResponseDTO<TokenDTO> naverCallback(@RequestParam String code, @RequestParam String state){

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("state", state);

        HttpEntity<MultiValueMap<String, String>> naverTokenRequest = new HttpEntity<>(params, headers);

        // POST 방식으로 Http 요청한다. 그리고 response 변수의 응답 받는다.
        ResponseEntity<NaverTokenDTO> response = restTemplate.exchange(
                tokenProviderUri,
                HttpMethod.POST,
                naverTokenRequest,
                NaverTokenDTO.class
        );

        Member member = naverLoginService.getNaverMember(Objects.requireNonNull(response.getBody()));

        return loginService.SocialLogin(member);
    }

}
