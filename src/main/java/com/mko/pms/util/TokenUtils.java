package com.mko.pms.util;

import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * @author Yuxz
 * @date 2019-03-11 14:44
 */
public class TokenUtils {
    private static final String KEY = "PMS_DG@#!~20190311";

    public static String getToken(String loginName) {
        String str = String.format("%s%s", loginName, KEY);
        try{
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.reset();
            md5.update(str.getBytes(StandardCharsets.UTF_8));
            final byte[] resultByte = md5.digest();
            str = new String(Hex.encodeHex(resultByte));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }


}