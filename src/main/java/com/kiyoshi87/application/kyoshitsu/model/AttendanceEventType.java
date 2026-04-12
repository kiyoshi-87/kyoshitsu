package com.kiyoshi87.application.kyoshitsu.model;

import lombok.Getter;

@Getter
public enum AttendanceEventType {

    STARTED("started"),
    ENDED("ended"),
    MARKED("marked");

    final String value;

    AttendanceEventType(String value) {
        this.value = value;
    }
}
