package com.qburst.plugin.android.utils.classutils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
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
    private List<ClassModel> subClasses;

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
        this.methods = new ArrayList<>();
        this.subClasses = new ArrayList<>();
    }

    public ClassModel(@NotNull ClassModel classModel,
                      @NotNull String name,
                      @NotNull Type type) {
        this(classModel.project, classModel.directory, name, type);
    }

    //methods
    public void addAllFields(List<FieldModel> fieldModelList){
        this.fields.addAll(fieldModelList);
    }

    public void addInnerClass(ClassModel classModel){
        this.subClasses.add(classModel);
    }

    public void addField(FieldModel field) {
        this.fields.add(field);
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

    //setters
    public void setName(String name){
        this.name = name;
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

    //getters
    public String getFullName() {
        // TODO: 06/02/17 return package name toooo
        return name;
    }
    public String getName() {
        return name;
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

    public enum Type{
        CLASS, INTERFACE
    }
}
