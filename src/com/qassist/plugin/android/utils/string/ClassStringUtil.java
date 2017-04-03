package com.qassist.plugin.android.utils.string;

/**
 * Created by sakkeer on 08/02/17.
 */
public class ClassStringUtil {

    public static String getClassNameFromQualified(String qualifiedName){
        int lastIndex = qualifiedName.lastIndexOf(".");
        if (lastIndex != -1 && (lastIndex + 1) < qualifiedName.length()) {
            return qualifiedName.substring(lastIndex + 1);
        }else{
            return "";
        }
    }

    public static String getCamelCaseFromUnderScore(String underScoreName){
        String[] strings = underScoreName.split("_");
        return StringUtils.capitaliseFirstLetter(strings);
    }
}
