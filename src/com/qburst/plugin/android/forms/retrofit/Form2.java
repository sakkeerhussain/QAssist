package com.qburst.plugin.android.forms.retrofit;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.qburst.plugin.android.actions.RetrofitIntegrator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by sakkeer on 11/01/17.
 */
public class Form2 {
    private static JFrame frame;
    private JButton cancelButton;
    private JButton finishButton;
    private JPanel rootPanel;
    private JButton nextButton;
    private JButton previousButton;

    private RetrofitIntegrator controller;

    public Form2() {
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });
        finishButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: 11/01/17 Do integrate REST API to that project.
                hide();
            }
        });
        previousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.openForm1();
            }
        });
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.openForm3();
            }
        });
    }

    public void hide() {
        if (frame != null){
            frame.setVisible(false);
        }
    }

    public static Form2 main(String[] args) {
        System.out.println("Creating new form2....");
        Form2 form2 = new Form2();
        frame = new JFrame("Form 2");
        frame.setContentPane(form2.rootPanel);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        return form2;
    }

    public void setData(RetrofitIntegrator controller){
        this.controller = controller;
    }

    public boolean isShowing(){
        return frame.isShowing();
    }
}
