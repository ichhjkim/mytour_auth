package com.mytour.auth.util;

import com.mytour.auth.domain.MemberDTO;
import com.mytour.auth.payload.request.SignupRequest;

import java.util.regex.Pattern;

public class ValidateMember {

    public static boolean validateMember(SignupRequest member) {

        return validateUsername(member)
                && validatePassword(member)
                && validateEmail(member);
    }

    public static boolean validateUsername(SignupRequest member) {
        String username = member.getUsername();

        if (username==null || "".equals(username)) return false;

        String user_name_reg = "[a-z0-9]{5,10}$";

        boolean matched = Pattern.matches(user_name_reg, username);
        System.out.println(matched);
        return matched;
    }

    public static boolean validatePassword(SignupRequest member) {

        String password = member.getPassword();

        if (password==null || "".equals(password)) return false;

        String pw_reg = "^((?=.*\\d)(?=.*[a-zA-Z])(?=.*[\\W]).{8,12})$";
        boolean matched = Pattern.matches(pw_reg, password);

        return matched;
    }

    public static boolean validateEmail(SignupRequest member) {
        String email = member.getEmail();

        if (email==null || "".equals(email)) return false;
        if (email.indexOf("--") > -1) return false;

        String email_reg = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$";
        boolean matched = Pattern.matches(email_reg, email);

        return matched;
    }
}
