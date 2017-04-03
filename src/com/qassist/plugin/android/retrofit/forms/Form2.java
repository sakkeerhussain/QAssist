package com.qassist.plugin.android.retrofit.forms;

import com.qassist.plugin.android.retrofit.Constants;
import com.qassist.plugin.android.retrofit.EndPointDataModel;
import com.qassist.plugin.android.retrofit.JsonManager;
import com.qassist.plugin.android.retrofit.RetrofitController;
import com.qassist.plugin.android.utils.http.HTTPUtils;
import com.qassist.plugin.android.utils.http.UrlParamModel;
import com.qassist.plugin.android.utils.log.Log;
import com.qassist.plugin.android.utils.string.UrlStringUtil;
import org.json.JSONException;
import org.json.JSONObject;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.Character.isDigit;

/**
 * Created by sakkeer on 11/01/17.
 */
public class Form2 {
    private static final String TAG = "Form2";
    private int currentEndPoint;
    private Boolean flag;
    private DocumentListener documentListener;
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
    private JButton urlHelpButton;
    private JScrollPane scrollPanel;
    private JLabel responseModelErrorLabel;
    private JLabel requestModelEerrorLabel;
    private RetrofitController controller;
    private Form2() {
        addActionListener();
        addDocumenListener();
        addToolTipText();
    }

    private void addToolTipText() {
        endPointUrlTextField.setToolTipText("\"<html>Enter you end point URL with path parameters inside curly bracket with sample value<br/>(Eg: test/{id=4}/details/).<br/><br/>If you have list as value, then give values within a square bracket and have sample values as comma separated<br/>(Eg: names/[\\\"name_1\\\", \\\"name_2\\\"]/save/).<br/><br/> Include your query parameters after a '?' mark with separated '&' symbol<br/>(Eg: url/?key1=value1&key2=value2&field3=value3\\\"</html>\"");
        urlHelpButton.setToolTipText("\"<html>Enter you end point URL with path parameters inside curly bracket with sample value<br/>(Eg: test/{id=4}/details/).<br/><br/>If you have list as value, then give values within a square bracket and have sample values as comma separated<br/>(Eg: names/[\\\"name_1\\\", \\\"name_2\\\"]/save/).<br/><br/> Include your query parameters after a '?' mark with separated '&' symbol<br/>(Eg: url/?key1=value1&key2=value2&field3=value3\\\"</html>\"");
    }

    private void addActionListener() {
        cancelButtonActionListener();
        nextButtonActionListener();
        previousButtonActionListener();
        methodChooserComboBoxActionListener();
        formatRequestButtonActionListener();
        formatResponseButtonActionListener();

    }

    private void formatResponseButtonActionListener() {
        formatResponseButton.addActionListener(e -> {
            responseModelErrorLabel.setText("");
            String responseModelText = responseModelTextArea.getText();
            String json = JsonManager.formatJson(responseModelText);
            if (json == null){
                responseModelErrorLabel.setText(JsonManager.getFormatJsonError(responseModelText));
            }else {
                responseModelTextArea.setText(json);
            }
        });

    }

    private void formatRequestButtonActionListener() {
        formatRequestButton.addActionListener(e -> {
            requestModelLabel.setText("");
            String requestModelText = requestModelTextArea.getText();
            String json = JsonManager.formatJson(requestModelText);
            if (json == null){
                requestModelEerrorLabel.setText(JsonManager.getFormatJsonError(requestModelText));
            }else {
                requestModelTextArea.setText(json);
            }
        });
    }
    private void cancelButtonActionListener() {
        cancelButton.addActionListener(e -> controller.hideForm());
    }
    private void nextButtonActionListener() {
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
    }

    private void previousButtonActionListener() {
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
    }
    private void methodChooserComboBoxActionListener() {
        methodChooserComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(HTTPUtils.isPayloadNotSupportingMethod(methodChooserComboBox.getSelectedItem().toString()))
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


        });
    }

    private void addDocumenListener() {
        createDocumentListener();
        endPointUrlTextField.getDocument().addDocumentListener(documentListener);
        endPointNameTextField.getDocument().addDocumentListener(documentListener);
        requestModelTextArea.getDocument().addDocumentListener(documentListener);
        responseModelTextArea.getDocument().addDocumentListener(documentListener);
    }

    private void createDocumentListener() {
        documentListener = new DocumentListener() {
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

    private boolean validData() {
        requestModelEerrorLabel.setText("");
        responseModelErrorLabel.setText("");
        if (endPointNameTextField.getText().isEmpty()) {
            errorLabel.setText("End point Name is empty");
            return false;
        }
        if (isDigit(endPointNameTextField.getText().charAt(0))) {
            errorLabel.setText("End point name starts with digit");
            return false;
        }
        if(!endPointNameTextField.getText().matches("[_a-zA-Z][_a-zA-Z0-9]*")) {
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
        if (!endPointUrlTextField.getText().matches(Constants.RegExp.END_POINT_URL)) {
            errorLabel.setText("End point URL is not in valid format");
            return false;
        }
        if(!Objects.equals(checkForKeyRepetition(), null))
        {
            errorLabel.setText(checkForKeyRepetition());
            return false;
        }
        if (!HTTPUtils.isPayloadNotSupportingMethod(methodChooserComboBox.getSelectedItem().toString())) {
            if (requestModelTextArea.getText().isEmpty()) {
                errorLabel.setText("Request model is empty");
                return false;
            }
            try {
                new JSONObject(requestModelTextArea.getText());
            } catch (JSONException e) {
                e.printStackTrace();
                errorLabel.setText("Request model is not valid");
                return false;
            }
        }

        if (responseModelTextArea.getText().isEmpty()) {
            errorLabel.setText("Response model is empty");
            return false;
        }
        try {
            new JSONObject(responseModelTextArea.getText());
        } catch (JSONException e) {
            errorLabel.setText("Response model is not valid");
            return false;
        }
        errorLabel.setText("");
        return true;

    }

    private String checkForKeyRepetition() {
        List<UrlParamModel> queryParams = UrlStringUtil.getListOfQueryParams(endPointUrlTextField.getText());
        List<UrlParamModel> pathParams = UrlStringUtil.getListOfPathParams(endPointUrlTextField.getText());
        List<String> keys = new ArrayList<String>();
        for (UrlParamModel queryParam : queryParams) {
            keys.add(queryParam.getKey());
        }
        for (UrlParamModel pathParam : pathParams) {
            keys.add(pathParam.getKey());
        }
        for(int i=0;i<keys.size();i++) {
            for(int j=i+1;j<keys.size();j++) {
                if(keys.get(i).equals(keys.get(j))) {
                    return "Key \'"+keys.get(j)+"\' is repeated";
                }
            }
        }
        return null;
    }

    private void storeData() {
        EndPointDataModel endPointData = new EndPointDataModel();
        endPointData.setEndPointNo(currentEndPoint);
        endPointData.setEndPointName(endPointNameTextField.getText());
        endPointData.setEndPointUrl(endPointUrlTextField.getText());
        endPointData.setMethod(methodChooserComboBox.getSelectedItem().toString());
        endPointData.setRequestModel(requestModelTextArea.getText().trim());
        endPointData.setResponseModel(responseModelTextArea.getText().trim());
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
        form.scrollPanel.setBorder(null);
        form.urlHelpButton.setBorder(null);
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
