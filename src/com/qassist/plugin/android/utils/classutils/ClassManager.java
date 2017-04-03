package com.qassist.plugin.android.utils.classutils;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.PsiFileFactoryImpl;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.psi.impl.source.tree.JavaElementType;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTypesUtil;
import com.qassist.plugin.android.retrofit.RetrofitController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sakkeer on 01/02/17.
 */
public class ClassManager {
    private static ClassManager instance;

    private ClassManager() {
    }

    public static ClassManager get() {
        if (instance == null) {
            instance = new ClassManager();
        }
        return instance;
    }

    public boolean createClass(ClassModel classModel) {
        PsiJavaFileImpl classExists = isClassExistsAsImmediateChild(classModel.getDirectory(), classModel.getName());
        if (classExists != null) {
            classExists.delete();
        }
        try {
            PsiFile file = PsiFileFactoryImpl.getInstance(classModel.getProject())
                    .createFileFromText(classModel.getName() + JavaFileType.DOT_DEFAULT_EXTENSION,
                            JavaFileType.INSTANCE,
                            classModel.getClassBaseFormat());
            file = (PsiFile) classModel.getDirectory().add(file);
            PsiClass classObj = ((PsiJavaFile)file).getClasses()[0];
            classModel.setPsiClass(classObj);
            for (FieldModel field : classModel.getFields()) {
                classModel.getPsiClass().add(field.getPsiField());
            }
            for (PsiMethod method : classModel.getMethods()) {
                classModel.getPsiClass().add(method);
            }
            for (ClassModel subClass : classModel.getSubClasses()) {
                if (!addSubClass(subClass, classObj)) {
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

    private boolean addSubClass(ClassModel subClassModel, PsiClass parentClass) {
        try {
            PsiClass psiSubClass = subClassModel.getPsiClassFromText(parentClass);
            psiSubClass = (PsiClass) parentClass.add(psiSubClass);
            subClassModel.setPsiClass(psiSubClass);
            for (FieldModel field : subClassModel.getFields()) {
                field.setTargetClass(subClassModel);
                psiSubClass.add(field.getPsiField());
            }

            for (PsiMethod method : subClassModel.getMethods()) {
                psiSubClass.add(method);
            }

            for (ClassModel subClass : subClassModel.getSubClasses()) {
                if (!addSubClass(subClass, psiSubClass)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private PsiJavaFileImpl isClassExistsAsImmediateChild(PsiDirectory parentDir, String name) {
        for (PsiElement file : parentDir.getChildren()) {
            if (file.getClass() == PsiJavaFileImpl.class
                    && ((PsiJavaFileImpl) file).getName().equals(name + ".java")) {
                return (PsiJavaFileImpl) file;
            }
        }
        return null;
    }

    public PsiJavaFileImpl isClassExists(String name, Project project, RetrofitController controller) {
        PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, name + ".java", GlobalSearchScope.allScope(project));
        if (psiFiles.length <= 0) {
            return null;
        } else {
            return (PsiJavaFileImpl) psiFiles[0];
        }
    }

    public Object getFieldValue(String fieldName, PsiClass classObj){
        PsiField field = classObj.findFieldByName(fieldName, true);
        String value = null;
        if (field == null) {
            return null;
        }
        value = field.getNode().findChildByType(JavaElementType.LITERAL_EXPRESSION).getText();
        if (field.getType().equalsToText(CommonClassNames.JAVA_LANG_STRING)){
            return value.substring(1, value.length()-1);
        }else if (PsiTypesUtil.compareTypes(field.getType(), PsiType.INT, true)){
            return Integer.parseInt(value);
        }
        return value;
    }

    public Module getContainingModule(PsiClass classObj) {
        for (Module module: ModuleManager.getInstance(classObj.getProject()).getModules()){
            GlobalSearchScope scope = GlobalSearchScope.moduleScope(module);
            PsiFile[] psiFiles = FilenameIndex.getFilesByName(classObj.getProject(), classObj.getName() + ".java", scope);
            if (psiFiles.length > 0) {
                return module;
            }
        }
        return null;
    }

    public String getDummyDataOfType(PsiType type, boolean stringWrappedInQuotes){
        if (type.equalsToText(CommonClassNames.JAVA_LANG_STRING)){
            if (stringWrappedInQuotes) {
                return "\"dummyStr\"";
            }else{
                return "dummyStr";
            }
        }else if (type.equalsToText(CommonClassNames.JAVA_LANG_INTEGER) || type.equalsToText("int")){
            return "1";
        }else if (type.equalsToText(CommonClassNames.JAVA_LANG_FLOAT) || type.equalsToText("float")){
            return "1.1";
        }else  if (type.equalsToText(CommonClassNames.JAVA_LANG_DOUBLE) || type.equalsToText("double")){
            return "1.1111111111";
        }else {
            return "";
        }
    }

    public List<ClassModel> createBaseClassModel(List<ClassModel> classModelList, String baseClassName){
        ClassModel baseClassModel = new ClassModel(classModelList.get(0));
        baseClassModel.setName(baseClassName);
        for (ClassModel classModel : classModelList) {
            List<FieldModel> baseClassFields = new ArrayList<>(baseClassModel.getFields());
            for (FieldModel fieldModel : baseClassFields) {
                if (!classModel.isFieldPresent(fieldModel)) {
                    baseClassModel.getFields().remove(fieldModel);
                }
            }
        }
        List<FieldModel> baseClassFields = baseClassModel.getFields();
        if (baseClassFields.size() <= 0){
            return classModelList;
        }
        for (ClassModel classModel : classModelList) {
            classModel.setSuperClass(baseClassName);
            List<FieldModel> fields = new ArrayList<>();
            for (FieldModel fieldModel : classModel.getFields()) {
                boolean found = false;
                for (FieldModel baseFieldModel : baseClassFields) {
                    if (baseFieldModel.equals(fieldModel)) {
                        found = true;
                    }
                }
                if (!found){
                    fields.add(fieldModel);
                }
            }
            classModel.setFields(fields);
        }
        classModelList.add(baseClassModel);
        return classModelList;
    }
}
