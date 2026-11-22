package com.vertexone.prod.auth.service;

import com.vertexone.prod.security.dto.request.LoginRequest;
import com.vertexone.prod.security.dto.response.LoginResponse;
import com.vertexone.prod.security.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    public LoginResponse login(LoginRequest request) {

        String email = request.email().trim().toLowerCase();

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                request.password()
                        )
                );

        String accessToken =
                jwtTokenService.generateAccessToken(authentication);

        return new LoginResponse(
                accessToken,
                "Bearer",
                jwtTokenService.getAccessTokenTtlSeconds()
        );
    }
}