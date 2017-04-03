package com.qassist.plugin.android.utils.classutils;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

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

