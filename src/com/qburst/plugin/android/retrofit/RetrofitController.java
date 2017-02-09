package com.qburst.plugin.android.retrofit;

import com.android.tools.idea.gradle.parser.BuildFileKey;
import com.android.tools.idea.gradle.parser.Dependency;
import com.android.tools.idea.gradle.parser.GradleBuildFile;
import com.android.tools.idea.gradle.parser.GradleSettingsFile;
import com.android.tools.idea.gradle.project.GradleProjectImporter;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.roots.impl.SourceFolderImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.SmartList;
import com.qburst.plugin.android.retrofit.forms.Form1;
import com.qburst.plugin.android.retrofit.forms.Form2;
import com.qburst.plugin.android.retrofit.forms.Form3;
import com.qburst.plugin.android.utils.classutils.ClassManager;
import com.qburst.plugin.android.utils.classutils.ClassModel;
import com.qburst.plugin.android.utils.classutils.FieldModel;
import com.qburst.plugin.android.utils.log.Log;
import com.qburst.plugin.android.utils.notification.NotificationManager;
import com.qburst.plugin.android.utils.string.StringUtils;
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes;
import org.jetbrains.jps.model.java.JavaSourceRootProperties;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by sakkeer on 11/01/17.
 */
public class RetrofitController {
    private static final String TAG = "RetrofitController";

    private Project project;
    private Module moduleSelected;
    private String baseUrl;
    private int noOfEndPoints;
    private List<EndPointDataModel> endPointDataModelList = new ArrayList<>();

    private JFrame frame;
    private SourceFolder sourceFolderSelected;

