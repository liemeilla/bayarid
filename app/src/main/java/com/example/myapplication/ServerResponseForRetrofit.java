package com.example.myapplication;

import com.google.gson.annotations.SerializedName;

public class ServerResponseForRetrofit {
    //variable name should be same as in the json response from flask
    @SerializedName("response_msg")
    String response_msg;
    @SerializedName("response_code")
    int response_code;

    String getResponseMsg(){
        return response_msg;
    }

    int getResponseCode(){
        return response_code;
    }
}
