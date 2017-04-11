package com.qassist.plugin.android.utils.classutils;

/**
 * Created by sakkeer on 28/2/17.
 */
public class ParameterModel {
    protected String argumentType;
    protected String argumentName;

    public ParameterModel(String argumentType, String argumentName) {
        this.argumentType = argumentType;
        this.argumentName = argumentName;
    }

    public String toString(){
        return argumentType+" "+argumentName;
    }
}

