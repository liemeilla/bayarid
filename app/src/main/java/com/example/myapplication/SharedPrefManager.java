package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    //Properties
    public static final String SP_BAYAR_ID = "spBayarId";
    public static final String SP_ID_USER_LOGIN = "spIdUserLogin";
    public static final String SP_SUDAH_LOGIN = "spSudahLogin";

    SharedPreferences sp;
    SharedPreferences.Editor spEditor;

    //Constructor
    public SharedPrefManager(Context context){
        sp = context.getSharedPreferences(SP_BAYAR_ID, Context.MODE_PRIVATE);
        spEditor = sp.edit();
    }

    public void saveSPString(String keySP, String value){
        spEditor.putString(keySP, value);
        spEditor.commit();
    }

    public void saveSPInt(String keySP, int value){
        spEditor.putInt(keySP, value);
        spEditor.commit();
    }

    public void saveSPBoolean(String keySP, boolean value){
        spEditor.putBoolean(keySP, value);
        spEditor.commit();
    }

    public void deleteSharedPref(String key_id_login, String key_id_bool){
        spEditor.remove(key_id_login);
        spEditor.remove(key_id_bool);
        spEditor.apply();
    }

    public String getSPIdUserLogin(){
        return sp.getString(SP_ID_USER_LOGIN, "");
    }

    public Boolean getSPSudahLogin(){
        return sp.getBoolean(SP_SUDAH_LOGIN, false);
    }

}
