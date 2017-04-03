package com.qassist.plugin.android.retrofit.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.qassist.plugin.android.retrofit.RetrofitController;

/**
 * Created by sakkeer on 20/01/17.
 */
public class RetrofitRepairAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
       new RetrofitController().repairRetrofitAction(e);
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);

        boolean repairAvail = new RetrofitController().isAvailRepairRetrofitAction(e);
        e.getPresentation().setEnabled(repairAvail);
    }


}

