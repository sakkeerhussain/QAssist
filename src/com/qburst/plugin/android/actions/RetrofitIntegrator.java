package com.qburst.plugin.android.actions;

import com.android.tools.idea.gradle.parser.BuildFileKey;
import com.android.tools.idea.gradle.parser.Dependency;
import com.android.tools.idea.gradle.parser.GradleBuildFile;
import com.android.tools.idea.gradle.parser.GradleSettingsFile;
import com.android.tools.idea.gradle.project.AndroidStudioGradleSettings;
import com.android.tools.idea.gradle.project.GradleProjectImporter;
import com.intellij.ide.actions.CreateClassAction;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.diagnostic.Logger;
import com.qburst.plugin.android.forms.retrofit.Form1;
import com.qburst.plugin.android.forms.retrofit.Form2;
import com.qburst.plugin.android.forms.retrofit.Form3;
import org.jetbrains.jps.model.serialization.PathMacroUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.openapi.vfs.VfsUtilCore.isEqualOrAncestor;

/**
 * Created by sakkeer on 11/01/17.
 */
public class RetrofitIntegrator extends AnAction {
    private static final Logger log = Logger.getInstance(RetrofitIntegrator.class);

    private Project project;
    private Module moduleSelected;
    private String baseUrl;
    private int noOfEndPoints;

    private Form3 form3;
    private Form2 form2;
    private Form1 form1;

    @Override
    public void actionPerformed(AnActionEvent event) {
        log.debug("Integrating Retrofit to your Project...");
        //log.error("Integrating Retrofit to your Project...");
        log.info("Integrating Retrofit to your Project...");
        project = event.getData(PlatformDataKeys.PROJECT);
        openForm1();
    }

    public void openForm1(){
        String[] flags = new String[0];
        form1 = Form1.main(flags);
        form1.setData(this, project);
    }

    public void openForm2(){
        hideAllForm();
        String[] flags = new String[0];
        form2 = Form2.main(flags);
        form2.setData(this);
    }

    public void openForm3(){
        String[] flags = new String[0];
        form3 = Form3.main(flags);
        form3.setData(this);
    }

    public void hideAllForm(){
        if (form1 != null && form1.isShowing()){
            form1.hide();
            form1 = null;
        }
        if (form2 != null && form2.isShowing()){
            System.out.println("Hiding form2....");
            form2.hide();
            form2 = null;
        }
        if (form3 != null && form3.isShowing()){
            form3.hide();
            form3 = null;
        }
    }

    public void setModuleSelected(Module moduleSelected) {
        this.moduleSelected = moduleSelected;
    }

    public void integrateRetrofit() {
        log.debug("Integrating Retrofit to your Project...");
                /*test code*/

        VirtualFile a = moduleSelected.getModuleFile();
        Module[] b = ModuleRootManager.getInstance(moduleSelected).getModuleDependencies(false);
        VirtualFile[] c = ModuleRootManager.getInstance(moduleSelected).getContentRoots();
        VirtualFile moduleRoot = c[0];
        String buildFileUrl = moduleRoot.getPath() + "/build.gradle";
        /*ApplicationManager.getApplication().runWriteAction(() -> {
            try{
                FileWriter fw = new FileWriter(buildFileUrl,true);
                fw.write("Base URL: "+baseUrl+", add a line\n");
                fw.close();
            }catch(IOException ioe){
                Messages.showInfoMessage(project, ioe.getLocalizedMessage(), "Exception");
                System.err.println("IOException: " + ioe.getMessage());
            }
        });*/

        ApplicationManager.getApplication().runWriteAction(() -> {
            if (moduleSelected == null) { return; }
            //CreateClassAction f = new CreateClassAction();
            String moduleGradlePath = GradleSettingsFile.getModuleGradlePath(moduleSelected);
            if (moduleGradlePath == null) { return; }
            GradleSettingsFile mySettingsFile = GradleSettingsFile.get(project);
            final GradleBuildFile buildFile = mySettingsFile.getModuleBuildFile(moduleGradlePath);
            List<Dependency> value = (List<Dependency>)buildFile.getValue(BuildFileKey.DEPENDENCIES);
            final List<Dependency> dependencies = value != null ? value : new ArrayList<Dependency>();
        });

        GradleProjectImporter.getInstance().requestProjectSync(project, null);

    }

    public void addSourceFolder(String relativePath, Module module) {
        ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();
        File directory = new File(PathMacroUtil.getModuleDir(module.getModuleFilePath()), relativePath);
        if(!directory.exists()) {
            directory.mkdirs();
        }
        VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(directory);
        ContentEntry e = getContentRootFor(virtualFile, rootModel);
        e.addSourceFolder(virtualFile, false);
        rootModel.commit();
    }

    private ContentEntry getContentRootFor(VirtualFile url, ModifiableRootModel rootModel) {
        for (ContentEntry e : rootModel.getContentEntries()) {
            if (isEqualOrAncestor(e.getUrl(), url.getUrl())) return e;
        }
        return null;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setNoOfEndPoints(int noOfEndPoints) {
        this.noOfEndPoints = noOfEndPoints;
    }
}
