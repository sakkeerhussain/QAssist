package com.qburst.plugin.android.utils.classutils;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qburst on 27/2/17.
 */
public class MethodModel {
    protected String accessSpecifier;
    protected boolean isStatic;
    protected String returnType;
    protected String methodName;
    protected String innerContent;
    protected ClassModel targetClass;
    protected List<ParameterModel> parameterModel;
//    public MethodModel(@NotNull ClassModel targetClass) {
//        this(targetClass, "private", false, null, null);
//    }
    public MethodModel(@NotNull ClassModel targetClass, String accessSpecifier,
                      boolean isStatic, String returnType, String methodName,List<ParameterModel> parameterModel,String innerContent) {
        this.parameterModel=  new ArrayList<>();
        this.targetClass = targetClass;
        this.isStatic = isStatic;
        this.innerContent = innerContent;
        this.accessSpecifier = accessSpecifier;
        this.returnType = returnType;
        this.methodName = methodName;
        this.parameterModel = parameterModel;
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
        fieldSb.append("(");
        if(parameterModel!=null){
            for (int i=0; i<parameterModel.size();i++) {
                fieldSb.append(parameterModel.get(i).toString());
                if(parameterModel.size()-1 > i){
                    fieldSb.append(",");
                }
            }
        };
        fieldSb.append(")");
        fieldSb.append("{");
        fieldSb.append(innerContent);
        fieldSb.append("}");
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

