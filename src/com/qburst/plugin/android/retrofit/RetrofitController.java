package com.qburst.plugin.android.retrofit;

import com.android.tools.idea.gradle.parser.BuildFileKey;
import com.android.tools.idea.gradle.parser.Dependency;
import com.android.tools.idea.gradle.parser.GradleBuildFile;
import com.android.tools.idea.gradle.parser.GradleSettingsFile;
import com.android.tools.idea.gradle.project.GradleProjectImporter;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.vfs.newvfs.NewVirtualFile;
import com.intellij.openapi.vfs.newvfs.NewVirtualFileSystem;
import com.intellij.openapi.vfs.newvfs.impl.VirtualDirectoryImpl;
import com.intellij.openapi.vfs.newvfs.impl.VirtualFileSystemEntry;
import com.intellij.psi.*;
import com.qburst.plugin.android.retrofit.forms.Form1;
import com.qburst.plugin.android.retrofit.forms.Form2;
import com.qburst.plugin.android.retrofit.forms.Form3;
import com.qburst.plugin.android.utils.log.Log;
import com.qburst.plugin.android.utils.notification.NotificationManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sakkeer on 11/01/17.
 */
public class RetrofitController {
    private static final String TAG = "RetrofitController";
    private static final String COMMAND_TITLE = "Create Library";

    private Project project;
    private Module moduleSelected;
    private String baseUrl;
    private int noOfEndPoints;
    private List<EndPointDataModel> endPointDataModelList = new ArrayList<>();

    private JFrame frame;
    private AnActionEvent event;

    public void integrateRetrofitAction(AnActionEvent event) {
        this.project = event.getData(PlatformDataKeys.PROJECT);
        this.event = event;
        this.frame = new JFrame("Retrofit");
        openForm1();
    }

    public void openForm1(){
        Log.d(TAG, "openForm1() called");
        String[] flags = new String[0];
        Form1 form1 = Form1.main(flags, frame);
        form1.setData(this, project, baseUrl, noOfEndPoints, moduleSelected);
    }

    public void openForm2(boolean fromStart){
        Log.d(TAG, "openForm2() called");
        String[] flags = new String[0];
        Form2 form = Form2.main(flags, frame);
        form.setData(this);
        if (fromStart) {
            form.setCurrentEndPoint(1);
        }else{
            form.setCurrentEndPoint(this.noOfEndPoints);
        }
    }

    public void openForm3(){
        Log.d(TAG, "openForm3() called");
        String[] flags = new String[0];
        Form3 form3 = Form3.main(flags, frame);
        form3.setData(this);
    }

    public void hideForm(){
        Log.d(TAG, "hideForm() called");
        frame.setVisible(false);
    }

    public void setTitle(String title){
        frame.setTitle("Retrofit - "+title);
    }

    public void setModuleSelected(Module moduleSelected) {
        this.moduleSelected = moduleSelected;
    }

    public void setEndPointDataModel(EndPointDataModel endPointDataModel) {
        boolean alreadyExisting = false;
        for (EndPointDataModel endPointDataModelIter : endPointDataModelList) {
            if (endPointDataModelIter.getEndPointNo() == endPointDataModel.getEndPointNo()) {
                alreadyExisting = true;
                this.endPointDataModelList.set(this.endPointDataModelList.indexOf(endPointDataModelIter), endPointDataModel);
            }
        }
        if (!alreadyExisting) {
            this.endPointDataModelList.add(endPointDataModel);
        }
    }

    public EndPointDataModel getEndPointDataModel(int position) {
        for (EndPointDataModel endPointDataModel : endPointDataModelList) {
            if (endPointDataModel.getEndPointNo() == position) {
                return endPointDataModel;
            }
        }
        return new EndPointDataModel();
    }

    public int getNoOfEndPoints() {
        return noOfEndPoints;
    }

