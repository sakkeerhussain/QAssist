package com.qburst.plugin.android.retrofit.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.qburst.plugin.android.retrofit.RetrofitController;

/**
 * Created by sakkeer on 20/01/17.
 */
public class RetrofitIntegrateAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        new RetrofitController().integrateRetrofitAction(e);
    }
}
