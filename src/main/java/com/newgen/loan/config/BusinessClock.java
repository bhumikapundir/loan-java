package com.newgen.loan.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class BusinessClock {

    private static LocalDate overrideDate = null;

    public static void setBusinessDate(LocalDate date) {
        overrideDate = date;
    }

    public static void reset() {
        overrideDate = null;
    }

    public static LocalDate today() {
        return overrideDate != null ? overrideDate : LocalDate.now();
    }

    public static LocalDateTime now() {
        return LocalDateTime.of(today(), LocalTime.now());
    }
}