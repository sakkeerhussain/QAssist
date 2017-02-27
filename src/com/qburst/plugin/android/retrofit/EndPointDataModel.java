package com.qburst.plugin.android.retrofit;

/**
 * Created by sakkeer on 20/01/17.
 */
public class EndPointDataModel {
    private int endPointNo;
    private boolean createIgnoreModelClasses;
    private String endPointName;
    private String endPointUrl;
    private String method;
    private String requestModel;
    private String responseModel;

    private String requestModelClassName;
    private String responseModelClassName;

    public EndPointDataModel() {
        this.endPointName = "";
        this.endPointUrl = "";
        this.method = "GET";
        this.requestModel = "";
        this.responseModel = "";
    }

    public int getEndPointNo() {
        return endPointNo;
    }

    public String getEndPointUrl() {
        return endPointUrl;
    }

    public String getMethod() {
        return method;
    }

    public String getRequestModel() {
        return requestModel;
    }

    public String getResponseModel() {
        return responseModel;
    }

    public String getEndPointName() {
        return endPointName;
    }

    public String getSimpleRequestModelClassName() {
        return requestModelClassName;
    }

    public String getResponseModelClassName() {
        return Constants.PACKAGE_NAME_RETROFIT_RESPONSE+"."+responseModelClassName;
    }

    public boolean isCreateIgnoreModelClasses() {
        return createIgnoreModelClasses;
    }

    //setters
    public void setEndPointNo(int endPointNo) {
        this.endPointNo = endPointNo;
    }

    public void setEndPointUrl(String endPointUrl) {
        this.endPointUrl = endPointUrl;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setRequestModel(String requestModel) {
        this.requestModel = requestModel;
    }

    public void setResponseModel(String responseModel) {
        this.responseModel = responseModel;
    }

    public void setEndPointName(String endPointName) {
        this.endPointName = endPointName;
    }

    public void setRequestModelClassName(String requestModelClassName) {
        this.requestModelClassName = requestModelClassName;
    }

    public void setResponseModelClassName(String responseModelClassName) {
        this.responseModelClassName = responseModelClassName;
    }

    public void setCreateIgnoreModelClasses(boolean createIgnoreModelClasses) {
        this.createIgnoreModelClasses = createIgnoreModelClasses;
    }
}
