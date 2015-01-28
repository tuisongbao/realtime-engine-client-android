package com.tuisongbao.android.engine.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HashHmacUtil {

    private HashHmacUtil() {
        // empty
    }

    public static String hmacSHA256(String data, String key) {
        return hashHmac("HmacSHA256", data, key);
    }

    private static String hashHmac(String algorithm, String data, String key) {
        String result = "";
        byte[] bytesKey = key.getBytes();
        final SecretKeySpec secretKey = new SecretKeySpec(bytesKey, algorithm);
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(secretKey);
            byte[] macData = mac.doFinal(data.getBytes());
            result = StrUtil.byte2hex(macData);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return result;
    }
}
