package com.example.chatserver.common.configs;

import com.example.chatserver.common.auth.JwtAuthFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Slf4j
@Configuration
public class SecurityConfigs {

    private final JwtAuthFilter jwtAuthFilter;

    @Autowired
    public SecurityConfigs(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        return httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable) // csrf 보안 공격에 대비하지 않겠다 -> 코드, 서비스 운영하며 방어
                .httpBasic(AbstractHttpConfigurer::disable) // http basic 비활성화: 보안 인증 방법 중 하나
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/member/create", "/member/doLogin","/connect/**").permitAll() // 특정 url 패턴에 대해서는 authentication 객체 요구하지 않음(인증처리 예외)
                        .anyRequest().authenticated())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // session 방식 사용하지 않겠다
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class) // 검증
                .build();
    }

    // Filter 차원에서 cors 설정
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("*")); // 모든 http method 허용
        configuration.setAllowedHeaders(Arrays.asList("*")); // 모든 header값 허용
        configuration.setAllowCredentials(true); // 자격 증명 허용(인증 작업 관련한 자격 증명들 허용)

        // /** 모든 url 패턴에 대해 cors 허용 설정
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder(10); // 명시적으로 강도 지정
        }
}

