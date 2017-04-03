package com.qassist.plugin.android.retrofit.forms;

import com.qassist.plugin.android.retrofit.RetrofitController;

import javax.swing.*;

/**
 * Created by sakkeer on 11/01/17.
 */
public class Form3 {
    private JButton cancelButton;
    private JButton finishButton;
    private JPanel rootPanel;
    private JButton nextButton;
    private JButton previousButton;
    private JTextPane weWillAdddRetrofitTextPane;

    private RetrofitController controller;

    private Form3() {
        cancelButton.addActionListener(e -> controller.hideForm());
        finishButton.addActionListener(e -> {
            // TODO: 11/01/17 Do integrate REST API to that project.
            controller.integrateRetrofit();
            controller.hideForm();
        });
        previousButton.addActionListener(e -> controller.openForm2(false));
        //nextButton.addActionListener(e -> {});
    }

    public static Form3 main(String[] args, JFrame frame) {
        Form3 form = new Form3();
        frame.setContentPane(form.rootPanel);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.getRootPane().setDefaultButton(form.finishButton);
        frame.setVisible(true);
        return form;
    }

    public void setData(RetrofitController controller){
        this.controller = controller;
        this.controller.setTitle("Confirm");
    }
}
