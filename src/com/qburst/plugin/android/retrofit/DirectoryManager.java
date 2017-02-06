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

    public VirtualFile createDirectory(VirtualFile parentDir, String name) {
        if (parentDir == null) {
            return null;
        }
        VirtualFile childDir = isDirectoryExists(parentDir, name);
        if (childDir != null) {
            return childDir;
        }
        try {
            return parentDir.createChildDirectory(this, name);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private VirtualFile isDirectoryExists(VirtualFile parentDir, String name){
        for (VirtualFile file: parentDir.getChildren()) {
            if (file.getName().equals(name)){
                return file;
            }
        }
        return null;
    }
}
