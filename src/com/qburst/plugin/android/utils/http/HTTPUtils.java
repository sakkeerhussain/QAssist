package com.qburst.plugin.android.utils.http;

/**
 * Created by sakkeer on 08/02/17.
 */
public class HTTPUtils {

    public boolean isPayloadNotSupportingMethod(String method) {
        return ("GET".equals(method)
                || "DELETE".equals(method)
                || "HEAD".equals(method)
                || "OPTIONS".equals(method));
    }
}
