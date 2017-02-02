package com.qburst.plugin.android.utils.classutils;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.qburst.plugin.android.utils.log.Log;
import com.qburst.plugin.android.utils.notification.NotificationManager;

import javax.annotation.OverridingMethodsMustInvokeSuper;

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

    public void createClass(ClassModel classModel, Listener listener) {
        WriteCommandAction.runWriteCommandAction(classModel.getProject(), () -> {
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
                for (PsiField field:classModel.getFields()) {
                    classModel.getPsiClass().add(field);
                }
                for (PsiMethod method:classModel.getMethods()) {
                    classModel.getPsiClass().add(method);
                }
                JavaCodeStyleManager.getInstance(classModel.getProject()).shortenClassReferences(classObj);
                listener.classCreatedSuccessfully(classObj);
            } catch (Exception e) {
                e.printStackTrace();
                listener.failedToCreateClass(classModel.getProject(), e.getLocalizedMessage());
            }
        });
    }

    public PsiJavaFileImpl isClassExists(PsiDirectory parentDir, String name){
        for (PsiElement file: parentDir.getChildren()) {
            if (file.getClass() == PsiJavaFileImpl.class
                    && ((PsiJavaFileImpl)file).getName().equals(name+".java")){
                return (PsiJavaFileImpl) file;
            }
        }
        return null;
    }

    public static abstract class Listener {
        public abstract void classCreatedSuccessfully(PsiClass dir);

        @OverridingMethodsMustInvokeSuper
        void failedToCreateClass(Project project, String response){
            NotificationManager.get().integrationFailedNotification(project);
            Log.e(TAG, response);
        }
    }
}
