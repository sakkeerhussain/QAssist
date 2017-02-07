package com.qburst.plugin.android.utils.classutils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.qburst.plugin.android.utils.log.Log;
import com.qburst.plugin.android.utils.notification.NotificationManager;

/**
 * Created by sakkeer on 01/02/17.
 */
public class ClassManager {
    private static final String TAG = "ClassManager";
    private static ClassManager instance;

    private ClassManager() {}

    public static ClassManager get(){
        if (instance == null){
            instance = new ClassManager();
        }
        return instance;
    }

    public boolean createClass(ClassModel classModel) {
        PsiJavaFileImpl classExists = isClassExists(classModel.getDirectory(), classModel.getName());
        if (classExists != null) {
            classExists.delete();
        }
        try {
            PsiClass classObj = null;
            if (classModel.getType() == ClassModel.Type.CLASS) {
                classObj = JavaDirectoryService.getInstance().createClass(classModel.getDirectory(),
                        classModel.getName());
            }else if (classModel.getType() == ClassModel.Type.INTERFACE) {
                classObj = JavaDirectoryService.getInstance().createInterface(classModel.getDirectory(),
                        classModel.getName());
            }
            classModel.setPsiClass(classObj);
            for (FieldModel field:classModel.getFields()) {
                classModel.getPsiClass().add(field.getPsiField());
            }
            for (PsiMethod method:classModel.getMethods()) {
                classModel.getPsiClass().add(method);
            }
            for (ClassModel subClass:classModel.getSubClasses()) {
                if (!addSubClass(subClass, classObj)){
                    return false;
                }
            }
            JavaCodeStyleManager.getInstance(classModel.getProject()).shortenClassReferences(classObj);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean addSubClass(ClassModel subClassModel, PsiClass parentClass){
        try {
            PsiClass psiSubClass = subClassModel.getPsiClassFromText(parentClass);
            psiSubClass = (PsiClass) parentClass.add(psiSubClass);
            subClassModel.setPsiClass(psiSubClass);
            for (FieldModel field:subClassModel.getFields()) {
                field.setTargetClass(subClassModel);
                psiSubClass.add(field.getPsiField());
            }

            for (PsiMethod method:subClassModel.getMethods()) {
                psiSubClass.add(method);
            }

            for (ClassModel subClass:subClassModel.getSubClasses()) {
                if (!addSubClass(subClass, psiSubClass)){
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private PsiJavaFileImpl isClassExists(PsiDirectory parentDir, String name){
        for (PsiElement file: parentDir.getChildren()) {
            if (file.getClass() == PsiJavaFileImpl.class
                    && ((PsiJavaFileImpl)file).getName().equals(name+".java")){
                return (PsiJavaFileImpl) file;
            }
        }
        return null;
    }
}
