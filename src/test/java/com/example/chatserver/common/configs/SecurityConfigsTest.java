package com.example.chatserver.common.configs;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SecurityConfigsTest {

    @Test
    public void test() {
        PasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String encoded = encoder.encode("1234");
        System.out.println("encoded: " + encoded);
        System.out.println("matches: " + encoder.matches("1234", encoded));
    }
}