package io.agora.scene.playzone.sub.quickstart;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class QuickStartUtils {

    /**
     * 随机生成一个userId，用于演示
     * Generate a random userId for demonstration purposes.
     */
    public static String genUserID() {
        return md5Hex8(UUID.randomUUID().toString());
    }

    public static String md5Hex8(String plainText) {
        byte[] secretBytes;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(plainText.getBytes());
        } catch (NoSuchAlgorithmException e) {
            return plainText;
        }
        String md5code = new BigInteger(1, secretBytes).toString(16);
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = String.format("0%s", md5code);
        }

        return md5code.substring(8, 16);
    }

}
