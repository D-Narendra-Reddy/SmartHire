package com.smarthire.service;

import com.smarthire.dto.AuthResponseDto;
import com.smarthire.dto.LoginDto;
import com.smarthire.dto.RegisterDto;

public interface AuthService {
    AuthResponseDto login(LoginDto loginDto);
    String register(RegisterDto registerDto);
}
