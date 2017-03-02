package com.qburst.plugin.android.utils.string;

import com.qburst.plugin.android.utils.http.UrlParamModel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
}
