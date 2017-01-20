package com.qburst.plugin.android.retrofit.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.qburst.plugin.android.utils.notification.NotificationManager;

/**
 * Created by sakkeer on 20/01/17.
 */
public class RetrofitRepairAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        NotificationManager.get().showNotificationInfo(project, "Retrofit", "Repair", "Not yet implemented!");
    }
}
