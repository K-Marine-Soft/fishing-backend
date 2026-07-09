package com.kmarine.fishing.common;

public class XssUtil {

    // HTML 태그 제거
    public static String sanitize(String input) {
        if (input == null) return null;
        return input
                .replaceAll("<script.*?>.*?</script>", "")
                .replaceAll("<.*?>", "")
                .replaceAll("javascript:", "")
                .replaceAll("on\\w+\\s*=", "");
    }
}