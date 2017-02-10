package com.qburst.plugin.android.utils.string;

import com.qburst.plugin.android.utils.http.UrlParamModel;

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
    public List<HashMap<String, String>> getListOfPathVars(String url){
        ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
        return result;
    }

    public String getParamsPartOfUrl(String url){
        int questionMarkIndex = url.indexOf("?");
        if (questionMarkIndex != -1){
            return url.substring(questionMarkIndex+1);
        }else{
            return "";
        }
    }

    public String getParamsRemovedUrl(String url){
        int questionMarkIndex = url.indexOf("?");
        if (questionMarkIndex != -1){
            return url.substring(0, questionMarkIndex);
        }else{
            return url;
        }
    }

    public List<UrlParamModel> getListOfQueryParams(String url){
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
}
