package com.example.myapplication;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PembelianPulsaActivity extends AppCompatActivity {
    Spinner spinnerProvider, spinnerNominalPulsa;
    SharedPrefManager sharedPrefManager;
    EditText editNoHpBeliPulsa;
    TextView txtPrefixNoHp;
    Button btnBeliPulsa;
    List<String> arrListProvider;
    List<Integer> arrListNominalPulsa;
    RequestQueue mQueue;
    String base_url;

    private double saldo_user;

    String id_transaksi, id_user_login, nama_provider, no_hp_beli_pulsa, prefix_no_hp;
    int nominal_pulsa;

    final ArrayList<String> prefix_telkomsel = new ArrayList<String>(){
        {
            add("11"); add("12"); add("13"); add("21"); add("22");
        }

    };
    final ArrayList<String> prefix_indosat = new ArrayList<String>(){
        {
            add("14"); add("15"); add("16"); add("55"); add("56"); add("57");
        }

    };
    final ArrayList<String> prefix_3 = new ArrayList<String>(){
        {
            add("96"); add("97"); add("98"); add("99");
        }

    };
    final ArrayList<String> prefix_xl = new ArrayList<String>(){
        {
            add("77"); add("78");
        }

    };

    ArrayAdapter<String> adapterProvider;
    ArrayAdapter<Integer> adapterPulsa;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pembelian_pulsa);

        //cek data bundle
        Bundle dataBundle = getIntent().getExtras();
        if(dataBundle != null){
            saldo_user = dataBundle.getInt("saldo_user");
        }

        sharedPrefManager = new SharedPrefManager(this);

        id_user_login = sharedPrefManager.getSPIdUserLogin();

        mQueue = Volley.newRequestQueue(this);
        base_url = getString(R.string.base_url);

        //ini untuk pake tombol back di action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        arrListProvider = new ArrayList<String>();
        arrListNominalPulsa = new ArrayList<Integer>();

        spinnerProvider = (Spinner) findViewById(R.id.spinnerProvider);
        spinnerNominalPulsa = (Spinner) findViewById(R.id.spinnerNominalPulsa);
        editNoHpBeliPulsa = (EditText) findViewById(R.id.editNoHpBeliPulsa);
        txtPrefixNoHp = (TextView) findViewById(R.id.txtPrefixNoHp);
        btnBeliPulsa = (Button) findViewById(R.id.btnBeliPulsa);

        //buat supaya otomatis kepilih providernya
        TextWatcher textWatcherProvider = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //ambil dulu string no hp
                String no_hp = editNoHpBeliPulsa.getText().toString();
                //cek dlu apakah sudah 12 digit
                if(no_hp.length() >= 3){
                    String prefix = no_hp.substring(1,3);

                    if(prefix_telkomsel.contains(prefix)){
                        spinnerProvider.setSelection(adapterProvider.getPosition("Telkomsel"));
                    }else if(prefix_indosat.contains(prefix)){
                        spinnerProvider.setSelection(adapterProvider.getPosition("Indosat Ooredo"));
                    }else if(prefix_3.contains(prefix)){
                        spinnerProvider.setSelection(adapterProvider.getPosition("3 (Tri)"));
                    }else if(prefix_xl.contains(prefix)){
                        spinnerProvider.setSelection(adapterProvider.getPosition("XL Axiata"));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        editNoHpBeliPulsa.addTextChangedListener(textWatcherProvider);

        btnBeliPulsa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editNoHpBeliPulsa.getText().toString().equals("")){
                    int random = (int) (Math.random() * 1000000);
                    id_transaksi = "TRS" + random;
                    //ambil provider, no hp, dan nominal pulsa yang dipilih
                    nama_provider = spinnerProvider.getSelectedItem().toString();
                    nominal_pulsa = Integer.parseInt(spinnerNominalPulsa.getSelectedItem().toString());
                    prefix_no_hp = txtPrefixNoHp.getText().toString();
                    no_hp_beli_pulsa = prefix_no_hp + editNoHpBeliPulsa.getText().toString();

                    //cek dulu saldo user cukup ga buat beli pulsa

                    if(saldo_user < nominal_pulsa){
                        Toast.makeText(PembelianPulsaActivity.this, "Saldo Anda tidak mencukupi.", Toast.LENGTH_SHORT).show();
                    }else{
                        requestPembelianPulsa();
                    }
                }else{
                    Toast.makeText(PembelianPulsaActivity.this, "Anda belum mengisi nomor handphone.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        requestAllProduct();
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestAllProduct();
    }

    public void requestPembelianPulsa(){
        String base_url = getString(R.string.base_url);
        String url = base_url + "/api/transactions";

        //params utk register user
        Map<String, String> params = new HashMap<>();
        params.put("id_transaksi", id_transaksi);
        params.put("id_user", id_user_login);
        params.put("nama_provider", nama_provider);
        params.put("no_hp", no_hp_beli_pulsa);
        params.put("nominal_pulsa", String.valueOf(nominal_pulsa));

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int response_code = response.getInt("response_code");

                            //kalo berhasil insert ke db di server
                            if(response_code == 0){
                                Bundle dataBundle = new Bundle();
                                dataBundle.putString("id_transaksi", id_transaksi);
                                dataBundle.putString("id_user", id_user_login);

                                Intent intent = new Intent(PembelianPulsaActivity.this, VoiceAuthenticationActivity.class);
                                intent.putExtras(dataBundle);

                                startActivity(intent);
                                finish();

                            }else{
                                Toast.makeText(PembelianPulsaActivity.this, "Gagal : " + response_code, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        mQueue.add(request);
    }

    public void requestAllProduct(){
        //Buat request JSON
        String url =  base_url + "/api/products";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            arrListNominalPulsa.clear();
                            arrListProvider.clear();

                            int response_code = response.getInt("response_code");

                            if(response_code == 0){
                                //ambil array products
                                JSONArray products = response.getJSONArray("products");

                                //ambil array provider
                                JSONObject obj_providers = products.getJSONObject(0);
                                JSONArray providers = obj_providers.getJSONArray("provider");
                                for(int i=0; i<providers.length(); i++){
                                    JSONObject provider = providers.getJSONObject(i);
                                    String nama_provider = provider.getString("nama_provider");
                                    arrListProvider.add(nama_provider);
                                }

                                //ambil array pulsa
                                JSONObject obj_pulsa = products.getJSONObject(1);
                                JSONArray arr_pulsa = obj_pulsa.getJSONArray("pulsa");
                                for(int k=0; k<arr_pulsa.length(); k++){
                                    JSONObject pulsa = arr_pulsa.getJSONObject(k);
                                    int nominal_pulsa = pulsa.getInt("nominal_pulsa");
                                    arrListNominalPulsa.add(nominal_pulsa);
                                }

                                // buat array adapter untuk memasang arayylist ke dalam spinner
                                 adapterProvider = new ArrayAdapter<String>(
                                        PembelianPulsaActivity.this,
                                        android.R.layout.simple_spinner_item,
                                        arrListProvider);
                                adapterPulsa = new ArrayAdapter<Integer>(
                                        PembelianPulsaActivity.this,
                                        android.R.layout.simple_spinner_item,
                                        arrListNominalPulsa);

                                adapterProvider.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinnerProvider.setAdapter(adapterProvider);

                                adapterPulsa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinnerNominalPulsa.setAdapter(adapterPulsa);

                            }else{
                                Toast.makeText(PembelianPulsaActivity.this, "Gagal kode : " + response_code, Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

        }

        return super.onOptionsItemSelected(item);
    }
}
