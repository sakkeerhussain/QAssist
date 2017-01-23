package com.qburst.plugin.android.retrofit;

/**
 * Created by sakkeer on 20/01/17.
 */
public class EndPointDataModel {
    private int endPointNo;
    private String endPointUrl;
    private String method;
    private String requestModel;
    private String responseModel;

    public EndPointDataModel() {
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
}
