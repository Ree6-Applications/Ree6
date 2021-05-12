package de.presti.ree6.utils;

import org.apache.commons.codec.binary.Base64;

public class Crypter {

    public static String de(String in) {
        try {
            return new String(Base64.decodeBase64(in));
        } catch (Exception ex) {
            return null;
        }
    }

    public static String en(String in) {
        try {
            return Base64.encodeBase64String(in.getBytes());
        } catch (Exception ex) {
            return null;
        }
    }

}