package com.example.chatserver.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Component
@Slf4j
public class JwtAuthFilter extends GenericFilter {



    /**
     * request에서 token을 꺼내서 검증
     * 정상이면 authentication 객체 만들어줘야함 doFilter, 이상이면 error
     */
    @Value("${jwt.secretKey}")
    private String secretKey;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        String token = httpServletRequest.getHeader("Authorization");

        try {
            if (token != null) {
                // token이 있는 경우만 Authentication 객체 생성
                if (!token.substring(0, 7).equals("Bearer ")) {
                    throw new AuthenticationServiceException("Bearer 형식이 아닙니다.");
                }

                String jwtToken = token.substring(7);

                // jwtToken을 검증하려면 signature를 검증해야한다.
                // header와 payload로 다시 jwtToken을 만들어 signature를 비교해본다.
                // token 검증 및 claims 추출
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(secretKey)
                        .build()
                        .parseClaimsJws(jwtToken) // secretkey로 다시 token을 만들어봄 + 비교
                        .getBody();


                // authentication 객체 생성
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + claims.get("role")));

                // 이미 jwt로 인증이 됐기때문에 password, credential 같은 2차 인증 불필요
                // “The credentials may be null if the Authentication is created after successful authentication.”
                UserDetails userDetails = new User(claims.getSubject(), "", authorities);
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());

                // securityContext 안에 securityContext안에 authentication 객체들 저장되어있음
                SecurityContextHolder.getContext().setAuthentication(authentication); // = 사용자 인증 정보

            }

            // 다시 chain으로 돌아감 & request에 token이 들어있음
            // token이 들어있지 않으면(회원가입, 로그인 -> Chain에서 permitAll()) 그냥 지나감
            filterChain.doFilter(servletRequest, servletResponse);

        } catch (Exception e) {
            e.printStackTrace();
            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().write("Invalid token");

        }
    }
}
