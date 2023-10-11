package com.spring.demo.api.common.service;

import com.spring.demo.domain.member.entity.Member;
import com.spring.demo.domain.member.exception.ApiException;
import com.spring.demo.domain.member.repository.MemberRepository;

import java.util.Optional;

import static com.spring.demo.domain.member.exception.ErrorCode.NO_MEMBER_ERROR;

public interface DefaultService {

    default Member getMember(String email, MemberRepository memberRepository) {
        Optional<Member> optionalMember = memberRepository.findByEmail(email);
        if (optionalMember.isEmpty()) { throw new ApiException(NO_MEMBER_ERROR); }
        return optionalMember.get();
    }
}
