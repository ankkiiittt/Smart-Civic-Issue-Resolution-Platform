package com.smartcivic.service;

import com.smartcivic.dto.AuthResponse;
import com.smartcivic.dto.LoginRequest;
import com.smartcivic.dto.RegisterRequest;
import com.smartcivic.dto.UserResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserResponse getProfile(String username);
}
