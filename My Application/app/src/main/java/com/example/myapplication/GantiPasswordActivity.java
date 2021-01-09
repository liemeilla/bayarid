package com.example.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class GantiPasswordActivity extends AppCompatActivity {
    EditText editPasswordLama, editPasswordBaru;
    Button btnUpdatePasswordBaru;

    String id_user_login = "";
    String password_user_lama = "";
    String base_url;

    SharedPrefManager sharedPrefManager;
    RequestQueue mQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ganti_password);

        editPasswordBaru = (EditText) findViewById(R.id.editPasswordBaru);
        editPasswordLama = (EditText) findViewById(R.id.editPasswordLama);
        btnUpdatePasswordBaru = (Button) findViewById(R.id.btnUpdatePasswordBaru);

        sharedPrefManager = new SharedPrefManager(this);
        mQueue = Volley.newRequestQueue(this);

        base_url = getString(R.string.base_url);

        id_user_login = sharedPrefManager.getSPIdUserLogin();

        Bundle bundle = getIntent().getExtras();
        password_user_lama = bundle.getString("password_lama");

        btnUpdatePasswordBaru.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestUpdatePassword();
            }
        });
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

    //request untuk update password
    public void requestUpdatePassword(){
        String id_user_login = sharedPrefManager.getSPIdUserLogin();
        String url =  base_url + "/api/users/" + id_user_login + "/password";

        try {
            String password_baru = md5(editPasswordBaru.getText().toString());
            String password_lama = md5(editPasswordLama.getText().toString());

            //kalo kosong
            if(editPasswordBaru.getText().toString().equals("") || editPasswordLama.getText().toString().equals("")){
                Toast.makeText(this, "Anda belum mengisi semua field.", Toast.LENGTH_SHORT).show();
            }else{ // kalo semua field udah keisi
                if(!password_lama.equals(password_user_lama)){
                    Toast.makeText(this, "Password lama yang dimasukkan salah.", Toast.LENGTH_SHORT).show();
                }else{
                    Map<String ,String> params = new HashMap<>();
                    params.put("password_user", password_baru);

                    JSONObject json_request = new JSONObject(params);

                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.PATCH, url, json_request,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    // ambil array users
                                    try {
                                        int response_code = response.getInt("response_code");

                                        VolleyLog.wtf(response.toString());

                                        if(response_code == 0){
                                            Toast.makeText(GantiPasswordActivity.this, "Berhasil memperbaharui kata sandi.", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(GantiPasswordActivity.this, MainActivity.class));
                                        }else{
                                            Toast.makeText(GantiPasswordActivity.this, "Gagal memperbaharui kata sandi. Silahkan coba kembali", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(GantiPasswordActivity.this, "Gagal memperbaharui kata sandi. Silahkan coba kembali", Toast.LENGTH_SHORT).show();
                                }
                            });
                    mQueue.add(request);
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }
}
