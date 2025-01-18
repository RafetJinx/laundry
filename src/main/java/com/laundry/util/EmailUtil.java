package com.laundry.util;

import org.apache.commons.validator.routines.EmailValidator;

public class EmailUtil {
    public static boolean isEmailValid(String email) {
        return EmailValidator.getInstance().isValid(email);
    }
}
