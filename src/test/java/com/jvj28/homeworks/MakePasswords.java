package com.jvj28.homeworks;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.PBEConfig;
import org.jasypt.encryption.pbe.config.SimplePBEConfig;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MakePasswords {

    @Test
    void testMakePasswords() {

        StringEncryptor encryptor = stringEncryptor();

        String result = encryptor.encrypt("LutronGUI");
        System.out.println("hw.username=ENC(" + result + ")");
        String original = encryptor.decrypt(result);
        assertEquals("LutronGUI", original);

        result = encryptor.encrypt("xxxx");
        System.out.println("hw.password=ENC(" + result + ")");
        original = encryptor.decrypt(result);
        assertEquals("xxxx", original);

        result = encryptor.encrypt("lutron");
        System.out.println("spring.datasource.username=ENC(" + result + ")");
        original = encryptor.decrypt(result);
        assertEquals("lutron", original);

        result = encryptor.encrypt("xxxx");
        System.out.println("spring.datasource.password=ENC(" + result + ")");
        original = encryptor.decrypt(result);
        assertEquals("xxxx", original);

    }

    StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(System.getProperty("jasypt.encryptor.password"));
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
