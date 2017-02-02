package com.qburst.plugin.android.retrofit;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by sakkeer on 02/02/17.
 */
public class JsonManager {

    private String formatJson(String json){
        json = json.trim();
        if (json.startsWith("{")) {
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.toString(4);
        } else if (json.startsWith("[")) {
            JSONArray jsonArray = new JSONArray(json);
            return jsonArray.toString(4);
        }else{
            return json;
        }
    }
}
