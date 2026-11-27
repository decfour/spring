package com.kdj.food.auth.service;

import java.util.Locale;

public final class MemberEmail {
    private MemberEmail() {
    }

    public static String normalize(String email) {
        return email == null ? null : email.strip().toLowerCase(Locale.ROOT);
    }
}
