package com.qburst.plugin.android.retrofit.forms;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.qburst.plugin.android.retrofit.actions.RetrofitIntegrator;

import javax.swing.*;

/**
 * Created by sakkeer on 11/01/17.
 */
public class Form1 {
    private JButton cancelButton;
    private JButton finishButton;
    private JPanel rootPanel;
    private JTextField baseUrlTextField;
    private JTextField noOfEndPointsTextField;
    private JButton nextButton;
    private JComboBox modulesList;

    private RetrofitIntegrator controller;
    private Project project;
    private Module[] modules;

    private Form1() {
        cancelButton.addActionListener(e -> controller.hideForm());
        //finishButton.addActionListener(e -> {});
        nextButton.addActionListener(e -> {
            String noOfEndPointsString = noOfEndPointsTextField.getText();
            int noOfEndPoints = 0;
            try {
                noOfEndPoints = Integer.parseInt(noOfEndPointsString);
            }catch (Exception exception){
                Messages.showMessageDialog(project, "Invalid number provided for no. of end points.",
                        "Exception", Messages.getInformationIcon());
                return;
            }
            controller.setBaseUrl(baseUrlTextField.getText());
            controller.setNoOfEndPoints(noOfEndPoints);
            controller.setModuleSelected(modules[modulesList.getSelectedIndex()]);
            controller.openForm2();
        });
    }

    public static Form1 main(String[] args, JFrame frame) {
        Form1 form = new Form1();
        frame.setContentPane(form.rootPanel);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        return form;
    }

    public void setData(RetrofitIntegrator controller, Project project){
        this.controller = controller;
        this.project = project;
        modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            modulesList.addItem(module.getName());
        }
        this.controller.setModuleSelected(modules[0]);
    }
}