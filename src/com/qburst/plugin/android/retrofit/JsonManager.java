package com.qburst.plugin.android.retrofit;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.util.PsiTypesUtil;
import com.qburst.plugin.android.utils.classutils.*;
import com.qburst.plugin.android.utils.string.ClassStringUtil;
import com.qburst.plugin.android.utils.string.StringUtils;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by sakkeer on 02/02/17.
 */
public class JsonManager {

    private HashMap<String, FieldModel> declareFields;
    private HashMap<String, ClassModel> declareClass;

    public JsonManager() {
        this.declareFields = new HashMap<>();
        this.declareClass = new HashMap<>();
    }

    public ClassModel getRequestClassModel(EndPointDataModel endPointDataModel, @NotNull Project project, @NotNull PsiDirectory directory) {
        String name = StringUtils.capitaliseFirstLetter(new String[]{endPointDataModel.getEndPointName(), Constants.STRING_REQUEST_MODEL});
        endPointDataModel.setRequestModelClassName(name);
        ClassModel classModel = new ClassModel(project, directory, name, ClassModel.Type.CLASS);
        parseJson(endPointDataModel.getRequestModel(), classModel);
        return classModel;
    }

    public ClassModel getResponseClassModel(EndPointDataModel endPointDataModel, Project project, PsiDirectory directory) {
        String name = StringUtils.capitaliseFirstLetter(new String[]{endPointDataModel.getEndPointName(), Constants.STRING_RESPONSE_MODEL});
        ClassModel classModel = new ClassModel(project, directory, name, ClassModel.Type.CLASS);
        parseJson(endPointDataModel.getResponseModel(), classModel);
        return classModel;
    }

    private void parseJson(String json, ClassModel classModel) {
        JSONObject jsonObj = parseJSONObject(json);
        List<String> generateFiled = collectGenerateFiled(jsonObj);
        List<FieldModel> createdFields = createFields(jsonObj, generateFiled, classModel);
        classModel.addAllFields(createdFields);
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

    private List<FieldModel> createFields(JSONObject json, List<String> fieldList, ClassModel parentClass) {
        List<FieldModel> fieldModelList = new ArrayList<>();
        List<String> listEntityList = new ArrayList<>();

        for (String key : fieldList) {
            Object value = json.get(key);
            if (value instanceof JSONArray) {
                listEntityList.add(key);
                continue;
            }
            FieldModel fieldModel = createField(parentClass, key, value);
            fieldModelList.add(fieldModel);
        }

        for (String key : listEntityList) {
            Object type = json.get(key);
            FieldModel fieldModel = createField(parentClass, key, type);
            fieldModelList.add(fieldModel);
        }

        return fieldModelList;
    }

    private FieldModel createField(ClassModel parentClass, String key, Object type) {
        String fieldName = key;
        fieldName = handleDeclareFieldName(fieldName, "");

        FieldModel fieldModel = typeByValue(parentClass, key, type);
        fieldModel.setFieldName(fieldName);
        return fieldModel;
    }

    private FieldModel typeByValue(ClassModel parentClass, String key, Object type) {
        FieldModel result;
        if (type instanceof JSONObject) {
            ClassModel classModel = existDeclareClass((JSONObject) type);
            if (classModel == null) {
                FieldModel fieldModel = new FieldModel(parentClass);
                ClassModel innerClassModel = createInnerClass(createSubClassName(key, type), (JSONObject) type, parentClass);
                fieldModel.setKey(key);
                fieldModel.setType(innerClassModel.getName());
                fieldModel.setTargetClass(innerClassModel);
                result = fieldModel;
            } else {
                FieldModel fieldModel = new FieldModel(parentClass);
                fieldModel.setKey(key);
                fieldModel.setTargetClass(parentClass);
                result = fieldModel;
            }
        } else if (type instanceof JSONArray) {
            result = handleJSONArray(parentClass, (JSONArray) type, key, 1);
        } else {
            FieldModel fieldModel = new FieldModel(parentClass);
            fieldModel.setKey(key);
            fieldModel.setType(DataType.typeOfObject(type).getValue());
            result = fieldModel;
            if (type != null) {
                result.setComment(type.toString());
            }
        }
        // TODO: 22/03/17 Remove below line. Seems overwriting value.
        result.setKey(key);
        return result;
    }

    private FieldModel handleJSONArray(ClassModel parentClass, JSONArray jsonArray, String key, int deep) {

        FieldModel fieldModel;
        if (jsonArray.length() > 0) {
            Object item = jsonArray.get(0);
            if (item instanceof JSONObject) {
                item = getJsonObject(jsonArray);
            }
            fieldModel = listTypeByValue(parentClass, key, item, deep);
        } else {
            fieldModel = new IterableFieldModel(parentClass);
            fieldModel.setKey(key);
            fieldModel.setType("?");
            ((IterableFieldModel) fieldModel).setDeep(deep);
        }
        return fieldModel;
    }

    private FieldModel listTypeByValue(ClassModel parentClass, String key, Object type, int deep) {

        FieldModel item;
        if (type instanceof JSONObject) {
            ClassModel classModel = existDeclareClass((JSONObject) type);
            if (classModel == null) {
                IterableFieldModel iterableFieldModel = new IterableFieldModel(parentClass);
                ClassModel innerClassModel = createInnerClass(createSubClassName(key, type), (JSONObject) type, parentClass);
                iterableFieldModel.setDeep(deep);
                iterableFieldModel.setTargetClass(innerClassModel);
                iterableFieldModel.setType(innerClassModel.getName());
                item = iterableFieldModel;
            } else {
                IterableFieldModel fieldModel = new IterableFieldModel(parentClass);
                fieldModel.setTargetClass(classModel);
                fieldModel.setType(classModel.getName());
                fieldModel.setDeep(deep);
                item = fieldModel;
            }

        } else if (type instanceof JSONArray) {
            FieldModel fieldModel = handleJSONArray(parentClass, (JSONArray) type, key, ++deep);
            fieldModel.setKey(key);
            item = fieldModel;
        } else {
            IterableFieldModel fieldModel = new IterableFieldModel(parentClass);
            fieldModel.setKey(key);
            fieldModel.setType(type.getClass().getSimpleName());
            fieldModel.setDeep(deep);
            item = fieldModel;
        }
        return item;
    }

    private ClassModel existDeclareClass(JSONObject jsonObject) {
        for (ClassModel classModel : declareClass.values()) {
            Iterator<String> keys = jsonObject.keys();
            boolean had = false;
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = jsonObject.get(key);
                had = false;
                for (FieldModel fieldModel : classModel.getFields()) {
                    if (fieldModel.getKey().equals(key) && DataType.isSameDataType(DataType.typeOfString(fieldModel.getType()), DataType.typeOfObject(value))) {
                        had = true;
                        break;
                    }
                }
                if (!had) {
                    break;
                }
            }
            if (had) {
                return classModel;
            }
        }
        return null;
    }

