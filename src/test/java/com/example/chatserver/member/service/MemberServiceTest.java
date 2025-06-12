package com.example.chatserver.member.service;

import com.example.chatserver.member.domain.Member;
import com.example.chatserver.member.dto.MemberLoginReqDto;
import com.example.chatserver.member.dto.MemberSaveReqDto;
import com.example.chatserver.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberService memberService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void Create() {
        String username = "test1";
        String password = "1234";
        String email = "test1@naver.com";

        MemberSaveReqDto memberSaveReqDto = new MemberSaveReqDto(username, password, email);
        memberService.create(memberSaveReqDto);

        Member member = memberRepository.findByEmail(email).get();
        String hashedPassword = member.getPassword();
        assertThat(passwordEncoder.matches(password, hashedPassword)).isTrue();
    }

    @Test
    void login(){
        String username = "test1";
        String password = "1234";
        String email = "test1@naver.com";

        MemberSaveReqDto memberSaveReqDto = new MemberSaveReqDto(username, password, email);
        memberService.create(memberSaveReqDto);

        MemberLoginReqDto memberLoginReqDto = new MemberLoginReqDto(email, password);

        Assertions.assertThatCode(() -> {
            memberService.login(memberLoginReqDto);
        }).doesNotThrowAnyException();

    }
}