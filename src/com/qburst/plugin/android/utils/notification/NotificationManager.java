package com.qburst.plugin.android.utils.notification;

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

}