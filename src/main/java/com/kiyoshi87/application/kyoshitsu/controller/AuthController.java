package com.kiyoshi87.application.kyoshitsu.controller;

import com.kiyoshi87.application.kyoshitsu.model.ApiResponseEntity;
import com.kiyoshi87.application.kyoshitsu.model.auth.LoginRequest;
import com.kiyoshi87.application.kyoshitsu.model.auth.LoginResponse;
import com.kiyoshi87.application.kyoshitsu.model.auth.SignUpResponse;
import com.kiyoshi87.application.kyoshitsu.model.auth.SignupRequest;
import com.kiyoshi87.application.kyoshitsu.model.common.UserDto;
import com.kiyoshi87.application.kyoshitsu.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "Signup, login, and current-user endpoints")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Create a new user account")
    @ApiResponse(
            responseCode = "200",
            description = "User account created",
            content = @Content(schema = @Schema(implementation = ApiResponseEntity.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid signup request")
    @PostMapping("/signup")
    public ApiResponseEntity<SignUpResponse> signup(@RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @Operation(summary = "Authenticate a user and return a JWT")
    @ApiResponse(
            responseCode = "200",
            description = "Authenticated successfully",
            content = @Content(schema = @Schema(implementation = ApiResponseEntity.class))
    )
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @PostMapping("/login")
    public ApiResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // TODO: Take this out of auth controller
    @Operation(summary = "Get the authenticated user's profile", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(
            responseCode = "200",
            description = "Authenticated user profile",
            content = @Content(schema = @Schema(implementation = ApiResponseEntity.class))
    )
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    @GetMapping("/me") // Auth Required at this point
    public ApiResponseEntity<UserDto> getUser(@Parameter(hidden = true) Authentication authentication) {
        return authService.getUser(authentication);
    }
}
