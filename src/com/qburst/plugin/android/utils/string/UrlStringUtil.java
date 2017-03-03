package com.qburst.plugin.android.utils.string;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.qburst.plugin.android.utils.classutils.ClassManager;
import com.qburst.plugin.android.utils.classutils.DataType;
import com.qburst.plugin.android.utils.http.UrlParamModel;

import javax.lang.model.element.TypeElement;
import javax.print.attribute.standard.JobStateReasons;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sakkeer on 08/02/17.
 */
public class UrlStringUtil {

    public static String getParamsPartOfUrl(String url){
        int questionMarkIndex = url.indexOf("?");
        if (questionMarkIndex != -1){
            return url.substring(questionMarkIndex+1);
        }else{
            return "";
        }
    }

    private static String getParamsRemovedUrl(String url){
        int questionMarkIndex = url.indexOf("?");
        if (questionMarkIndex != -1){
            return url.substring(0, questionMarkIndex);
        }else{
            return url;
        }
    }

    public static List<UrlParamModel> getListOfQueryParams(String url){
        String paramsPart = getParamsPartOfUrl(url);
        Matcher matcher = Pattern.compile("([a-zA-Z][a-zA-Z0-9_]*)=[^&=]+").matcher(paramsPart);
        List<UrlParamModel> result = new ArrayList<>();
        while(matcher.find()) {
            String matchedString = matcher.group();
            UrlParamModel item = new UrlParamModel();
            String queryKey = matchedString.substring(0, matchedString.indexOf("="));
            String queryValue = matchedString.substring(queryKey.length()+1);
            item.setKey(queryKey);
            item.setValue(queryValue);
            result.add(item);
            System.out.println(matchedString);
        }
        return result;
    }

    public static List<UrlParamModel> getListOfPathParams(String url){
        String initialPartOfUrl = getParamsRemovedUrl(url);
        Matcher matcher = Pattern.compile(Const.PATH_PARAM_REGEX).matcher(initialPartOfUrl);
        List<UrlParamModel> result = new ArrayList<>();
        while(matcher.find()) {
            String matchedString = matcher.group();
            UrlParamModel item = new UrlParamModel();
            String queryKey = matchedString.substring(1, matchedString.indexOf("="));
            String queryValue = matchedString.substring(queryKey.length()+2, matchedString.length()-1);
            item.setKey(queryKey);
            item.setValue(queryValue);
            result.add(item);
            System.out.println(matchedString);
        }
        return result;
    }

    public static String getPrettyUrl(String url) {
        String paramsRemovedUrl =  getParamsRemovedUrl(url);
        Matcher matcher = Pattern.compile(Const.PATH_PARAM_REGEX).matcher(paramsRemovedUrl);
        while(matcher.find()) {
            String matchedString = matcher.group();
            String queryKeyWithinBraces = matchedString.substring(0, matchedString.indexOf("=")).concat("}");
            paramsRemovedUrl = paramsRemovedUrl.replace(matchedString, queryKeyWithinBraces);
        }
        return paramsRemovedUrl;
    }

    public static String getUrlWithDummyData(String prettyUrl, PsiParameterList parameterList) {
        String url =  getParamsRemovedUrl(prettyUrl);
        String paramsPart = "";
        for(PsiParameter param: parameterList.getParameters()) {
            PsiAnnotation annotation = param.getModifierList().getAnnotations()[0];
            String paramType = annotation.getQualifiedName();
            if (Const.Retrofit.PATH.equals(paramType)) {
                String keyName = annotation.getParameterList().getAttributes()[0].getLiteralValue();
                String dummyData = ClassManager.get().getDummyDataOfType(param.getType());
                String queryWithDummyDataWithinBraces = "{".concat(keyName).concat("=").concat(dummyData).concat("}");
                String queryText = "{".concat(keyName).concat("}");
                url = url.replace(queryText, queryWithDummyDataWithinBraces);
            } else if (paramType.equals(Const.Retrofit.QUERY)) {
                String keyName = annotation.getParameterList().getAttributes()[0].getLiteralValue();
                String dummyData = ClassManager.get().getDummyDataOfType(param.getType());
                String query = "&".concat(keyName).concat("=").concat(dummyData);
                paramsPart = paramsPart.concat(query);
            }
        }
        if (!"".equals(paramsPart)){
            url = url.concat("?").concat(paramsPart.substring(1));
        }
        return url;
    }

    public String getParamType(String value) {
        if (value.startsWith("[")){
            String type = "java.util.List<%s>";
            // TODO: 15/02/17 Consider all child values
            String value1;
            if (value.length() >= 2 && value.charAt(1) == '[') {
                value1 = value.substring(1).split("]")[0];
            }else {
                value1 = value.substring(1).split(",")[0];
            }
            return String.format(type, getParamType(value1));
        }else {
            try{
                Integer.parseInt(value);
                return "Integer";
            }catch (NumberFormatException ignored){}
            try{
                Float.parseFloat(value);
                return "Float";
            }catch (NumberFormatException ignored){}
            try{
                Double.parseDouble(value);
                return "Double";
            }catch (NumberFormatException ignored){}
            return "String";
        }
    }
}
