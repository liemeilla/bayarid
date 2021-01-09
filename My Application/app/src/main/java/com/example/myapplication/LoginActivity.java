package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;

import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private EditText email, password;
    private Button btnLogin;
    private RequestQueue mQueue;
    SharedPrefManager sharedPrefManager;
    ProgressBar pbLogin;
    CustomProgress customProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        customProgress = CustomProgress.getInstance();

        btnLogin = (Button) findViewById(R.id.btnLogin);

        pbLogin = (ProgressBar) findViewById(R.id.pbLogin);
        pbLogin.setVisibility(View.GONE);

        email = (EditText) findViewById(R.id.editEmail);
        password = (EditText) findViewById(R.id.editPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        mQueue = Volley.newRequestQueue(this);
        sharedPrefManager = new SharedPrefManager(this);

        if(sharedPrefManager.getSPSudahLogin()){
            startActivity(new Intent(LoginActivity.this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        }
    }

    //Fungsi untuk mengecek apakah email nya valid
    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    //Fungsi untuk MD5
    public String md5(String s) throws NoSuchAlgorithmException {
        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
        digest.update(s.getBytes());
        byte messageDigest[] = digest.digest();

        // Create Hex String
        StringBuffer hexString = new StringBuffer();
        for (int i=0; i<messageDigest.length; i++)
            hexString.append(Integer.toHexString(0xFF & messageDigest[i]));

        return hexString.toString();
    }

    //Fungsi untuk klik button submit login
    public void loginProcess(View v) throws NoSuchAlgorithmException {
        String getEmail = email.getText().toString();
        String getPassword = password.getText().toString();

        if(getEmail.equals("") || getPassword.equals("")){
            Toast.makeText(this, "Anda belum mengisi semua field.", Toast.LENGTH_SHORT).show();
        }
        else if(!isEmailValid(getEmail)){
            Toast.makeText(this, "Format email tidak valid", Toast.LENGTH_SHORT).show();
        }
        else{
            String pass_md5 = md5(getPassword);
            requestLogin(getEmail, pass_md5);
        }
    }

    //Fungsi untuk pindah dari LoginActivity ke RegisterActivity
    public void pindahKeRegister(View v){
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        finish();
    }

    //Fungsi untuk request ke server buat login
    private void requestLogin(String email, String password){
        //Buat json request
        Map<String, String> params = new HashMap();
        params.put("email", email);
        params.put("password", password);

        JSONObject json_request = new JSONObject(params);

//        pbLogin.setVisibility(View.VISIBLE);
//        btnLogin.setEnabled(false);

        customProgress.showProgress(this, "Mohon menunggu...", false);

        //request pake volley
        String base_url = getString(R.string.base_url);
        String url =  base_url + "/api/session";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, json_request,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            VolleyLog.wtf(response.toString());

                            int response_code = response.getInt("response_code");

                            String id_user_login = "";
                            //kalo request nya berhasil berarti code = 200
                            if(response_code == 0){
                                // ambil session
                                JSONArray jsonArray = response.getJSONArray("session");
                                //ambil anggota di dalam session
                                JSONObject session = jsonArray.getJSONObject(0);
                                //ambil id_user dari response JSON
                                id_user_login = session.getString("id_user");

                                VolleyLog.wtf(session.toString());
                                VolleyLog.wtf(String.valueOf(response_code));

                                //masukin id_user_login ke dalem SharedPreference
                                sharedPrefManager.saveSPString(SharedPrefManager.SP_ID_USER_LOGIN, id_user_login);

                                //set bahwa sudah berhasil login
                                sharedPrefManager.saveSPBoolean(SharedPrefManager.SP_SUDAH_LOGIN, true);

                                VolleyLog.wtf(id_user_login);
                                VolleyLog.wtf("masuk 1");

                                Toast.makeText(getApplicationContext(), "Anda berhasil masuk", Toast.LENGTH_SHORT).show();

                                //hide progress bar
                                customProgress.hideProgress();

                                //pindah ke MainActivity
                                startActivity(new Intent(LoginActivity.this, MainActivity.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));

                                //supaya ga bisa balik ke LoginActivity lagi
                                finish();
                            }
                            else if(response_code == -1401){
                                //hide progress bar
                                customProgress.hideProgress();

                                Toast.makeText(getApplicationContext(), "Akun tidak terdaftar.", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                VolleyLog.wtf("masuk 2");
                                //hide progress bar
                                customProgress.hideProgress();

                                Toast.makeText(getApplicationContext(), "Anda gagal masuk.", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            //hide progress bar
                            customProgress.hideProgress();
                            Toast.makeText(getApplicationContext(), "Anda gagal masuk. Silahkan coba masuk kembali", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //hide progress bar
//                        pbLogin.setVisibility(View.GONE);
                        customProgress.hideProgress();
                        Toast.makeText(LoginActivity.this, "Anda gagal masuk. Silahkan coba masuk kembali", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
                });

        mQueue.add(request);
    }

}
