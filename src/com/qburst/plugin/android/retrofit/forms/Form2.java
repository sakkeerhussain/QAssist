package com.qburst.plugin.android.retrofit.forms;

import com.qburst.plugin.android.retrofit.actions.RetrofitIntegrator;
import com.qburst.plugin.android.utils.log.Log;

import javax.swing.*;

/**
 * Created by sakkeer on 11/01/17.
 */
public class Form2 {
    private static final String TAG = "Form2";
    private JButton cancelButton;
    private JButton finishButton;
    private JPanel rootPanel;
    private JButton nextButton;
    private JButton previousButton;

    private RetrofitIntegrator controller;

    private Form2() {
        cancelButton.addActionListener(e -> controller.hideForm());
        /*finishButton.addActionListener(e -> {
        });*/
        previousButton.addActionListener(e -> controller.openForm1());
        nextButton.addActionListener(e -> controller.openForm3());
    }

    public static Form2 main(String[] args, JFrame frame) {
        Log.d(TAG,"Creating new form 2....");
        Form2 form = new Form2();
        frame.setContentPane(form.rootPanel);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        return form;
    }

    public void setData(RetrofitIntegrator controller){
        this.controller = controller;
    }
}
