package com.qburst.plugin.android.utils.classutils;

import com.intellij.psi.PsiField;
import org.json.JSONObject;

/**
 * Created by sakkeer on 03/02/17.
 */
public class FieldModel {
    private PsiField psiField;
    protected String key;
    protected String type;
    protected String fieldName;
    protected String value;
    protected ClassModel targetClass;

    //methods
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

    //getter and setter
    public PsiField getPsiField() {
        return psiField;
    }

    public void setPsiField(PsiField psiField) {
        this.psiField = psiField;
    }

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

    public ClassModel getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(ClassModel targetClass) {
        this.targetClass = targetClass;
    }
}
