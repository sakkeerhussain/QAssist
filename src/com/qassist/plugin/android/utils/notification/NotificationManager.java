package com.qassist.plugin.android.utils.notification;

import com.intellij.notification.*;
import com.intellij.openapi.project.Project;

import javax.swing.*;

/**
 * Created by sakkeer on 18/01/17.
 */
public class NotificationManager {
    private static NotificationManager instance;

    private final NotificationGroup notificationGroup;

    public static NotificationManager get() {
        if (instance == null){
            instance = new NotificationManager();
        }
        return instance;
    }

    private NotificationManager() {
        notificationGroup = new NotificationGroup("QBurstAndroidPlugin",
            NotificationDisplayType.BALLOON, true, "sample", new ImageIcon());
    }

    public Notification createNotification(String title, String subtitle, String content, NotificationType notificationType){
        return notificationGroup.createNotification(title, subtitle, content, notificationType);
    }

    public void showNotification(Project project, Notification notification){
        Notifications.Bus.notify(notification, project);
    }

    public void showNotificationInfo(Project project, String title, String subtitle, String content){
        showNotification(project, createNotification(title, subtitle, content, NotificationType.INFORMATION));
    }

    public void showNotificationError(Project project, String title, String subtitle, String content){
        showNotification(project, createNotification(title, subtitle, content, NotificationType.ERROR));
    }

    public void integrationCompletedNotification(Project project){
        String message = "Retrofit integration completed to your Project...";
        showNotificationInfo(project, "Retrofit", "", message);
    }

    public void integrationFailedNotification(Project project, String errorMessage){
        String title = "Retrofit integration to your project failed";
        String message = errorMessage +
                " Please try agin or contact us in below email sakkeer@qburst.com for support";
        showNotificationError(project, "Retrofit", title, message);
    }
}
