package com.kiyoshi87.application.kyoshitsu.model;

import lombok.Getter;

@Getter
public enum Role {

    STUDENT("student"),
    TEACHER("teacher"),
    ADMIN("admin");

    private final String name;

    Role(String name) {
        this.name = name;
    }

    public static Role getByName(String name) {
        for (Role role : values()) {
            if (role.name.equals(name)) {
                return role;
            }
        }
        return null;
    }

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
