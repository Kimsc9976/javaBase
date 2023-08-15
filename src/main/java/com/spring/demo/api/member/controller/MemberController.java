package com.spring.demo.api.member.controller;

import com.spring.demo.api.member.dto.BaseResponseDTO;
import com.spring.demo.api.member.service.MemberService;
import com.spring.demo.domain.member.dto.MemberInformDTO;
import com.spring.demo.domain.member.dto.MemberJoinDTO;
import com.spring.demo.domain.member.dto.MemberLoginDTO;
import com.spring.demo.domain.member.entity.Member;
import com.spring.demo.global.jwt.dto.TokenDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/member")
@Tag(name = "Member 컨트롤러", description = "Member Controller API Document")
public class MemberController {

    private final MemberService memberService;
    public MemberController(MemberService memberService){
        this.memberService = memberService;
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "회원가입을 진행합니다", tags = {"View"})
    public ResponseEntity<? extends BaseResponseDTO<String>> signUp(@RequestBody MemberJoinDTO memberInfo) {
        BaseResponseDTO<String> response = memberService.signUp(memberInfo);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/signin")
    @Operation(summary = "로그인", description = "Id, PW로 로그인 진행", tags = {"View"})
    public ResponseEntity<? extends BaseResponseDTO<TokenDTO>> signIn(@RequestBody MemberLoginDTO memberInfo, HttpServletResponse httpResponse){
        BaseResponseDTO<TokenDTO> response = memberService.signIn(memberInfo, httpResponse);
        return ResponseEntity.status(response.getStatusCode()).body(response);

    }

    @PostMapping("/inform")
    @Operation(summary = "사용자 정보", description = "Post를 통해 받은 Email을 통해 정보를 받아옴", tags = {"View"})
    public  ResponseEntity<? extends BaseResponseDTO<Member>> getInfo(@RequestBody MemberInformDTO memberInfo){
//        System.out.println("제블 드러와줘");
        BaseResponseDTO<Member> response = memberService.getInfo(memberInfo);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}
