package com.vertexone.prod.auth.controller;

import com.vertexone.prod.auth.service.AuthenticationService;
import com.vertexone.prod.security.dto.request.LoginRequest;
import com.vertexone.prod.security.dto.response.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                authenticationService.login(request)
        );
    }
}