    private ClassModel createInnerClass(String className, JSONObject json, ClassModel parentClass) {

        ClassModel subClassModel = new ClassModel(parentClass, className, ClassModel.Type.CLASS);
        subClassModel.setPackageName(parentClass.getQualifiedName());

        Set<String> set = json.keySet();
        List<String> list = new ArrayList<>(set);
        List<FieldModel> fields = createFields(json, list, subClassModel);
        subClassModel.addAllFields(fields);

        subClassModel.setPackageName(parentClass.getQualifiedName());
        subClassModel.setName(className);

        handleDeclareClassName(subClassModel, "");
        parentClass.addInnerClass(subClassModel);

        return subClassModel;
    }

    private String createSubClassName(String key, Object o) {
        String name = "";
        if (o instanceof JSONObject) {
            if (TextUtils.isEmpty(key)) {
                return key;
            }
            name = ClassStringUtil.getCamelCaseFromUnderScore(key);
        }
        return name+Constants.STRING_MODEL;

    }

    private String handleDeclareFieldName(String fieldName, String appendName) {
        fieldName += appendName;
        // TODO: 06/02/17 check whether the field name already exists or not.
        if (false) {
            return handleDeclareFieldName(fieldName, "X");
        }
        return fieldName;
    }

    private void handleDeclareClassName(ClassModel className, String appendName) {
        className.setName(className.getName() + appendName);
        // TODO: 06/02/17 check whether the field name already exists or not.
        if (false) {
            handleDeclareClassName(className, "X");
        }
    }


    /*
    * Class to Json
    */
    public static String getJsonFromPsiClass(PsiType psiClassType){
        boolean listTypeField = false;
        if (psiClassType.getCanonicalText().startsWith(Constants.ClassName.JAVA_UTIL_LIST)) {
            psiClassType = ((PsiClassReferenceType) psiClassType).getParameters()[0];
            listTypeField = true;
        }
        PsiClass psiClass = PsiTypesUtil.getPsiClass(psiClassType);
        if (psiClass == null) {
            if (listTypeField){
                return "[{\"<null>\"},{\"<null>\"}]";
            }else {
                return "{\"<null>\"}";
            }
        }
        String json = "{";
        for (PsiField field : psiClass.getAllFields()) {
            PsiType type = field.getType();
            String value = ClassManager.get().getDummyDataOfType(type, true);
            if ("".equals(value)){
                value = getJsonFromPsiClass(type);
            }

            String jsonFieldName = field.getNameIdentifier().getText();
            for (PsiAnnotation psiAnnotation : field.getModifierList().getAnnotations()) {
                if (Constants.ClassName.SERIALIZED_NAME.equals(psiAnnotation.getQualifiedName())){
                    PsiNameValuePair[] attributes = psiAnnotation.getParameterList().getAttributes();
                    if (attributes.length > 0) {
                        jsonFieldName = attributes[0].getLiteralValue();
                    }
                }
            }

            json = json.concat("\"")
                    .concat(jsonFieldName)
                    .concat("\":")
                    .concat(value)
                    .concat(",");
        }
        if (json.length() > 1) {
            json = json.substring(0, json.length() - 1);
        }
        json = json.concat("}");
        if (listTypeField){
            json =  "["+json+","+json+"]";
        }
        if (JsonManager.isValidJson(json)){
            return JsonManager.formatJson(json);
        }else {
            return "Error reading json from model class";
        }
    }

    /*
    * Utils
    */
    public static boolean isValidJson(String json) {
        json = json.trim();
        if (json.startsWith("{")) {
            try {
                new JSONObject(json);
                return true;
            }catch (JSONException e){
                return false;
            }
        } else if (json.startsWith("[")) {
            try {
                new JSONArray(json);
                return true;
            }catch (JSONException e){
                return false;
            }
        } else {
            return false;
        }
    }

    public static String formatJson(String json) {
        json = json.trim();
        if (json.startsWith("{")) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                return jsonObject.toString(4);
            }catch (JSONException e){
                // TODO: 23/02/17 show invalid json message to user.
                return json;
            }
        } else if (json.startsWith("[")) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                return jsonArray.toString(4);
            }catch (JSONException e){
                // TODO: 23/02/17 show invalid json message to user.
                return json;
            }
        } else {
            return json;
        }
    }
}
