// EncryptionConverter.java 생성
package com.kmarine.fishing.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

@Converter
public class EncryptionConverter
        implements AttributeConverter<String, String> {

    // 운영에서는 환경변수로 분리 필요
    private static final String PASSWORD = "encryptKmarine2024";
    private static final String SALT     = "a1b2c3d4e5";

    private final TextEncryptor encryptor =
            Encryptors.text(PASSWORD, SALT);

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        return encryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return encryptor.decrypt(dbData);
    }
}