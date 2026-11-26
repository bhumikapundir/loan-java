package com.newgen.loan.model;

import java.util.EnumSet;
import java.util.Set;

public enum Status {
    DRAFT,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    DISBURSED,
    EXIT;

    public Set<Status> allowedNext() {
        switch (this) {
            case DRAFT:        return EnumSet.of(UNDER_REVIEW, REJECTED);
            case UNDER_REVIEW: return EnumSet.of(APPROVED, REJECTED);
            case APPROVED:     return EnumSet.of(DISBURSED);
            case DISBURSED:    return EnumSet.of(EXIT);
            default:           return EnumSet.noneOf(Status.class);
        }
    }

    public boolean canMoveTo(Status next) {
        return allowedNext().contains(next);
    }

    public boolean isActive() {
        return this == DRAFT || this == UNDER_REVIEW || this == APPROVED;
    }
}