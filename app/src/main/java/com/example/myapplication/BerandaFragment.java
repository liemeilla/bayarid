package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class BerandaFragment extends Fragment {
    ImageView imgBeliPulsa, imgTentangKami;
    TextView txtJumlahSaldo, txtWelcome;

    private RequestQueue mQueue;

    //data user
    private int saldo_user = 0;
    private String nama_user = "";


    SharedPrefManager sharedPrefManager;

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_beranda, container, false);

        mQueue = Volley.newRequestQueue(getContext());
        sharedPrefManager = new SharedPrefManager(getContext());

        // Setup any handles to view objects here
        imgBeliPulsa = (ImageView) rootView.findViewById(R.id.imgBeliPulsa);
        imgTentangKami = (ImageView) rootView.findViewById(R.id.imgTentangKami);

        txtJumlahSaldo = (TextView) rootView.findViewById(R.id.txtJumlahSaldo);
        txtWelcome = (TextView) rootView.findViewById(R.id.textWelcome);

        //setOnClickListener pada imgBeliPulsa agar dapat pindah ke PembelianPulsaActivity
        imgBeliPulsa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle dataBundle = new Bundle();
                dataBundle.putInt("saldo_user", saldo_user);
                Intent intent = new Intent(getActivity(), PembelianPulsaActivity.class);
                intent.putExtras(dataBundle);

                startActivity(intent);
            }
        });

        //setOnClickListener pada imgTentangKami agar dapat pindah ke TentangKamiActivity
        imgTentangKami.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), TentangKamiActivity.class));
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
        String base_url = getString(R.string.base_url);

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
                                saldo_user = users.getInt("saldo_user");
                                nama_user = users.getString("nama_user");

                                String saldo_rupiah = formatRupiah(saldo_user);
                                txtJumlahSaldo.setText(saldo_rupiah);
                                txtWelcome.setText("Selamat Datang, " + nama_user);

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

    public String formatRupiah(int saldo){
        DecimalFormat kursIndonesia = (DecimalFormat) DecimalFormat.getCurrencyInstance();
        DecimalFormatSymbols formatRp = new DecimalFormatSymbols();

        formatRp.setCurrencySymbol("Rp. ");
        formatRp.setMonetaryDecimalSeparator(',');
        formatRp.setGroupingSeparator('.');

        kursIndonesia.setDecimalFormatSymbols(formatRp);
        String x = kursIndonesia.format(saldo);

        return x;
    }
}
