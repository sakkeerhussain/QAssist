package com.qburst.plugin.android.retrofit.forms;

import com.qburst.plugin.android.retrofit.EndPointDataModel;
import com.qburst.plugin.android.retrofit.RetrofitController;
import com.qburst.plugin.android.utils.http.HTTPUtils;
import com.qburst.plugin.android.utils.log.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.lang.Character.isDigit;

/**
 * Created by sakkeer on 11/01/17.
 */
public class Form2 {
    private static final String TAG = "Form2";
    private int currentEndPoint;
    private Boolean flag;

    private JButton cancelButton;
    private JButton finishButton;
    private JPanel rootPanel;
    private JButton nextButton;
    private JButton previousButton;
    private JTextField endPointUrlTextField;
    private JComboBox methodChooserComboBox;
    private JTextArea requestModelTextArea;
    private JTextArea responseModelTextArea;
    private JButton formatResponseButton;
    private JButton formatRequestButton;
    private JTextField endPointNameTextField;
    private JLabel errorLabel;
    private JLabel requestModelLabel;

    private RetrofitController controller;

    private Form2() {

        cancelButton.addActionListener(e -> controller.hideForm());
        /*finishButton.addActionListener(e -> {
        });*/
        previousButton.addActionListener(e -> {
            flag = true;
            storeData();
            if (currentEndPoint <= 1) {
                controller.openForm1();
            } else {
                currentEndPoint--;
                setUpView();
            }
        });
        nextButton.addActionListener(e -> {
            flag = true;
            if (!validData()) {
                return;
            }
            storeData();
            if (currentEndPoint >= controller.getNoOfEndPoints()) {
                controller.openForm3();
            } else {
                currentEndPoint++;
                setUpView();
            }
        });
        formatRequestButton.addActionListener(e -> {
            String json = requestModelTextArea.getText();
            requestModelTextArea.setText(formatJson(json));
        });
        formatResponseButton.addActionListener(e -> {
            String json = responseModelTextArea.getText();
            responseModelTextArea.setText(formatJson(json));
        });
        DocumentListener documentListener = new DocumentListener() {
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
        endPointUrlTextField.getDocument().addDocumentListener(documentListener);
        endPointNameTextField.getDocument().addDocumentListener(documentListener);
        requestModelTextArea.getDocument().addDocumentListener(documentListener);
        responseModelTextArea.getDocument().addDocumentListener(documentListener);
        methodChooserComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(new HTTPUtils().isPayloadNotSupportingMethod(methodChooserComboBox.getSelectedItem().toString()))
                {
                    if(flag)
                        validData();
                    hideRequestField();
                }
                else
                {
                    if(flag)
                        validData();
                    showRequestField();
                }
            }

            private void showRequestField() {
                requestModelTextArea.setVisible(true);
                requestModelLabel.setVisible(true);
                formatRequestButton.setVisible(true);
            }


            private void hideRequestField() {
                if(flag)
                validData();
                requestModelTextArea.setVisible(false);
                requestModelLabel.setVisible(false);
                formatRequestButton.setVisible(false);
            }
        });
    }

    private boolean validData() {

        if (endPointNameTextField.getText().isEmpty()) {

            errorLabel.setText("End point Name is empty");
            return false;
        }
        if (isDigit(endPointNameTextField.getText().charAt(0))) {
            errorLabel.setText("End point name starts with digit");
            return false;
        }
        if(!endPointNameTextField.getText().matches("[_a-zA-Z][_a-zA-Z0-9]*"))
        {
            errorLabel.setText("End point name is not in valid format");
            return false;
        }
        if (endPointUrlTextField.getText().isEmpty()) {

            errorLabel.setText("End point URL is empty");
            return false;
        }
        if (isDigit(endPointUrlTextField.getText().charAt(0))) {

            errorLabel.setText("End point URL starts with digit");
            return false;
        }
        if (!endPointUrlTextField.getText().matches("[_a-zA-Z][_a-zA-Z0-9/]*")) {
            errorLabel.setText("End point URL is not in valid format");
            return false;
        }
        if (!new HTTPUtils().isPayloadNotSupportingMethod(methodChooserComboBox.getSelectedItem().toString())) {

            if (requestModelTextArea.getText().isEmpty()) {
                errorLabel.setText("Request model is empty");
                return false;
            }
            try {
                JSONObject jsonObject = new JSONObject(requestModelTextArea.getText());
            } catch (JSONException e) {
                e.printStackTrace();
                errorLabel.setText("Request model is not a valid JSON");
                return false;
            }
        }

        if (responseModelTextArea.getText().isEmpty()) {

            errorLabel.setText("Response model is empty");
            return false;
        }
        try {
            JSONObject jsonObject = new JSONObject(responseModelTextArea.getText());
        } catch (JSONException e) {
            e.printStackTrace();

            errorLabel.setText("Response model is not a valid JSON");
            return false;
        }
        errorLabel.setText("");
        return true;

    }

    private String formatJson(String json) {
        json = json.trim();
        if (json.startsWith("{")) {
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.toString(4);
        } else if (json.startsWith("[")) {
            JSONArray jsonArray = new JSONArray(json);
            return jsonArray.toString(4);
        } else {
            return json;
        }
    }

    private void storeData() {
        EndPointDataModel endPointData = new EndPointDataModel();
        endPointData.setEndPointNo(currentEndPoint);
        endPointData.setEndPointName(endPointNameTextField.getText());
        endPointData.setEndPointUrl(endPointUrlTextField.getText());
        endPointData.setMethod(methodChooserComboBox.getSelectedItem().toString());
        endPointData.setRequestModel(requestModelTextArea.getText());
        endPointData.setResponseModel(responseModelTextArea.getText());
        controller.setEndPointDataModel(endPointData);
    }

    private void setUpView() {
        flag = false;
        errorLabel.setText("");
        controller.setTitle("End point " + currentEndPoint);
        EndPointDataModel endPointData = controller.getEndPointDataModel(currentEndPoint);
        endPointNameTextField.setText(endPointData.getEndPointName());
        endPointUrlTextField.setText(endPointData.getEndPointUrl());
        methodChooserComboBox.setSelectedItem(endPointData.getMethod());
        requestModelTextArea.setText(endPointData.getRequestModel());
        responseModelTextArea.setText(endPointData.getResponseModel());
    }

    public static Form2 main(String[] args, JFrame frame) {
        Log.d(TAG, "Creating new form 2....");
        Form2 form = new Form2();
        frame.setContentPane(form.rootPanel);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.getRootPane().setDefaultButton(form.finishButton);
        frame.setVisible(true);

        Border border = new JTextField().getBorder();
        //form.methodChooserComboBox.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        form.requestModelTextArea.setBorder(border);
        form.responseModelTextArea.setBorder(border);

        return form;
    }

    public void setData(RetrofitController controller) {
        this.controller = controller;
    }

    public void setCurrentEndPoint(int currentEndPoint) {
        this.currentEndPoint = currentEndPoint;
        setUpView();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
