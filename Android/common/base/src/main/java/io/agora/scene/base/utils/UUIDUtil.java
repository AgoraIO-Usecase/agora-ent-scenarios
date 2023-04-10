package io.agora.scene.base.utils;

import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class UUIDUtil {
    private static final String TAG = "UUIDUtil";

    public static String uuid(String uuid) {
        String[] hexArray = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(uuid.getBytes());
            byte[] rawBit = md.digest();
            String outputMD5 = " ";
            for (int i = 0; i < 16; i++) {
                outputMD5 = outputMD5 + hexArray[rawBit[i] >>> 4 & 0x0f];
                outputMD5 = outputMD5 + hexArray[rawBit[i] & 0x0f];
            }
            return outputMD5.trim();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

}
