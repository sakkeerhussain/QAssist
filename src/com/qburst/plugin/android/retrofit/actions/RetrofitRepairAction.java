package com.qburst.plugin.android.retrofit.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiPackage;
import com.qburst.plugin.android.retrofit.Constants;
import com.qburst.plugin.android.utils.notification.NotificationManager;
import org.jetbrains.annotations.NotNull;

/**
 * Created by sakkeer on 20/01/17.
 */
public class RetrofitRepairAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        NotificationManager.get().showNotificationInfo(project, "Retrofit", "Repair", "Not yet implemented!");
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
            e.getPresentation().setEnabled(false);

    }
}
