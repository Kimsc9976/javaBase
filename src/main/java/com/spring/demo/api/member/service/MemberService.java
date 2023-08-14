package com.spring.demo.api.member.service;

import com.spring.demo.api.member.dto.BaseResponseDTO;
import com.spring.demo.domain.member.dto.MemberInformDTO;
import com.spring.demo.domain.member.dto.MemberJoinDTO;
import com.spring.demo.domain.member.dto.MemberLoginDTO;
import com.spring.demo.domain.member.entity.Member;
import com.spring.demo.global.jwt.dto.TokenDTO;
import jakarta.servlet.http.HttpServletResponse;

public interface MemberService {
    BaseResponseDTO<String> signUp(MemberJoinDTO memberInfo);
    BaseResponseDTO<TokenDTO> signIn(MemberLoginDTO memberInfo, HttpServletResponse response);
    BaseResponseDTO<Member> getInfo(MemberInformDTO memberInfo);
}
