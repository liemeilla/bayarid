package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.HashMap;
import java.util.Map;

public class ProfilFragment extends Fragment {
    EditText editUpdateNama, editUpdateEmail, editUpdateNoHp;
    Button btnGantiPassword, btnKeluar, btnUpdateProfile;

    String base_url;
    SharedPrefManager sharedPrefManager;
    RequestQueue mQueue;

    //data user
    private String nama_user = "";
    private String email_user = "";
    private String no_hp_user = "";
    private String password_user = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_profil, container, false);

        sharedPrefManager = new SharedPrefManager(getContext());
        mQueue = Volley.newRequestQueue(getContext());

        base_url = getString(R.string.base_url);

        editUpdateNama = (EditText) rootView.findViewById(R.id.editUpdateNama);
        editUpdateEmail = (EditText) rootView.findViewById(R.id.editUpdateEmail);
        editUpdateNoHp = (EditText) rootView.findViewById(R.id.editUpdateNoHp);
        btnGantiPassword = (Button) rootView.findViewById(R.id.btnUpdatePassword);
        btnKeluar = (Button) rootView.findViewById(R.id.btnKeluar);
        btnUpdateProfile = (Button) rootView.findViewById(R.id.btnUpdateProfile);

        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestUpdateProfile();
            }
        });

        btnGantiPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), GantiPasswordActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("password_lama", password_user);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        btnKeluar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Anda keluar dari BAYAR ID", Toast.LENGTH_SHORT).show();
                sharedPrefManager.saveSPBoolean(SharedPrefManager.SP_SUDAH_LOGIN, false);
                sharedPrefManager.deleteSharedPref(sharedPrefManager.SP_ID_USER_LOGIN, sharedPrefManager.SP_SUDAH_LOGIN);

                startActivity(new Intent(getActivity(), LoginActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                getActivity().finish();
            }
        });

        requestInformasiUser();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        requestInformasiUser();
    }

    public void requestInformasiUser(){
        //Buat request JSON
        String id_user_login = sharedPrefManager.getSPIdUserLogin();
        String url =  base_url + "/api/users/" + id_user_login;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,

                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //ambil array users
                            JSONArray jsonArrayRequest = response.getJSONArray("user");

                            //ambil anggota di dalem users
                            JSONObject users = jsonArrayRequest.getJSONObject(0);
                            int response_code = response.getInt("response_code");

                            if(response_code == 0){
                                nama_user = users.getString("nama_user");
                                email_user = users.getString("email_user");
                                no_hp_user = users.getString("no_hp_user");
                                password_user = users.getString("password_user");

                                editUpdateNama.setText(nama_user);
                                editUpdateEmail.setText(email_user);
                                editUpdateNoHp.setText(no_hp_user);

                            }else{
                                Toast.makeText(getContext(), "Gagal kode : " + response_code, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        mQueue.add(request);

    }

    public void requestUpdateProfile(){

        String id_user_login = sharedPrefManager.getSPIdUserLogin();
        String url =  base_url + "/api/users/" + id_user_login + "/profile";

        Map<String ,String> params = new HashMap<>();
        params.put("nama_user", editUpdateNama.getText().toString());
        params.put("email_user", editUpdateEmail.getText().toString());
        params.put("no_hp_user", editUpdateNoHp.getText().toString());

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
                                Toast.makeText(getContext(), "Berhasil memperbaharui data profil.", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(getContext(), "Gagal memperbaharui data profil. Silahkan coba kembali", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), "Gagal memperbaharui data profil. Silahkan coba kembali", Toast.LENGTH_SHORT).show();
                    }
                });
        mQueue.add(request);
    }
}
