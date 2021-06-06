package com.upgrad.FoodOrderingApp.service.businness;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UtilityService {

    public boolean isStringEmptyOrNull(String variable) {
        if (variable == null || variable.isEmpty())
            return true;
        else
            return false;
    }

    public boolean isValidEmail(String email) {
        return email.matches("^(([A-Za-z0-9]*))(@)(([A-Za-z0-9]*))(?<!\\.)\\.(?!\\.)(([A-Za-z0-9]*))");
    }

    public boolean isPhoneNumberValid(String phoneno) {
        if (phoneno.length() == 10 && phoneno.matches("[0-9]+"))
            return true;
        else
            return false;
    }

    public boolean isPasswordValid(String password) {
        Pattern special = Pattern.compile("[\\[\\]#@$%&*!^]");
        Matcher hasSpecial = special.matcher(password);

        Pattern digit = Pattern.compile("[0-9]+");
        Matcher hasDigit = digit.matcher(password);

        Pattern alphabetUpper = Pattern.compile("[A-Z]+");
        Matcher hasUpperAlpha = alphabetUpper.matcher(password);
        return ((password.length() >= 8) && hasSpecial.find() && hasDigit.find() && hasUpperAlpha.find());
    }

    public boolean hasTokenExpired(String dateString){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = null;

        try {
            date = format.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (date.getTime() > System.currentTimeMillis()) {
            return false;
        } else {
            return true;
        }
    }
}
