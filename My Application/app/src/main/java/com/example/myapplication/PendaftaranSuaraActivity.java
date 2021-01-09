package com.example.myapplication;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Multipart;

public class PendaftaranSuaraActivity extends AppCompatActivity {
    ListView lstDaftarSuara;
    Button btnDaftarSuara;

    ArrayList<PendaftaranSuara> arrayListPenSuara;
    CustomAdapterRekam customAdapterRekam;
    ArrayList<String> sembilan_rekaman_suara;
    CustomProgress customProgress;
    final int REQUEST_PERMISSION_CODE = 1000;

    RequestQueue mQueue;

    private long mLastClickTime = 0;

    String id_user_daftar, nama_user_daftar, email_user_daftar, no_hp_user_daftar, password_user_daftar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pendaftaran_suara);

        customProgress = CustomProgress.getInstance();

        mQueue = Volley.newRequestQueue(this);

        //ambil bundle dari intent register activity
        Bundle dataBundle = getIntent().getExtras();
        if(dataBundle != null){
            id_user_daftar = dataBundle.getString("id_user_daftar");
            nama_user_daftar = dataBundle.getString("nama_user_daftar");
            email_user_daftar = dataBundle.getString("email_user_daftar");
            no_hp_user_daftar = dataBundle.getString("no_hp_user_daftar");
            password_user_daftar = dataBundle.getString("password_user_daftar");
        }

        arrayListPenSuara = new ArrayList<PendaftaranSuara>();

        lstDaftarSuara = (ListView) findViewById(R.id.lstDaftarSuara);
        btnDaftarSuara = (Button) findViewById(R.id.btnDaftarSuara);

        //pada saat Runtime minta permission untuk rekam suara dulu
        if(checkPermissionFromDevice()){
            //Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show();
        }else{
            requestPermissions();
        }

        //set custom adapter untuk merekam 9 suara
        setPendaftaranSuara();

        //ambil arraylist yang isinya 9 rekaman untuk diupload ke server
        sembilan_rekaman_suara = customAdapterRekam.getKumpulanRekamanDaftarSuara();

        //onclicklistener untuk upload
        btnDaftarSuara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(sembilan_rekaman_suara.size() < 9){
                    Toast.makeText(PendaftaranSuaraActivity.this, "Anda belum merekam seluruh digit angka", Toast.LENGTH_SHORT).show();
                }else{
                    Log.e("daftar suara", sembilan_rekaman_suara.toString());

                    int jum_upload = 0;
                    //upload 9 audio ke server
                    try{
                        for(int i=0; i<9; i++){
                            uploadToServer(sembilan_rekaman_suara.get(i));
                            jum_upload++;
                        }
                    }catch (Exception e){
                        Toast.makeText(PendaftaranSuaraActivity.this, "Upload error", Toast.LENGTH_SHORT).show();
                    }

                    //update status pendaftara suara di tabel user
                    if(jum_upload == 9) {
                        //delay untuk 10 detik
                        final int finalJum_upload = jum_upload;

                        //show custom progress bar
                        customProgress.showProgress(PendaftaranSuaraActivity.this, "Mohon menunggu...", false);

                        new CountDownTimer(12000, 1000) {
                            public void onTick(long millisUntilFinished) { }

                            public void onFinish() {
                                //setelah 10 detik baru request update
                                requestUpdateStatusPendaftaranSuara();
                            }
                        }.start();
                    }
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Anda belum menyelesaikan langkah pendaftaran suara. " +
                "Jika Anda keluar aplikasi, maka data pendaftaran akun hilang. Anda ingin keluar?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user pressed "yes", then he is allowed to exit from application
                requestDeleteAkun();
            }
        });
        builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user select "No", just cancel this dialog and continue with app
                dialog.cancel();
            }
        });
        AlertDialog alert=builder.create();
        alert.show();
    }

    //set custom adapter rekam di listview untuk rekam suara
    public void setPendaftaranSuara(){
        arrayListPenSuara.clear();
        for(int i=1 ; i<10; i++){
            String digit = String.valueOf(i);
            PendaftaranSuara rekam_suara = new PendaftaranSuara(digit);
            arrayListPenSuara.add(rekam_suara);
        }

        //set array list ke array adapter
        customAdapterRekam = new CustomAdapterRekam(this, arrayListPenSuara, id_user_daftar, "");

        //bind adapter ke listview
        lstDaftarSuara.setAdapter(customAdapterRekam);
    }

    //request update status pendaftaran suara
    public void requestUpdateStatusPendaftaranSuara(){
        String base_url = getString(R.string.base_url);
        String url =  base_url + "/api/users/" + id_user_daftar+ "/active";

        Map<String , String> params = new HashMap<>();
        params.put("status_pendaftaran_suara", "1");

        JSONObject json_request = new JSONObject(params);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PATCH, url, json_request,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // ambil array users
                        try {
                            int response_code = response.getInt("response_code");

                            VolleyLog.wtf(response.toString());

                            //hide custom progress bar
                            customProgress.hideProgress();

                            if(response_code == 0){
                                Toast.makeText(PendaftaranSuaraActivity.this, "Berhasil mendaftar akun. Silahkan masuk ke akun Anda.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(PendaftaranSuaraActivity.this, LoginActivity.class));
                                finish();
                            }else if(response_code == -1544){
                                Toast.makeText(PendaftaranSuaraActivity.this, "Terjadi kesalahan. Silahkan tekan tombol Daftar Suara", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(PendaftaranSuaraActivity.this, "Gagal mendaftar akun. Silahkan coba kembali", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(PendaftaranSuaraActivity.this, "Gagal mendaftar akun. Silahkan coba kembali", Toast.LENGTH_SHORT).show();
                    }
                });
        mQueue.add(request);
    }

    private void requestDeleteAkun() {
        String base_url = getString(R.string.base_url);
        String url =  base_url + "/api/users/" + id_user_daftar;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int response_code = response.getInt("response_code");

                            VolleyLog.wtf(response.toString());

                            if(response_code == 0){
                                finish();
                            }else{
                                Toast.makeText(PendaftaranSuaraActivity.this, "Terjadi kesalahan. Silahkan coba keluar kembali", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PendaftaranSuaraActivity.this, "Terjadi kesalahan. Silahkan coba keluar kembali", Toast.LENGTH_SHORT).show();
                    }
                });
        mQueue.add(request);
    }

    //upload audio using retrofit
    private void uploadToServer(String filepath){
        //create a file object using filepath
        File file = new File(filepath);

        //create a request body with file and audio type
        RequestBody requestBody = RequestBody.create(MediaType.parse("audio/*"), file);

        //create MultipartBody.Part using file request-body, file name and part name
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", file.getName(), requestBody);

        //create request body for filename
        RequestBody filename = RequestBody.create(MediaType.parse("text/plain"), file.getName());

        Retrofit retrofit = NetworkClient.getRetrofitClient(this);
        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);

        //call
        Call call = uploadAPIs.uploadAudio(fileToUpload, filename);

        call.enqueue(new Callback<ServerResponseForRetrofit>() {
            @Override
            public void onResponse(Call<ServerResponseForRetrofit> call, Response<ServerResponseForRetrofit> response) {
                ServerResponseForRetrofit serverResponse = ((ServerResponseForRetrofit) response.body());

                if(serverResponse != null){
                    //dapetin response code
                    int response_code = serverResponse.getResponseCode();
                    String response_msg = serverResponse.getResponseMsg();
                    if(response_code == 0){
                        //Toast.makeText(PendaftaranSuaraActivity.this, response_msg, Toast.LENGTH_SHORT).show();
                    }else{
                        //Toast.makeText(PendaftaranSuaraActivity.this, response_msg, Toast.LENGTH_SHORT).show();
                    }
                }else {
                    assert serverResponse != null;
                    Log.v("Response", serverResponse.toString());
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {

            }
        });
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO
        }, REQUEST_PERMISSION_CODE);
    }

    //press ctrl o
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_PERMISSION_CODE:{
                if(grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
            }

            break;
        }
    }

    private boolean checkPermissionFromDevice() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);

        return write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                record_audio_result == PackageManager.PERMISSION_GRANTED;
    }
}