    public void integrateRetrofit() {
        String message = "Integrating Retrofit to your Project...";
        NotificationManager.get().showNotificationInfo(project, "Retrofit", "", message);
        hideForm();
        addDependencies();
        createClasses();
    }

    private boolean createClasses() {
        ModuleRootManager root = ModuleRootManager.getInstance(moduleSelected);
        for (VirtualFile file : root.getSourceRoots(false)) {
            System.out.println(file);
        }

        System.out.println("=====================");

        NewVirtualFile file = (NewVirtualFile) root.getSourceRoots(false)[7];
        String PACKAGE_NAME = "com.qburst.retrofit";
        String CLASS_NAME = "RetrofitManager";
        VirtualFile comDir = createDirectory(file, "com");
        VirtualFile qBurstDir = createDirectory(comDir, "qburst");
        VirtualFile retrofitDir = createDirectory(qBurstDir, "retrofit");
        if (retrofitDir == null){
            return false;
        }

        PsiPackage pkg = JavaPsiFacade.getInstance(project).findPackage(PACKAGE_NAME);
        if (pkg == null){
            return false;
        }

        for (PsiClass classObj : pkg.getClasses()){
            System.out.println(classObj);
        }
        PsiDirectory psiDirectory = pkg.getDirectories()[0];
        JavaDirectoryService.getInstance().createClass(psiDirectory, CLASS_NAME);
        return true;
    }

    private VirtualFile createDirectory(VirtualFile parentDir, String name){
        if (parentDir == null){
            return null;
        }
        VirtualFile childDir = isDirectoryExists(parentDir, name);
        if (childDir != null){
            return childDir;
        }
        try {
            childDir = parentDir.createChildDirectory(this, name);
            return childDir;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return childDir;
    }

    private VirtualFile isDirectoryExists(VirtualFile parentDir, String name){
        for (VirtualFile file: parentDir.getChildren()) {
            if (file.getName().equals(name)){
                return file;
            }
        }
        return null;
    }

    private void addDependencies() {
        ApplicationManager.getApplication().runWriteAction(() -> {
            if (moduleSelected == null) { return; }
            //CreateClassAction f = new CreateClassAction();
            String moduleGradlePath = GradleSettingsFile.getModuleGradlePath(moduleSelected);
            if (moduleGradlePath == null) { return; }
            GradleSettingsFile mySettingsFile = GradleSettingsFile.get(project);
            final GradleBuildFile buildFile;
            if (mySettingsFile == null) { return; }
            buildFile = mySettingsFile.getModuleBuildFile(moduleGradlePath);
            if (buildFile == null) { return; }
            List<Dependency> value = (List<Dependency>)buildFile.getValue(BuildFileKey.DEPENDENCIES);
            final List<Dependency> dependencies = value != null ? value : new ArrayList<Dependency>();

            boolean added = false;
            //new MavenArtifact
            Dependency retrofit = new Dependency(Dependency.Scope.COMPILE,
                    Dependency.Type.EXTERNAL,
                    Constants.DEPENDENCY_RETROFIT);
            if (!dependencies.contains(retrofit)) {
                dependencies.add((retrofit));
                added = true;
            }
            Dependency retrofitGson = new Dependency(Dependency.Scope.COMPILE,
                    Dependency.Type.EXTERNAL,
                    Constants.DEPENDENCY_RETROFIT_GSON);
            if (!dependencies.contains(retrofitGson)) {
                dependencies.add((retrofitGson));
                added = true;
            }
            if (added) {
                new WriteCommandAction<Void>(project, COMMAND_TITLE, buildFile.getPsiFile()) {
                    @Override
                    protected void run(@NotNull Result<Void> result) throws Throwable {
                        buildFile.setValue(BuildFileKey.DEPENDENCIES, dependencies);
                        GradleProjectImporter.getInstance().requestProjectSync(project, null);
                    }
                }.execute();
            }
        });


    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setNoOfEndPoints(int noOfEndPoints) {
        this.noOfEndPoints = noOfEndPoints;
    }
}
