package com.qburst.plugin.android.retrofit.actions;

import com.android.tools.idea.gradle.parser.BuildFileKey;
import com.android.tools.idea.gradle.parser.Dependency;
import com.android.tools.idea.gradle.parser.GradleBuildFile;
import com.android.tools.idea.gradle.parser.GradleSettingsFile;
import com.android.tools.idea.gradle.project.GradleProjectImporter;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.qburst.plugin.android.retrofit.Constants;
import com.qburst.plugin.android.retrofit.forms.Form1;
import com.qburst.plugin.android.retrofit.forms.Form2;
import com.qburst.plugin.android.retrofit.forms.Form3;
import com.qburst.plugin.android.utils.log.Log;
import com.qburst.plugin.android.utils.notification.NotificationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.serialization.PathMacroUtil;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.openapi.vfs.VfsUtilCore.isEqualOrAncestor;

/**
 * Created by sakkeer on 11/01/17.
 */
public class RetrofitIntegrator extends AnAction {
    private static final String TAG = "RetrofitIntegrator";
    private static final String COMMAND_TITLE = "Create Library";

    private Project project;
    private Module moduleSelected;
    private String baseUrl;
    private int noOfEndPoints;

    private JFrame frame;

    @Override
    public void actionPerformed(AnActionEvent event) {
        project = event.getData(PlatformDataKeys.PROJECT);
        frame = new JFrame("Retrofit");
        openForm1();
    }

    public void openForm1(){
        Log.d(TAG, "openForm1() called");
        String[] flags = new String[0];
        Form1 form1 = Form1.main(flags, frame);
        form1.setData(this, project);
    }

    public void openForm2(){
        Log.d(TAG, "openForm2() called");
        String[] flags = new String[0];
        Form2 form2 = Form2.main(flags, frame);
        form2.setData(this);
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

    public void setModuleSelected(Module moduleSelected) {
        this.moduleSelected = moduleSelected;
    }

    public void integrateRetrofit() {
        String message = "Integrating Retrofit to your Project...";
        NotificationManager.get().showNotificationInfo(project, "Retrofit", "gagag", message);

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
