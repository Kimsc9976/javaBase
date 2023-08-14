package com.spring.demo.api.member.controller;

import com.spring.demo.api.member.dto.BaseResponseDTO;
import com.spring.demo.api.member.service.MemberService;
import com.spring.demo.domain.member.dto.MemberInformDTO;
import com.spring.demo.domain.member.dto.MemberJoinDTO;
import com.spring.demo.domain.member.dto.MemberLoginDTO;
import com.spring.demo.domain.member.entity.Member;
import com.spring.demo.domain.member.exception.ApiException;
import com.spring.demo.global.jwt.dto.TokenDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/member")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService){
        this.memberService = memberService;
    }

    @PostMapping("/signup")
    public ResponseEntity<? extends BaseResponseDTO<String>> signUp(@RequestBody MemberJoinDTO memberInfo) {
        BaseResponseDTO<String> response = memberService.signUp(memberInfo);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/signin")
    public ResponseEntity<? extends BaseResponseDTO<TokenDTO>> signIn(@RequestBody MemberLoginDTO memberInfo, HttpServletResponse httpResponse){
        BaseResponseDTO<TokenDTO> response = memberService.signIn(memberInfo, httpResponse);
        return ResponseEntity.status(response.getStatusCode()).body(response);

    }

    @PostMapping("/inform")
    public  ResponseEntity<? extends BaseResponseDTO<Member>> getInfo(@RequestBody MemberInformDTO memberInfo){
//        System.out.println("제블 드러와줘");
        BaseResponseDTO<Member> response = memberService.getInfo(memberInfo);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/test")
    public ResponseEntity<? extends BaseResponseDTO<Member>> test(@RequestBody MemberLoginDTO memberInfo){
//        System.out.println("제블 드러와줘");
        BaseResponseDTO<String> response = memberService.test(memberInfo);
        return null;
    }
}
