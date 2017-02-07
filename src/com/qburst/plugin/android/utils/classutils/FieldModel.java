package com.qburst.plugin.android.utils.classutils;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

/**
 * Created by sakkeer on 03/02/17.
 */
public class FieldModel {
    protected String accessSpecifier;
    protected boolean isStatic;
    protected boolean isFinal;
    protected String type;
    protected String fieldName;
    protected String value;
    private String comment;
    protected String key;
    protected ClassModel targetClass;

    public FieldModel(@NotNull ClassModel targetClass) {
        this(targetClass, "public", false, false, null, null);
    }

    public FieldModel(@NotNull ClassModel targetClass, String accessSpecifier,
                      boolean isStatic, boolean isFinal, String type, String fieldName) {
        this.targetClass = targetClass;
        this.isStatic = isStatic;
        this.isFinal = isFinal;
        this.accessSpecifier = accessSpecifier;
        this.type = type;
        this.fieldName = fieldName;
    }

    //methods
    public PsiField getPsiField() {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(targetClass.getProject());
        String fieldStr = generateFieldText();
        PsiElement context = targetClass.getDirectory();
        if (targetClass.getPsiClass() != null){
            context = targetClass.getPsiClass();
        }
        return factory.createFieldFromText(fieldStr, context);
    }

    public boolean isSameType(Object o) {
        if (o instanceof JSONObject) {
            if (targetClass != null) {
                return targetClass.isSame((JSONObject) o);
            }
        } else {
            return DataType.isSameDataType(DataType.typeOfString(type), DataType.typeOfObject(o));
        }
        return false;
    }

    private String generateFieldText() {
        StringBuilder fieldSb = new StringBuilder();

        // TODO: 06/02/17 add @SerializedName annotation with 'this.key'
        if (!fieldName.equals(this.getKey())) {
        }
        fieldSb.append(accessSpecifier).append(" ");
        if (isStatic) {
            fieldSb.append("static ");
        }

        if (type != null) {
            fieldSb.append(type).append(" ");
        }else{
            fieldSb.append("Null").append(" ");
        }

        fieldSb.append(fieldName).append(" ");
        if (value != null) {
            fieldSb.append("= ").append(value);
        }
        fieldSb.append(";");

//        if (comment != null) {
//            fieldSb.append(" //").append(key).append(" = ").append(comment);
//        }

        return fieldSb.toString();
    }

    //getter and setter
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ClassModel getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(ClassModel targetClass) {
        this.targetClass = targetClass;
    }
}
