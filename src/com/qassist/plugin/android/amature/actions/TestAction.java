package com.qassist.plugin.android.amature.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by sakkeer on 11/01/17.
 */
public class TestAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        System.out.println("Test action triggered.");
        String[] flags = new String[0];
    }
}
