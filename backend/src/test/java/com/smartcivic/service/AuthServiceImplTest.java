package com.smartcivic.service;

import com.smartcivic.dto.AuthResponse;
import com.smartcivic.dto.LoginRequest;
import com.smartcivic.dto.RegisterRequest;
import com.smartcivic.dto.UserResponse;
import com.smartcivic.entity.User;
import com.smartcivic.entity.UserRole;
import com.smartcivic.exception.BadRequestException;
import com.smartcivic.exception.ResourceNotFoundException;
import com.smartcivic.repository.UserRepository;
import com.smartcivic.security.CustomUserDetails;
import com.smartcivic.security.CustomUserDetailsService;
import com.smartcivic.security.JwtTokenUtil;
import com.smartcivic.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("citizenjoe")
                .email("joe@example.com")
                .password("password123")
                .firstName("Joe")
                .lastName("Citizen")
                .phoneNumber("1234567890")
                .build();

        loginRequest = LoginRequest.builder()
                .username("citizenjoe")
                .password("password123")
                .build();

        testUser = User.builder()
                .id(1L)
                .username("citizenjoe")
                .email("joe@example.com")
                .passwordHash("encodedPassword")
                .role(UserRole.CITIZEN)
                .firstName("Joe")
                .lastName("Citizen")
                .phoneNumber("1234567890")
                .build();

        userDetails = new org.springframework.security.core.userdetails.User(
                "citizenjoe", "encodedPassword", Collections.singletonList(new SimpleGrantedAuthority("ROLE_CITIZEN"))
        );
    }

    @Test
    void register_Success() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userDetailsService.loadUserByUsername(testUser.getUsername())).thenReturn(userDetails);
        when(jwtTokenUtil.generateToken(userDetails)).thenReturn("testToken");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("testToken", response.getToken());
        assertEquals("citizenjoe", response.getUsername());
        assertEquals("CITIZEN", response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_DuplicateUsername_ThrowsBadRequest() {
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtTokenUtil.generateToken(userDetails)).thenReturn("testToken");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("testToken", response.getToken());
        assertEquals("citizenjoe", response.getUsername());
        assertEquals("CITIZEN", response.getRole());
    }

    @Test
    void getProfile_Success() {
        when(userRepository.findByUsername("citizenjoe")).thenReturn(Optional.of(testUser));

        UserResponse response = authService.getProfile("citizenjoe");

        assertNotNull(response);
        assertEquals(testUser.getId(), response.getId());
        assertEquals(testUser.getUsername(), response.getUsername());
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getRole().name(), response.getRole());
    }

    @Test
    void getProfile_UserNotFound_ThrowsResourceNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.getProfile("unknown"));
    }
}
