package com.qburst.plugin.android.fblogin.forms;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.qburst.plugin.android.retrofit.RetrofitController;

import javax.swing.*;

/**
 * Created by sakkeer on 11/01/17.
 */
public class Form1 {
    private static JFrame frame;
    private JButton cancelButton;
    private JButton finishButton;
    private JPanel rootPanel;
    private JTextField baseUrlTextField;
    private JTextField noOfEndPointsTextField;
    private JButton nextButton;
    private JComboBox modulesList;

    private RetrofitController controller;
    private Project project;
    private Module[] modules;

    public Form1() {
        cancelButton.addActionListener(e -> hide());
        finishButton.addActionListener(e -> {
            // TODO: 11/01/17 Do integrate REST API to that project.
            hide();
        });
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
            controller.openForm2(true);
        });
    }

    public void hide() {
        if (frame != null){
            frame.setVisible(false);
        }
    }

    public static Form1 main(String[] args) {
        frame = new JFrame("Form 1");
        Form1 form = new Form1();
        frame.setContentPane(form.rootPanel);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        return form;
    }

    public void setData(RetrofitController controller, Project project){
        this.controller = controller;
        this.project = project;
        modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            modulesList.addItem(module.getName());
        }
        this.controller.setModuleSelected(modules[0]);
    }

    public boolean isShowing(){
        return frame.isShowing();
    }
}
