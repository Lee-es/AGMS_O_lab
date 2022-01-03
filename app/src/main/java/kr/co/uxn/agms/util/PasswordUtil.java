package kr.co.uxn.agms.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordUtil {
    private PasswordUtil(){}

    public static String getEncrypt(String password){
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    public static boolean verify(String password, String bcryptHashString){
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), bcryptHashString);
        return result.verified;
    }
}
