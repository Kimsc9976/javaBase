package com.spring.demo.api.member.service;

import com.spring.demo.api.member.dto.BaseResponseDTO;
import com.spring.demo.domain.member.dto.MemberInformDTO;
import com.spring.demo.global.redis.dto.AuthDTO;
import jakarta.mail.MessagingException;

public interface EmailService {
        BaseResponseDTO<String> sendMail(String email, String type) throws MessagingException;
}
