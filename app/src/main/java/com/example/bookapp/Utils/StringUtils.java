package com.example.bookapp.Utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringUtils {
    
    /**
     * Chuyển chuỗi có dấu thành không dấu.
     * VD: "Đắc Nhân Tâm" -> "Dac Nhan Tam"
     */
    public static String removeAccents(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("")
                .replace('đ', 'd')
                .replace('Đ', 'D');
    }
}
