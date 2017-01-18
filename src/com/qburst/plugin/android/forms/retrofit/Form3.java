package com.qburst.plugin.android.forms.retrofit;

import com.qburst.plugin.android.actions.RetrofitIntegrator;

import javax.swing.*;

/**
 * Created by sakkeer on 11/01/17.
 */
public class Form3 {
    private static JFrame frame;
    private JButton cancelButton;
    private JButton finishButton;
    private JPanel rootPanel;
    private JButton nextButton;
    private JButton previousButton;

    private RetrofitIntegrator controller;

    public Form3() {
        cancelButton.addActionListener(e -> hide());
        finishButton.addActionListener(e -> {
            // TODO: 11/01/17 Do integrate REST API to that project.
            controller.integrateRetrofit();
            controller.hideAllForm();
        });
        previousButton.addActionListener(e -> {
            controller.openForm2();
        });
        nextButton.addActionListener(e -> {

        });
    }

    public void hide() {
        if (frame != null){
            frame.setVisible(false);
        }
    }

    public static Form3 main(String[] args) {
        frame = new JFrame("Form 3");
        Form3 form = new Form3();
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

    public boolean isShowing(){
        return frame.isShowing();
    }
}
