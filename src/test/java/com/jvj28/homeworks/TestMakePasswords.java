package com.jvj28.homeworks;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestMakePasswords {

    @Test
    void testMakePasswords() {

        StringEncryptor encryptor = stringEncryptor();

        String result = encryptor.encrypt("LutronGUI");
        System.out.println("hw.username=ENC(" + result + ")");
        String original = encryptor.decrypt(result);
        assertEquals("LutronGUI", original);

        result = encryptor.encrypt("b36ac9fd3c96cf5ea349a6dbf1e2c9aca044058304284762a9f32499");
        System.out.println("hw.password=ENC(" + result + ")");
        original = encryptor.decrypt(result);
        assertEquals("b36ac9fd3c96cf5ea349a6dbf1e2c9aca044058304284762a9f32499", original);

        result = encryptor.encrypt("lutron");
        System.out.println("spring.datasource.username=ENC(" + result + ")");
        original = encryptor.decrypt(result);
        assertEquals("lutron", original);

        result = encryptor.encrypt("xxxx");
        System.out.println("spring.datasource.password=ENC(" + result + ")");
        original = encryptor.decrypt(result);
        assertEquals("xxxx", original);

        BCryptPasswordEncoder benc = new BCryptPasswordEncoder();
        result = benc.encode("master");
        System.out.printf("Password: %s%n", result);
    }

    StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(System.getenv("JASYPT_ENCRYPTOR_PASSWORD"));
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        return encryptor;
    }
}
