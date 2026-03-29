package com.kiyoshi87.application.kyoshitsu.controller;

import com.kiyoshi87.application.kyoshitsu.model.ApiResponse;
import com.kiyoshi87.application.kyoshitsu.model.auth.LoginRequest;
import com.kiyoshi87.application.kyoshitsu.model.auth.LoginResponse;
import com.kiyoshi87.application.kyoshitsu.model.auth.SignUpResponse;
import com.kiyoshi87.application.kyoshitsu.model.auth.SignupRequest;
import com.kiyoshi87.application.kyoshitsu.model.common.UserDto;
import com.kiyoshi87.application.kyoshitsu.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<SignUpResponse> signup(@RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // TODO: Take this out of auth controller
    @GetMapping("/me") // Auth Required at this point
    public ApiResponse<UserDto> getUser(Authentication authentication) {
        return authService.getUser(authentication);
    }
}
