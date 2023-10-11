package com.spring.demo.api.member.service;

import com.spring.demo.api.common.dto.BaseResponseDTO;
import com.spring.demo.api.common.service.DefaultService;
import com.spring.demo.domain.member.dto.RequestDTO.MemberChangeProfileDTO;
import com.spring.demo.domain.member.dto.RequestDTO.MemberInformDTO;
import com.spring.demo.domain.member.dto.RequestDTO.MemberJoinDTO;
import com.spring.demo.domain.member.dto.RequestDTO.MemberLoginDTO;
import com.spring.demo.domain.member.dto.ResponseDTO.MemberMyInfoDTO;
import com.spring.demo.domain.member.dto.ResponseDTO.MemberMyInfoProfileDTO;
import com.spring.demo.domain.member.entity.Member;
import com.spring.demo.global.jwt.dto.TokenDTO;
import com.spring.demo.global.redis.dto.AuthDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface MemberService extends DefaultService {
    BaseResponseDTO<String> signUp(MemberJoinDTO memberInfo);

    BaseResponseDTO<TokenDTO> signIn(MemberLoginDTO memberInfo, HttpServletResponse response);
    BaseResponseDTO<String> signOut(String email, String access_token);

    BaseResponseDTO<String> verifyMember(String email, String type);
    BaseResponseDTO<String> verifyAuthNum(AuthDTO memberInfo);

    BaseResponseDTO<MemberMyInfoDTO> getInfo(String email);
    BaseResponseDTO<MemberMyInfoProfileDTO> getMyData(String email);

    BaseResponseDTO<String> changePassword(String email, String newPW);
    BaseResponseDTO<String> changeProfile(String email, MemberChangeProfileDTO memberChangeProfileDTO, MultipartFile file) throws IOException;

    BaseResponseDTO<String> withdrawalUser(String email, String access_token);
}
