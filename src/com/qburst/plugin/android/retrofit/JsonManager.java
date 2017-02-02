package com.qburst.plugin.android.retrofit;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.qburst.plugin.android.utils.classutils.ClassModel;
import com.qburst.plugin.android.utils.string.StringUtils;
import org.jetbrains.annotations.NotNull;
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

    public ClassModel getRequestClassModel(EndPointDataModel endPointDataModel, @NotNull Project project, @NotNull PsiDirectory directory) {
        String name = new StringUtils().capitaliseFirstLetter(new String[]{endPointDataModel.getEndPointName(), Constants.STRING_REQUEST_MODEL});
        endPointDataModel.setRequestModelClassName(name);
        ClassModel classModel = new ClassModel(project, directory, name, ClassModel.Type.CLASS);
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
}
