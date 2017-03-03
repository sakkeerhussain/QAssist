package com.qburst.plugin.android.utils.string;

/**
 * Created by sakkeer on 02/02/17.
 */
public class StringUtils {

    public static String lowersFirstLetter(String str){
        if (str.length()<1){
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    public static String capitaliseFirstLetter(String str){
        if (str.length()<1){
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String capitaliseFirstLetter(String[] strs){
        if (strs.length < 1){
            return "";
        }
        String result = "";
        for (String str:strs){
            result += capitaliseFirstLetter(str);
        }
        return result;
    }

    public static String getValueAsString(String str){
        return "\""+str+"\"";
    }

    public static String getUnwrapStringValue(String str){
        if (str.startsWith("\"")){
            str = str.substring(1);
        }
        if (str.endsWith("\"")){
            str = str.substring(0, str.length()-1);
        }
        return str;
    }
}
