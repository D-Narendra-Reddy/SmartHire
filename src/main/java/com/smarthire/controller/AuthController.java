package com.smarthire.controller;

import com.smarthire.dto.ApiResponse;
import com.smarthire.dto.AuthResponseDto;
import com.smarthire.dto.LoginDto;
import com.smarthire.dto.RegisterDto;
import com.smarthire.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(@Valid @RequestBody LoginDto loginDto) {
        AuthResponseDto authResponseDto = authService.login(loginDto);
        ApiResponse<AuthResponseDto> response = new ApiResponse<>(true, "Login successful", authResponseDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterDto registerDto) {
        String message = authService.register(registerDto);
        ApiResponse<String> response = new ApiResponse<>(true, message, null);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
