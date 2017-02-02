package com.qburst.plugin.android.utils.string;

/**
 * Created by sakkeer on 02/02/17.
 */
public class StringUtils {

    public String capitaliseFirstLetter(String str){
        if (str.length()<1){
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public String capitaliseFirstLetter(String[] strs){
        if (strs.length < 1){
            return "";
        }
        String result = "";
        for (String str:strs){
            result += capitaliseFirstLetter(str);
        }
        return result;
    }
}
