package com.qburst.plugin.android.retrofit;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.qburst.plugin.android.utils.log.Log;
import com.qburst.plugin.android.utils.notification.NotificationManager;

import java.io.IOException;

/**
 * Created by sakkeer on 01/02/17.
 */
public class DirectoryManager {
    private static final String TAG = "DirectoryManager";
    private static DirectoryManager instance;

    private DirectoryManager() {}

    public static DirectoryManager get(){
        if (instance == null){
            instance = new DirectoryManager();
        }
        return instance;
    }

    public void createDirectory(Project project, VirtualFile parentDir, String name, Listener listener){
        if (parentDir == null){
            listener.failedToCreateDirectory(project, "Parent dir is empty");
            return;
        }
        VirtualFile childDir = isDirectoryExists(parentDir, name);
        if (childDir != null){
            listener.createdDirectorySuccessfully(childDir);
            return;
        }
        WriteCommandAction.runWriteCommandAction(project, () -> {
                try {
                    VirtualFile createdDir = parentDir.createChildDirectory(this, name);
                    listener.createdDirectorySuccessfully(createdDir);
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.failedToCreateDirectory(project, e.getLocalizedMessage());
                }
            });
    }

    private VirtualFile isDirectoryExists(VirtualFile parentDir, String name){
        for (VirtualFile file: parentDir.getChildren()) {
            if (file.getName().equals(name)){
                return file;
            }
        }
        return null;
    }

    static abstract class Listener {
        abstract void createdDirectorySuccessfully(VirtualFile dir);
        void failedToCreateDirectory(Project project, String response){
            NotificationManager.get().integrationFailedNotification(project);
            Log.e(TAG, response);
        }
    }
}
