package com.rezwan_cs.owleyeqr;

import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class QRCodeClass {
    String code;
    Boolean activation;

    public QRCodeClass(String code, Boolean activation) {
        this.code = code;
        this.activation = activation;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getActivation() {
        return activation;
    }

    public void setActivation(Boolean activation) {
        this.activation = activation;
    }
}
