package com.qassist.plugin.android.retrofit.forms;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;


import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiPackage;
import com.qassist.plugin.android.retrofit.RetrofitController;
import com.qassist.plugin.android.retrofit.Constants;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

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
    private JLabel errorLabel;
    private JComboBox sourceFolderList;
    private JTextField packageNameTextField;
    private JLabel packageNameWarningLabel;

    private RetrofitController controller;
    private Project project;
    private List<Module> modules;
    private List<SourceFolder> sourceFolders;
    private DocumentListener errorListener;
    private Boolean flag = false;
    private DocumentListener warningListener;

    private Form1() {
        intializeArrayList();
        addActionListeners();
        addDocumenListener();

    }

    private void addDocumenListener() {
        createDocumentListener();
        baseUrlTextField.getDocument().addDocumentListener(errorListener);
        noOfEndPointsTextField.getDocument().addDocumentListener(errorListener);
        packageNameTextField.getDocument().addDocumentListener(errorListener);
        packageNameTextField.getDocument().addDocumentListener(warningListener);

    }

    private void validPackage() {
        if(!controller.isRepairMode()) {
            PsiPackage pkg = JavaPsiFacade.getInstance(project).findPackage(packageNameTextField.getText());
            if (!(pkg == null)) {
                packageNameWarningLabel.setText("Creating package with same name over writes the existing one");
                return;
            }

        }
        packageNameWarningLabel.setText("");
        return;

    }


    private void createDocumentListener() {
            errorListener = new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(flag)
                        validData();

                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(flag)
                        validData();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(flag)
                        validData();
                }
            };

         warningListener = new DocumentListener() {
             @Override
             public void insertUpdate(DocumentEvent e) {
                     validPackage();
             }

             @Override
             public void removeUpdate(DocumentEvent e) {
                     validPackage();
             }

             @Override
             public void changedUpdate(DocumentEvent e) {
                     validPackage();
             }
        };
        }


    private void intializeArrayList() {
        modules = new ArrayList<>();
        sourceFolders = new ArrayList<>();
    }

    private void addActionListeners() {
        cancelButtonActionListener();
        nextButtonActionListener();
        modulesListActionListener();
    }

    private boolean validData() {
        String baseUrl = baseUrlTextField.getText();
        try {
            URL url = new URL(baseUrl);
            URLConnection conn = url.openConnection();
            conn.connect();
        } catch (MalformedURLException exp) {
            errorLabel.setText("Invalid base URL provided.");
            return false;
        } catch (IOException ignored) {}

        String noOfEndPointsString = noOfEndPointsTextField.getText();
        try {
            if(Integer.parseInt(noOfEndPointsString)<1){
                errorLabel.setText("Invalid number provided for no. of end points.");
                return false;
            }
        }catch (Exception exception){
            errorLabel.setText("Invalid number provided for no. of end points.");
            return false;
        }
        if(!packageNameTextField.getText().matches(Constants.RegExp.PACKAGE_NAME)) {
            errorLabel.setText("Package name cannot start or end with '.'");
            return false;
        }
        errorLabel.setText("");
        return true;

    }


    private void cancelButtonActionListener() {
        cancelButton.addActionListener(e -> controller.hideForm());
    }
    private void nextButtonActionListener() {
        nextButton.addActionListener(e -> {
            flag = true;
            if(!validData())
                return;

            controller.setBaseUrl(baseUrlTextField.getText());
            controller.setPackageName(packageNameTextField.getText());
            controller.setNoOfEndPoints(Integer.parseInt(noOfEndPointsTextField.getText()));
            if(controller.getNoOfEndPoints()<controller.getSizeofEndPointDataModelList())
            {
                controller.setSizeofEndPointDataModelList(controller.getNoOfEndPoints());
            }
            controller.setModuleSelected(modules.get(modulesList.getSelectedIndex()));
            controller.setSourceFolderSelected(sourceFolders.get(sourceFolderList.getSelectedIndex()));
            controller.openForm2(true);
        });
    }
    private void modulesListActionListener() {
        modulesList.addActionListener(e -> updateSourceFolderList());
    }



    private void updateSourceFolderList() {
        controller.setModuleSelected(modules.get(modulesList.getSelectedIndex()));
        ModuleRootManager root = ModuleRootManager.getInstance(controller.getModuleSelected());
        VirtualFile contentRoot = root.getContentRoots()[0];
        sourceFolders = controller.getSourceRoots(controller.getModuleSelected());
        sourceFolderList.removeAllItems();
        for(SourceFolder sourceFolder: sourceFolders) {

            String contentRootStr = contentRoot.getUrl();
            String sourceRootStr = sourceFolder.getUrl();
            if (sourceRootStr.startsWith(contentRootStr)) {
                sourceFolderList.addItem(sourceRootStr.replaceFirst(contentRootStr, ""));
            }else{
                sourceFolderList.addItem(sourceRootStr);
            }
        }
        this.controller.setSourceFolderSelected(sourceFolders.get(0));

    }

    public static Form1 main(String[] args, JFrame frame) {
        Form1 form = new Form1();
        frame.setContentPane(form.rootPanel);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.getRootPane().setDefaultButton(form.finishButton);
        frame.setVisible(true);
        return form;
    }

    public void setData(RetrofitController controller, Project project, String baseUrl, String packageName, int noOfEndPoints, Module moduleSelected){
        this.controller = controller;
        this.project = project;
        for (Module module:ModuleManager.getInstance(project).getModules()){
            if (controller.getSourceRoots(module).size() >= 1){
                modules.add(module);
            }
        }

        for (Module module : modules) {
            modulesList.addItem(module.getName());
        }
        this.controller.setModuleSelected(modules.get(0));
        //SOURCE FOLDER LISTING

        updateSourceFolderList();

        this.controller.setTitle("base config");

        if (baseUrl == null || baseUrl.equals("")){
            baseUrlTextField.setText("http://");
        }else{
            baseUrlTextField.setText(baseUrl);
        }
        if (noOfEndPoints == 0){
            noOfEndPointsTextField.setText("");
        }else{
            noOfEndPointsTextField.setText(String.valueOf(noOfEndPoints));
        }
        if (moduleSelected != null){
            modulesList.setSelectedItem(moduleSelected);
        }
        packageNameTextField.setText(packageName);

    }
}
