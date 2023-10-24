package com.fatec.rfidscanwave.util;

public class StringUtil {
    public static boolean isNumeric(String str){
        boolean isNumeric = true;

        for(char c : str.toCharArray()){
            if(!Character.isDigit(c)) {
                isNumeric = false;
                break;
            }
        }

        return isNumeric;
    }
}
