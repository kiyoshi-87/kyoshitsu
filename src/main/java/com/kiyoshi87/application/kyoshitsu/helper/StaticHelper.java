package com.kiyoshi87.application.kyoshitsu.helper;

import com.kiyoshi87.application.kyoshitsu.model.auth.SignUpResponse;
import com.kiyoshi87.application.kyoshitsu.model.common.UserDto;
import com.kiyoshi87.application.kyoshitsu.model.entity.UserEntity;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StaticHelper {

    public static UserDto toUserDto(UserEntity user) {
        return com.kiyoshi87.application.kyoshitsu.model.common.UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .build();
    }

    public static SignUpResponse toSignupResponse(UserEntity user) {
        return SignUpResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .build();
    }
}
