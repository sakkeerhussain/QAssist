package com.qassist.plugin.android.utils.http;

/**
 * Created by sakkeer on 08/02/17.
 */
public class HTTPUtils {

    public static boolean isPayloadNotSupportingMethod(String method) {
        return ("GET".equals(method)
                || "HEAD".equals(method)
                || "OPTIONS".equals(method));
    }
}
