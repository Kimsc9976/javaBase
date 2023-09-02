package com.spring.demo.external.oauth.controller;

import com.spring.demo.domain.member.entity.Member;
import com.spring.demo.domain.member.repository.MemberRepository;
import com.spring.demo.global.jwt.dto.TokenDTO;
import com.spring.demo.global.jwt.service.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginController {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

//    public TokenDTO SocialLogin(Member member){
//
//        // Authentication 객체를 생성. 주의: 카카오 로그인이므로 password는 null이나 dummy 값을 사용.
//        UsernamePasswordAuthenticationToken authenticationToken =
//                new UsernamePasswordAuthenticationToken(member.getEmail(), null);
//
//        // Authentication 객체를 생성
//        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
//
//        // JWT 토큰 생성
//        TokenDTO tokenDTO = tokenProvider.generateTokenDTO(authentication);
//
//        // refreshToken을 DB에 저장
//        member.updateMemberToken(tokenDTO.getRefreshToken());
//        memberRepository.save(member);
//
//        // 이 부분은 필요에 따라 클라이언트에 토큰을 반환하는 방식으로 변경할 수 있습니다.
//        return new TokenResponse(tokenDTO.getAccessToken(), tokenDTO.getRefreshToken());
//    }

}