    public void integrateRetrofitAction(AnActionEvent event) {
        this.project = event.getData(PlatformDataKeys.PROJECT);
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
    public void setSourceFolderSelected(SourceFolder sourceFolderSelected){
        this.sourceFolderSelected = sourceFolderSelected;
    }

    public Module getModuleSelected()
    {
     return moduleSelected;
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
        hideForm();
        WriteCommandAction.runWriteCommandAction(project, () -> {
            String errorMessage = null;
            if (!addDependencies()){
                errorMessage = "Dependency injection failed!";
            }else if (!createPackage()){
                errorMessage = "Creating package failed!";
            }else if (!createClasses()){
                errorMessage = "Creating Class failed!";
            }

            if (errorMessage == null){
                NotificationManager.get().integrationCompletedNotification(project);
                GradleProjectImporter.getInstance().requestProjectSync(project, null);
            }else{
                NotificationManager.get().integrationFailedNotification( project, errorMessage);
            }
        });
    }

    private boolean addDependencies() {
        if (moduleSelected == null) { return false; }
        String moduleGradlePath = GradleSettingsFile.getModuleGradlePath(moduleSelected);
        if (moduleGradlePath == null) { return false; }
        GradleSettingsFile mySettingsFile = GradleSettingsFile.get(project);
        final GradleBuildFile buildFile;
        if (mySettingsFile == null) { return false; }
        buildFile = mySettingsFile.getModuleBuildFile(moduleGradlePath);
        if (buildFile == null) { return false; }
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
        Dependency retrofitLogging = new Dependency(Dependency.Scope.COMPILE,
                Dependency.Type.EXTERNAL,
                Constants.DEPENDENCY_RETROFIT_LOGGING);
        if (!dependencies.contains(retrofitLogging)) {
            dependencies.add((retrofitLogging));
            added = true;
        }
        if (added) {
            buildFile.setValue(BuildFileKey.DEPENDENCIES, dependencies);
        }
        return true;
    }

    private boolean createPackage(){
        SourceFolder sourceFolder = sourceFolderSelected;
// getSourceRoots(moduleSelected).get(0);
        DirectoryManager directoryManager = DirectoryManager.get();
        VirtualFile comDir = directoryManager.createDirectory(sourceFolder.getFile(), "com");
        if (comDir == null){ return false; }
        VirtualFile qBurstDir = directoryManager.createDirectory(comDir, "qburst");
        if (qBurstDir == null){ return false; }
        VirtualFile retrofitDir = directoryManager.createDirectory(qBurstDir, "retrofit");
        if (retrofitDir == null){ return false; }
        VirtualFile modelDir = directoryManager.createDirectory(retrofitDir, "model");
        if (modelDir == null){ return false; }
        if (directoryManager.createDirectory(modelDir, "request") == null){ return false; }
        if (directoryManager.createDirectory(modelDir, "response") == null){ return false; }
        return true;
    }

    private boolean createClasses() {

        PsiPackage pkgRequest = JavaPsiFacade.getInstance(project).findPackage(Constants.PACKAGE_NAME_RETROFIT_REQUEST);
        PsiPackage pkgResponse = JavaPsiFacade.getInstance(project).findPackage(Constants.PACKAGE_NAME_RETROFIT_RESPONSE);
        if (pkgRequest == null || pkgResponse == null){
            //NotificationManager.get().integrationFailedNotification(project, errorMessage);
            return false;
        }

        if (!createRequestModelClasses(pkgRequest.getDirectories()[0])){
            return false;
        }
        if (!createResponseModelClasses(pkgResponse.getDirectories()[0])){
            return false;
        }


        //get retrofit directory
        PsiPackage pkg = JavaPsiFacade.getInstance(project).findPackage(Constants.PACKAGE_NAME_RETROFIT);
        if (pkg == null){
            return false;
        }
        PsiDirectory psiDirectory = pkg.getDirectories()[0];
        if (!createServiceClass(psiDirectory)){
            return false;
        }
        if (!createManagerClass(psiDirectory)){
            return false;
        }
        return true;
    }

    private boolean createRequestModelClasses(PsiDirectory psiDirectoryRequest) {
        for (EndPointDataModel endPointDataModel : endPointDataModelList) {
            ClassModel classModel = new JsonManager().getRequestClassModel(endPointDataModel,
                    project, psiDirectoryRequest);
            classModel.setPackageName(Constants.PACKAGE_NAME_RETROFIT_REQUEST);
            if (!ClassManager.get().createClass(classModel)){
                return false;
            }
        }
        return true;
    }

    private boolean createResponseModelClasses(PsiDirectory psiDirectoryResponse) {
        for (EndPointDataModel endPointDataModel : endPointDataModelList) {
            ClassModel classModel = new JsonManager().getResponseClassModel(endPointDataModel,
                    project, psiDirectoryResponse);
            classModel.setPackageName(Constants.PACKAGE_NAME_RETROFIT_RESPONSE);
            if (!ClassManager.get().createClass(classModel)){
                return false;
            }
        }
        return true;
    }

    private boolean createServiceClass(PsiDirectory psiDirectory) {

        //Creating service class
        ClassModel classModel = new ClassModel(project, psiDirectory, Constants.className.SERVICE, ClassModel.Type.INTERFACE);
        classModel.setPackageName(Constants.PACKAGE_NAME_RETROFIT);
        for (int i = 0; i < noOfEndPoints; i++) {
            EndPointDataModel endPointData = endPointDataModelList.get(i);
            String methodString = String.format(Constants.ServiceInterface.POST,
                    endPointData.getEndPointUrl(),
                    endPointData.getResponseModelClassName(),
                    endPointData.getEndPointName(),
                    endPointData.getRequestModelClassName(),
                    new StringUtils().lowersFirstLetter(endPointData.getSimpleRequestModelClassName()));
            classModel.addMethod(methodString);
        }

        return ClassManager.get().createClass(classModel);
    }

    private boolean createManagerClass(PsiDirectory psiDirectory) {
        ClassModel classModel = new ClassModel(project, psiDirectory, Constants.className.MANAGER, ClassModel.Type.CLASS);
        classModel.setPackageName(Constants.PACKAGE_NAME_RETROFIT);
        FieldModel staticField = new FieldModel(classModel, "private", true, true,
                "String", "BASE_URL");
        staticField.setValue(new StringUtils().getValueAsString(baseUrl));
        classModel.addField(staticField);
        classModel.addMethod(Constants.GET_INSTANCE_METHOD);
        return ClassManager.get().createClass(classModel);
    }

    public List<SourceFolder> getSourceRoots(Module moduleSelected) {
        List<SourceFolder> result = new SmartList<>();
        ModuleRootManager root = ModuleRootManager.getInstance(moduleSelected);
        for (ContentEntry contentEntry : root.getContentEntries()) {
            Set<? extends JpsModuleSourceRootType<?>> rootType = Collections.singleton(JavaSourceRootType.SOURCE);
            final List<SourceFolder> sourceFolders = contentEntry.getSourceFolders(rootType);
            for (SourceFolder sourceFolder : sourceFolders) {
                if (sourceFolder == null) continue;
                if (sourceFolder.getFile() == null) continue;
                if (isForGeneratedSources((SourceFolderImpl) sourceFolder)) continue;
                result.add(sourceFolder);
            }
        }
        return result;
    }

    private boolean isForGeneratedSources(SourceFolderImpl sourceFolder) {
        JavaSourceRootProperties properties = sourceFolder.getJpsElement().getProperties(JavaModuleSourceRootTypes.SOURCES);
        return properties != null && properties.isForGeneratedSources();
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setNoOfEndPoints(int noOfEndPoints) {
        this.noOfEndPoints = noOfEndPoints;
    }
}
