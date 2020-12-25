package com.example.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    TextView txtLinkLogin;
    EditText editNamaDaftar, editEmailDaftar, editNoHpDaftar, editKataSandi;
    Button btnRegister;

    CustomProgress customProgress;

    RequestQueue mQueue;

    String id_user_daftar;
    String isi_nama;
    String isi_email;
    String isi_no_hp;
    String isi_password;
    String pass_md5 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mQueue = Volley.newRequestQueue(this);

        customProgress = CustomProgress.getInstance();

        editNamaDaftar = findViewById(R.id.editNama);
        editEmailDaftar = findViewById(R.id.editEmail);
        editNoHpDaftar = findViewById(R.id.editNoHp);
        editKataSandi = findViewById(R.id.editPassword);

        int random = (int) (Math.random() * 1000);
        id_user_daftar = "USR" + random;

        btnRegister = (Button) findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pesan = validasiField();
                if(pesan.equals("")){
                    try {
                        pass_md5 = md5(isi_password);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }

                    //request register user
                    customProgress.showProgress(RegisterActivity.this, "Mohon menunggu...", false);

                    requestPendaftaranUser();
                }else{
                    Toast.makeText(RegisterActivity.this, pesan, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void requestPendaftaranUser() {
        String base_url = getString(R.string.base_url);
        String url = base_url + "/api/users";

        //params utk register user
        Map<String, String> params = new HashMap<String, String>();
        params.put("id_user", id_user_daftar);
        params.put("nama_user", isi_nama);
        params.put("email_user", isi_email);
        params.put("no_hp_user", isi_no_hp);
        params.put("password_user", pass_md5);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int response_code = response.getInt("response_code");

                            //kalo berhasil insert ke db di server
                            if(response_code == 0){
                                Bundle dataBundle = new Bundle();
                                dataBundle.putString("id_user_daftar", id_user_daftar);
                                dataBundle.putString("nama_user_daftar", isi_nama);
                                dataBundle.putString("email_user_daftar", isi_email);
                                dataBundle.putString("no_hp_user_daftar", isi_no_hp);
                                dataBundle.putString("password_user_daftar", pass_md5);

                                customProgress.hideProgress();

                                Intent intent = new Intent(RegisterActivity.this, PendaftaranSuaraActivity.class);
                                intent.putExtras(dataBundle);

                                startActivity(intent);
                                finish();
                            }else{
                                customProgress.hideProgress();
                                Toast.makeText(RegisterActivity.this, response.getString("response_msg"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        customProgress.hideProgress();
                        Toast.makeText(RegisterActivity.this, "Terjadi kesalahan. Silahkan ulang kembali.", Toast.LENGTH_SHORT).show();
                    }
                });
        mQueue.add(request);
    }

    public String validasiField(){
        isi_nama = editNamaDaftar.getText().toString();
        isi_email = editEmailDaftar.getText().toString();
        isi_no_hp = editNoHpDaftar.getText().toString();
        isi_password = editKataSandi.getText().toString();
        String pesan = "";

        if(isi_nama.equals("") || isi_email.equals("") || isi_no_hp.equals("") || isi_password.equals("")){
            pesan = "Anda belum mengisi semua field. ";
        }else{
            //validasi email
            if(!isEmailValid(isi_email)) pesan += "Format email tidak valid. ";
            //cek no hp lebih dari 12 ga
            else if(!isNoHpValid(isi_no_hp)) pesan += "Nomor Handphone hanya mengandung digit angka. ";
            else if(isi_no_hp.length() > 12) pesan += "Nomor Handphone maksimal 12 digit. ";
            else if(isi_password.length() < 8) pesan += "Kata sandi minimal 8 karakter";
        }

        return pesan;
    }

    public void pindahKeLogin(View view){
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }

    //Fungsi untuk mengecek apakah email nya valid
    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    //Fungsi untuk mengecek format No Hp
    private boolean isNoHpValid(String nohp){
        try {
            Double no_hp = Double.parseDouble(nohp);
        }catch (Exception e){
            return false;
        }
        return true;
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


}
