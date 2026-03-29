package com.kiyoshi87.application.kyoshitsu.service;

import com.kiyoshi87.application.kyoshitsu.auth.JwtUtil;
import com.kiyoshi87.application.kyoshitsu.exceptions.ApiException;
import com.kiyoshi87.application.kyoshitsu.model.ApiResponse;
import com.kiyoshi87.application.kyoshitsu.model.Role;
import com.kiyoshi87.application.kyoshitsu.model.auth.LoginRequest;
import com.kiyoshi87.application.kyoshitsu.model.auth.LoginResponse;
import com.kiyoshi87.application.kyoshitsu.model.auth.SignUpResponse;
import com.kiyoshi87.application.kyoshitsu.model.auth.SignupRequest;
import com.kiyoshi87.application.kyoshitsu.model.common.UserDto;
import com.kiyoshi87.application.kyoshitsu.model.entity.UserEntity;
import com.kiyoshi87.application.kyoshitsu.repository.UserRepository;
import com.mongodb.MongoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.kiyoshi87.application.kyoshitsu.helper.StaticHelper.toSignupResponse;
import static com.kiyoshi87.application.kyoshitsu.helper.StaticHelper.toUserDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public ApiResponse<SignUpResponse> signup(SignupRequest request) {
        try {
            if(userRepository.existsByEmail(request.getEmail())) {
                throw new ApiException("Email already exists");
            }

            UserEntity user = UserEntity.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(Role.getByName(request.getRole()))
                    .build();

            userRepository.save(user);

            return ApiResponse.success(toSignupResponse(user));
        } catch (MongoException e) {
            log.error("Error with MongoDB while signup: {}", e.getMessage());
            throw new ApiException(e);
        }
    }

    public ApiResponse<LoginResponse> login(LoginRequest request) {
        try {
            UserEntity user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ApiException("User not found"));

            if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new ApiException("Invalid password");
            }

            String token = jwtUtil.generateToken(user.getEmail());

            log.info("User {} logged in", user.getEmail());

            return ApiResponse.success(LoginResponse.builder()
                    .token(token)
                    .build());
        } catch (MongoException e) {
            log.error("Error with MongoDB while login: {}", e.getMessage());
            throw new ApiException(e);
        }
    }

    public ApiResponse<UserDto> getUser(Authentication authentication) {
        try {
            String email = authentication.getName();

            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ApiException("User not found"));

            return ApiResponse.success(toUserDto(user));
        } catch (MongoException e) {
            log.error("Error with MongoDB while getting user: {}", e.getMessage());
            throw new ApiException(e);
        }
    }
}
