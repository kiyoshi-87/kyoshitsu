package com.kiyoshi87.application.kyoshitsu.service;

import com.kiyoshi87.application.kyoshitsu.model.auth.CustomUserDetails;
import com.kiyoshi87.application.kyoshitsu.exceptions.ApiException;
import com.kiyoshi87.application.kyoshitsu.model.entity.UserEntity;
import com.kiyoshi87.application.kyoshitsu.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found"));

        return new CustomUserDetails(user);
    }
}
