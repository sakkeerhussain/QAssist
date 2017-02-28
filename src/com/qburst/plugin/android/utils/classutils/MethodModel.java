package com.qburst.plugin.android.utils.classutils;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
/**
 * Created by qburst on 27/2/17.
 */
public class MethodModel {
    protected String accessSpecifier;
    protected boolean isStatic;
    protected String returnType;
    protected String methodName;
    protected ClassModel targetClass;
    public MethodModel(@NotNull ClassModel targetClass) {
        this(targetClass, "private", false, null, null);
    }
    public MethodModel(@NotNull ClassModel targetClass, String accessSpecifier,
                      boolean isStatic, String returnType, String methodName) {
        this.targetClass = targetClass;
        this.isStatic = isStatic;
        this.accessSpecifier = accessSpecifier;
        this.returnType = returnType;
        this.methodName = methodName;
    }
    public PsiField getPsiField() {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(targetClass.getProject());
        String fieldStr = generateFieldText();
        PsiElement context = targetClass.getDirectory();
        if (targetClass.getPsiClass() != null){
            context = targetClass.getPsiClass();
        }
        return factory.createFieldFromText(fieldStr, context);
    }

    public boolean isSamereturnType(Object o) {
        if (o instanceof JSONObject) {
            if (targetClass != null) {
                return targetClass.isSame((JSONObject) o);
            }
        } else {
            return DataType.isSameDataType(DataType.typeOfString(returnType), DataType.typeOfObject(o));
        }
        return false;
    }

    public String generateFieldText() {
        StringBuilder fieldSb = new StringBuilder();
        fieldSb.append(accessSpecifier).append(" ");
        if (isStatic) {
            fieldSb.append("static ");
        }
        if (returnType != null) {
            fieldSb.append(returnType).append(" ");
        }else{
            fieldSb.append("void").append(" ");
        }
        fieldSb.append(methodName);
        fieldSb.append("(){}");

//        fieldSb.append(";");
//        if (comment != null) {
//            fieldSb.append(" //").append(key).append(" = ").append(comment);
//        }
        return fieldSb.toString();
    }

    public String getType() {
        return returnType;
    }

    public void setType(String type) {
        this.returnType = type;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public ClassModel getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(ClassModel targetClass) {
        this.targetClass = targetClass;
    }
}

