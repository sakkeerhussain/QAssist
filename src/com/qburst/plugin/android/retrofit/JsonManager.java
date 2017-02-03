package com.qburst.plugin.android.retrofit;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.qburst.plugin.android.utils.classutils.ClassModel;
import com.qburst.plugin.android.utils.classutils.FieldModel;
import com.qburst.plugin.android.utils.string.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by sakkeer on 02/02/17.
 */
public class JsonManager {

    private HashMap<String, FieldModel> declareFields;
    private HashMap<String, ClassModel> declareClass;

    public JsonManager() {
        this.declareFields = new HashMap<>();
        this.declareFields = new HashMap<>();
    }

    public ClassModel getRequestClassModel(EndPointDataModel endPointDataModel, @NotNull Project project, @NotNull PsiDirectory directory) {
        String name = new StringUtils().capitaliseFirstLetter(new String[]{endPointDataModel.getEndPointName(), Constants.STRING_REQUEST_MODEL});
        endPointDataModel.setRequestModelClassName(name);
        ClassModel classModel = new ClassModel(project, directory, name, ClassModel.Type.CLASS);
        parseJson(endPointDataModel.getRequestModel());
        classModel.addField("private String requstField;");
        return classModel;
    }

    public ClassModel getResponseClassModel(EndPointDataModel endPointDataModel, Project project, PsiDirectory directory) {
        String name = new StringUtils().capitaliseFirstLetter(new String[]{endPointDataModel.getEndPointName(), Constants.STRING_RESPONSE_MODEL});
        endPointDataModel.setResponseModelClassName(name);
        ClassModel classModel = new ClassModel(project, directory, name, ClassModel.Type.CLASS);
        classModel.addField("private String responseField;");
        return classModel;
    }

    private void parseJson(String json) {
        JSONObject jsonObj = parseJSONObject(json);
        List<String> generateFiled = collectGenerateFiled(jsonObj);
    }

    private JSONObject parseJSONObject(String jsonStr) {
        if (jsonStr.startsWith("{")) {
            return new JSONObject(jsonStr);
        } else if (jsonStr.startsWith("[")) {
            JSONArray jsonArray = new JSONArray(jsonStr);

            if (jsonArray.length() > 0 && jsonArray.get(0) instanceof JSONObject) {
                return getJsonObject(jsonArray);
            }
        }
        return null;

    }

    private JSONObject getJsonObject(JSONArray jsonArray) {
        JSONObject resultJSON = jsonArray.getJSONObject(0);

        for (int i = 1; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);
            if (!(value instanceof JSONObject)) {
                break;
            }
            JSONObject json = (JSONObject) value;
            for (String key : json.keySet()) {
                if (!resultJSON.keySet().contains(key)) {
                    resultJSON.put(key, json.get(key));
                }
            }
        }
        return resultJSON;
    }

    private List<String> collectGenerateFiled(JSONObject json) {
        Set<String> keySet = json.keySet();
        List<String> fieldList = new ArrayList<>();
        for (String key : keySet) {
            if (!existDeclareField(key, json)) {
                fieldList.add(key);
            }
        }
        return fieldList;
    }

    private boolean existDeclareField(String key, JSONObject json) {
        FieldModel fieldModel = declareFields.get(key);
        if (fieldModel == null) {
            return false;
        }
        return fieldModel.isSameType(json.get(key));
    }

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
