package com.qburst.plugin.android.utils.classutils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.qburst.plugin.android.utils.string.StringUtils;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sakkeer on 02/02/17.
 */
public class ClassModel {

    private Type type;
    private String name;
    private Project project;
    private PsiClass psiClass;
    private String packageName;
    private PsiPackage packageObj;
    private PsiDirectory directory;
    private List<FieldModel> fields;
    private List<PsiMethod> methods;
    private List<MethodModel> methodModels;
    private List<ClassModel> subClasses;
    private String superClass;

    //constructor
    public ClassModel(@NotNull Project project,
                      @NotNull PsiDirectory directory,
                      @NotNull String name,
                      @NotNull Type type) {

        this.type = type;
        this.project = project;
        this.directory = directory;
        this.name = name;
        this.fields = new ArrayList<>();
        this.methodModels = new ArrayList<>();
        this.methods = new ArrayList<>();
        this.subClasses = new ArrayList<>();
    }

    public ClassModel(@NotNull ClassModel classModel,
                      @NotNull String name,
                      @NotNull Type type) {
        this(classModel.project, classModel.directory, name, type);
    }

    public ClassModel(ClassModel classModel) {
        this.type = classModel.type;
        this.name = classModel.name;
        this.project = classModel.project;
        this.psiClass = classModel.psiClass;
        this.packageName = classModel.packageName;
        this.packageObj = classModel.packageObj;
        this.directory = classModel.directory;
        this.fields = new ArrayList<>(classModel.fields);
        this.methods = new ArrayList<>(classModel.methods);
        this.methodModels = new ArrayList<>(classModel.methodModels);
        this.subClasses = new ArrayList<>(classModel.subClasses);
        this.superClass = classModel.superClass;
    }

    //methods
    public void addAllMethods(List<MethodModel> fieldModelList){
        this.methodModels.addAll(fieldModelList);
    }

    public void addAllFields(List<FieldModel> fieldModelList){
        this.fields.addAll(fieldModelList);
    }

    public void addInnerClass(ClassModel classModel){
        this.subClasses.add(classModel);
    }

    public void addField(FieldModel field) {
        this.fields.add(field);
    }
    public void addMethod(MethodModel field) {
        this.methodModels.add(field);
        addMethod(methodModels.get(methodModels.size()-1).generateFieldText());
    }
    public void addMethod(String methodString){
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(this.project);
        PsiMethod method = factory.createMethodFromText(methodString, directory);
        this.methods.add(method);
    }
    public boolean isSame(JSONObject o) {
        if (o == null) {
            return false;
        }
        boolean same = true;
        for (String key : o.keySet()) {
            same = false;
            for (FieldModel field : fields) {
                if (field.getKey().equals(key)) {
                    if (field.isSameType(o.get(key))) {
                        same = true;
                    }
                    break;
                }
            }
            if (!same) {
                break;
            }
        }
        return same;
    }
    public PsiClass getPsiClassFromText(PsiClass parentClass) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        String classStr = generateClassText();
        return factory.createClassFromText(classStr, parentClass).getInnerClasses()[0];
    }
    private String generateClassText() {
        StringBuilder fieldSb = new StringBuilder();
        fieldSb.append("public ");
        fieldSb.append("static ");
        fieldSb.append("class ");
        fieldSb.append(name).append(" ");
        fieldSb.append("{}");
        return fieldSb.toString();
    }
    public String getQualifiedName() {
        String fullClassName;
        if (!TextUtils.isEmpty(packageName)) {
            fullClassName = packageName + "." + name;
        } else {
            fullClassName = name;
        }

        return fullClassName;
    }

    public void generateGetterAndSetterMethods() {
        generateGetterAndSetterMethods(this);
    }

    private void generateGetterAndSetterMethods(ClassModel classModel) {
        List<FieldModel> fieldModels= classModel.getFields();
        if(classModel.getSubClasses().size()>0)
        {
            for(int i =0;i<classModel.getSubClasses().size();i++){
                generateGetterAndSetterMethods(classModel.getSubClasses().get(i));
            }
        }
        for (FieldModel field : fieldModels) {
            String fieldName = field.getFieldName();
            String fieldType = field.getFullNameType();
            String getMethodName = "get" + StringUtils.capitaliseFirstLetter(field.getFieldName());
            String setMethodName = "set" + StringUtils.capitaliseFirstLetter(field.getFieldName());
            String getInnerContent = "return this." + fieldName + ";";
            String setInnerContent = "this." + fieldName + " = " + fieldName + ";";
            List<ParameterModel> parameterModels = new ArrayList<>();
            ParameterModel parameterModel = new ParameterModel(fieldType, fieldName);
            parameterModels.add(parameterModel);
            MethodModel getMethod = new MethodModel(classModel, "public", false, fieldType, getMethodName, null, getInnerContent);
            MethodModel setMethod = new MethodModel(classModel, "public", false, "void", setMethodName, parameterModels, setInnerContent);
            classModel.addMethod(getMethod);
            classModel.addMethod(setMethod);
        }
    }

    //setters
    public void setName(String name){
        this.name = name;
    }

    public void setFields(List<FieldModel> fields) {
        this.fields = fields;
    }

    public void setPsiClass(PsiClass psiClass) {
        this.psiClass = psiClass;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setSuperClass(String superClass) {
        this.superClass = superClass;
    }

    //getters
    public String getName() {
        return name;
    }

    public String getFullName() {
        if (superClass == null || superClass.equals("")) {
            return name;
        }else{
            return name + " extends " + superClass;
        }
    }

    public String getClassBaseFormat() {
        return "public "+type.toString().toLowerCase()+" " + this.getFullName() + "{}";
    }

    public List<FieldModel> getFields() {
        return fields;
    }

    public List<PsiMethod> getMethods() {
        return methods;
    }

    public List<ClassModel> getSubClasses() {
        return subClasses;
    }

    public PsiClass getPsiClass() {
        return psiClass;
    }

    public PsiDirectory getDirectory() {
        return directory;
    }

    public Project getProject() {
        return project;
    }

    public Type getType() {
        return type;
    }

    public String getPackageName() {
        return packageName;
    }

    public boolean isFieldPresent(FieldModel fieldModel) {
        return fields.contains(fieldModel);
    }

    public String getSuperClass() {
        return superClass;
    }

    public enum Type{
        CLASS, INTERFACE
    }
}
