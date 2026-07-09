// TotpService.java
package com.kmarine.fishing.admin;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.stereotype.Service;

@Service
public class TotpService {

    private final GoogleAuthenticator gAuth =
            new GoogleAuthenticator();

    // 비밀키 생성 (최초 1회)
    public String generateSecretKey() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    // 코드 검증
    public boolean verifyCode(String secretKey, int code) {
        return gAuth.authorize(secretKey, code);
    }
}