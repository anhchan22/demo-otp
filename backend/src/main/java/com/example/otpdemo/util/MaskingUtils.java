package com.example.otpdemo.util;

public final class MaskingUtils {

    private MaskingUtils() {
    }

    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return email;
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        String visiblePart = localPart.substring(0, Math.min(2, localPart.length()));
        return visiblePart + "***" + domain;
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }

        int visibleDigits = Math.min(4, phone.length());
        return "******" + phone.substring(phone.length() - visibleDigits);
    }
}
