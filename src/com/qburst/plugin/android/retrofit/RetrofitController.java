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
import com.qburst.plugin.android.utils.log.Log;
import com.qburst.plugin.android.utils.notification.NotificationManager;
import org.jetbrains.annotations.NotNull;
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
        hideForm();
        addDependencies();
        createPackage();
    }

    private void createClasses() {
        PsiPackage pkg = JavaPsiFacade.getInstance(project).findPackage(Constants.PACKAGE_NAME);
        if (pkg == null){
            NotificationManager.get().integrationFailedNotification(project);
            return;
        }
        PsiDirectory psiDirectory = pkg.getDirectories()[0];
        createServiceClass(psiDirectory);
    }

    private void createServiceClass(PsiDirectory psiDirectory) {
        ClassModel classModel = new ClassModel(project, psiDirectory, Constants.className.SERVICE, ClassModel.Type.INTERFACE);
        classModel.addMethod(String.format(Constants.ServiceInterface.POST, "url/", "ResponseClass", "methodName", "RequestClass", "requestObj"));
        ClassManager.get().createClass(classModel, new ClassManager.Listener() {
            @Override
            public void classCreatedSuccessfully(PsiClass dir) {
                createManagerClass(psiDirectory);
            }
        });
    }

    private void createManagerClass(PsiDirectory psiDirectory) {
        ClassModel classModel = new ClassModel(project, psiDirectory, Constants.className.MANAGER, ClassModel.Type.CLASS);
        classModel.addField(String.format(Constants.BASE_URL_FIELD, baseUrl));
        classModel.addMethod(Constants.GET_INSTANCE_METHOD);
        ClassManager.get().createClass(classModel, new ClassManager.Listener() {
            @Override
            public void classCreatedSuccessfully(PsiClass dir) {
                NotificationManager.get().integrationCompletedNotification(project);
            }
        });
    }

    private void createPackage() {
        SourceFolder sourceFolder = getSourceRoots().get(0);
        DirectoryManager directoryManager = DirectoryManager.get();
        directoryManager.createDirectory(project, sourceFolder.getFile(), "com", new DirectoryManager.Listener() {
            @Override
            public void createdDirectorySuccessfully(VirtualFile comDir) {

                directoryManager.createDirectory(project, comDir, "qburst", new DirectoryManager.Listener() {
                    @Override
                    public void createdDirectorySuccessfully(VirtualFile qBurstDir) {


                        directoryManager.createDirectory(project, qBurstDir, "retrofit", new DirectoryManager.Listener() {
                            @Override
                            public void createdDirectorySuccessfully(VirtualFile retrofitDir) {
                                createClasses();
                            }
                        });
                    }
                });
            }
        });
    }

    private List<SourceFolder> getSourceRoots() {
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
            Dependency retrofitLogging = new Dependency(Dependency.Scope.COMPILE,
                    Dependency.Type.EXTERNAL,
                    Constants.DEPENDENCY_RETROFIT_LOGGING);
            if (!dependencies.contains(retrofitLogging)) {
                dependencies.add((retrofitLogging));
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
