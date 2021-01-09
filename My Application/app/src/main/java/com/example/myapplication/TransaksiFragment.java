package com.example.myapplication;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
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

public class TransaksiFragment extends Fragment {

    ListView lstTransaksi;
    ArrayList<Transaksi> arrayList; // array untuk menyimpan seluruh data transaksi
    CustomAdapter customAdapter;

    String base_url;
    SharedPrefManager sharedPrefManager;
    RequestQueue mQueue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_transaksi, container, false);

        base_url = getString(R.string.base_url);
        sharedPrefManager = new SharedPrefManager(getContext());
        mQueue = Volley.newRequestQueue(getContext());
        arrayList = new ArrayList<Transaksi>();

        lstTransaksi = (ListView) rootView.findViewById(R.id.lstTransaksi);

        requestSeluruhTransaksi();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        requestSeluruhTransaksi();
    }

    public void requestSeluruhTransaksi(){
        //Buat request JSON
        String id_user_login = sharedPrefManager.getSPIdUserLogin();
        String url =  base_url + "/api/transactions/" + id_user_login;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            arrayList.clear();

                            //ambil response code
                            int response_code = response.getInt("response_code");
                            String response_msg = response.getString("response_msg");
                            if(response_code == 0){
                                //ambil array transactions
                                JSONArray jsonArray = response.getJSONArray("transactions");
                                for(int i=0; i<jsonArray.length(); i++){
                                    JSONObject transaction = jsonArray.getJSONObject(i);

                                    String id_transaksi = transaction.getString("id_transaksi");
                                    String nama_provider = transaction.getString("nama_provider");
                                    String nominal_pulsa = String.valueOf(transaction.getInt("nominal_pulsa"));
                                    int kode_status_transaksi = transaction.getInt("status_transaksi");
                                    String status_transaksi = "";
                                    String no_hp = transaction.getString("no_hp");

                                    if(kode_status_transaksi == 1){
                                        status_transaksi = "Sukses";
                                    }else{
                                        status_transaksi = "Gagal";
                                    }
                                    String waktu_transaksi = transaction.getString("waktu_transaksi");

                                    Log.d("id_transaksi", id_transaksi);

                                    Transaksi transaksi = new Transaksi(id_transaksi, nama_provider,
                                            nominal_pulsa, status_transaksi, waktu_transaksi, no_hp);
                                    arrayList.add(transaksi);
                                }

                                //set arrayList ke custom adapter
                                customAdapter = new CustomAdapter(getContext(), arrayList);

                                //bind ke lstTransaksi
                                lstTransaksi.setAdapter(customAdapter);
                            }else{
                                Toast.makeText(getContext(), response_msg, Toast.LENGTH_SHORT).show();
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
}
