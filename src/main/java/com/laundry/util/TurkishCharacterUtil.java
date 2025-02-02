package com.laundry.util;

public class TurkishCharacterUtil {

    public static String convertTurkishCharacters(String input) {
        if (input == null) {
            return null;
        }
        String result = input;

        result = result.replace("İ", "I")
                .replace("I", "I")  // "I" zaten ASCII
                .replace("Ğ", "G")
                .replace("Ü", "U")
                .replace("Ş", "S")
                .replace("Ö", "O")
                .replace("Ç", "C");

        result = result.replace("ı", "i")
                .replace("ğ", "g")
                .replace("ü", "u")
                .replace("ş", "s")
                .replace("ö", "o")
                .replace("ç", "c");

        return result;
    }
}
