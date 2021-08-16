package com.mytour.auth.util;

public class PrivacyInfoUtil {

    public static String maskingName(String name) {

        String firstName = name.substring(0, 1);
        String lastName = name.substring(name.length()-1, name.length());
        if (name.length()==2) {
            return firstName + "*";
        }
        String masking = "";
        for(int i=0;i<name.length()-2;i++) {
            masking += "*";
        }
        return firstName + masking + lastName;
    }
}
