package com.example.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.ConditionVariable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class VoiceAuthenticationActivity extends AppCompatActivity {
    GridView lstVoiceAuth;
    Button btnVoiceAuth;
    Button btnRekam, btnHapus;

    ArrayList<PendaftaranSuara> arrayListSuara;
    ArrayList<String> lima_rekaman_suara;
    CustomProgress customProgress;

    String id_transaksi, id_user_login;

    RequestQueue mQueue;
    final int REQUEST_PERMISSION_CODE = 1000;

    int attempt;
    int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_authentication);

        attempt = 0;
        i = 0;
        lima_rekaman_suara = new ArrayList<>();
        btnVoiceAuth = (Button) findViewById(R.id.btnVoiceAuth);
        btnRekam = (Button) findViewById(R.id.btnRekamVoiceAuth);
        btnHapus = (Button) findViewById(R.id.btnHapusVoiceAuth);

        customProgress = CustomProgress.getInstance();

        arrayListSuara = new ArrayList<PendaftaranSuara>();

        //arrayListSuara = new ArrayList<PendaftaranSuara>();
        mQueue = Volley.newRequestQueue(this);

        //ambil bunlde dari intet
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            id_transaksi = bundle.getString("id_transaksi");
            id_user_login = bundle.getString("id_user");
        }

        //pada saat Runtime minta permission untuk rekam suara dulu
        if(!checkPermissionFromDevice()){
            requestPermissions();
        }

        //set gridview kode angka
        setVoiceAuthGridView();

        btnVoiceAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voiceAthentication();
            }
        });

        btnRekam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lima_rekaman_suara.isEmpty()){
                    rekamSuara(0);
                }else{
                    Toast.makeText(VoiceAuthenticationActivity.this, "Anda sudah merekam.", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnHapus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hapusRekaman();
            }
        });
    }

    public void voiceAthentication(){

        if(attempt > 2){
            Toast.makeText(VoiceAuthenticationActivity.this, "Transaksi gagal", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(VoiceAuthenticationActivity.this, MainActivity.class));
            finish();
        }else{
            //cek dulu udah rekam apa blom
            if(lima_rekaman_suara.isEmpty()){
                Toast.makeText(this, "Anda belum merekam.", Toast.LENGTH_SHORT).show();
            }else{
                //coba attempt ke server
                attempt += 1;
                //upload 5 audio ke server
                try{
                    for(int i=0; i<5; i++){
                        uploadToServer(lima_rekaman_suara.get(i), String.valueOf(attempt));
                    }
                }catch (Exception e){
                    Toast.makeText(VoiceAuthenticationActivity.this, "Upload error", Toast.LENGTH_SHORT).show();
                }

                //show custom progress bar
                customProgress.showProgress(VoiceAuthenticationActivity.this, "Mohon menunggu...", false);

                //lakukan autentikasi dan update status transaksi di tabel transaksi
                new CountDownTimer(10000, 1000) {
                    public void onTick(long millisUntilFinished) { }
                    public void onFinish() {
                        //setelah 10 detik baru request auth
                        requestVoiceAuthentication();
                    }
                }.start();
            }
        }
    }

    public void rekamSuara(final int i){
        final RecordProgress recordProgress = RecordProgress.getInstance();
        //ambil angka paling pertama dulu
        PendaftaranSuara ps = (PendaftaranSuara) lstVoiceAuth.getAdapter().getItem(i);
        final String angka = ps.getAngka();
        final String nama_rekaman = id_transaksi + "_" + id_user_login + "_" + angka + ".wav";

        //mau ambil textview untuk menandakan sudah selesai merekam
        View viewItem = lstVoiceAuth.getChildAt(i);
        final TextView txtProses = (TextView) viewItem.findViewById(R.id.txtTandaRekam);
        final TextView txtKodeAngka = (TextView) viewItem.findViewById(R.id.txtKodeAngka);

        //munculin dulu progress dialognya
        recordProgress.showProgress(this, true);
        recordProgress.changeAngka(angka);

        //bikin objek wavrecorder
        final WavRecorder wavRecorder = new WavRecorder(nama_rekaman);
        //start recording
        wavRecorder.startRecording();

        new CountDownTimer(2000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                recordProgress.changeMessage("Detik tersisa: " + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                wavRecorder.stopRecording();
                String path_file_rekaman = wavRecorder.getFilename();
                lima_rekaman_suara.add(path_file_rekaman);

                txtProses.setText("Sudah");
                txtKodeAngka.setBackgroundResource(R.color.biru_sudah);
                recordProgress.hideProgress();

                //kalo masih kurang dari 4 lanjut rekam suara
                if(i < 4){
                    rekamSuara(i + 1);
                }
            }
        }.start();

    }

    public void hapusRekaman(){
        if(lima_rekaman_suara.isEmpty()){
            Toast.makeText(this, "Rekaman suara tidak ada.", Toast.LENGTH_SHORT).show();
        }else{
            //loop 5x untuk delete 5 rekaman

            for(int i=0; i<5; i++){
                //buat file delete
                File fdelete = new File(lima_rekaman_suara.get(i));
                fdelete.delete();
                //reset kalimat di tandarekam menjadi 'Belum'
                View viewItem = lstVoiceAuth.getChildAt(i);
                TextView txtProses = (TextView) viewItem.findViewById(R.id.txtTandaRekam);
                TextView txtKodeAngka = (TextView) viewItem.findViewById(R.id.txtKodeAngka);
                txtProses.setText("Belum");
                txtKodeAngka.setBackgroundResource(R.color.abu_belum);
            }

            lima_rekaman_suara.clear();
        }
    }

    //set random number untuk voice authentication
    public void setVoiceAuthGridView(){
        lstVoiceAuth = (GridView) findViewById(R.id.lstVoiceAuth);
        arrayListSuara.clear();
        lima_rekaman_suara.clear();

        //random 5 angka
        int max = 9;
        int min = 1;
        Random random = new Random();
        HashSet<Integer> set_angka=new HashSet();

        for(int i=0; i<5; i++){
            // cek dulu angkanya ud pernah ada atau blom
            int randomNumber;
            while(true){
                randomNumber = random.nextInt(max + 1 - min) + min; // random angka dari 1 sampai 9

                //kalo blom ada, langsung tambahin ke hashset terus keluar dari loop
                if(!set_angka.contains(randomNumber)){
                    set_angka.add(randomNumber);
                    break;
                }
            }

            String angka = String.valueOf(randomNumber);
            PendaftaranSuara pendaftaranSuara = new PendaftaranSuara(angka);
            arrayListSuara.add(pendaftaranSuara);
        }

        //bikin adapter
        CustomAdapterVoiceAuth customAdapterVoiceAuth =
                new CustomAdapterVoiceAuth(this, arrayListSuara, id_user_login, id_transaksi);
        //bind adapter ke gridview
        lstVoiceAuth.setAdapter(customAdapterVoiceAuth);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Anda belum menyelesaikan langkah Voice Authentication. " +
                "Jika Anda keluar, maka transaksi pembelian pulsa gagal. Anda ingin keluar?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user pressed "yes", then he is allowed to exit from application
                finish();
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

    //upload ke server
    private void uploadToServer(String filepath, String attempt){
        //create a file object using filepath
        File file = new File(filepath);

        //create a request body with file and audio type
        RequestBody requestBody = RequestBody.create(MediaType.parse("audio/*"), file);

        //create MultipartBody.Part using file request-body, file name and part name
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", file.getName(), requestBody);

        //create request body for attempt
        RequestBody filename = RequestBody.create(MediaType.parse("text/plain"), attempt);

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

    //request voice authentication
    public void requestVoiceAuthentication(){
        String base_url = getString(R.string.base_url);
        String url =  base_url + "/api/authentication";

        //bikin object voices yang isinya json array voices
        JSONObject voices_obj = new JSONObject();
        JSONArray voices = new JSONArray();

        //bikin object voice dalem array voices
        for(int i=0; i<lima_rekaman_suara.size(); i++){
            String[] path = lima_rekaman_suara.get(i).split("/");
            String[] raw = path[5].split("_");
            String nama_rekaman = path[5];
            String id_user = raw[1];
            String angka_wav = raw[2].substring(0,1);

            JSONObject voice = new JSONObject();
            try {
                voice.put("nama_rekaman", nama_rekaman);
                voice.put("kode_angka", angka_wav);
                voice.put("id_user", id_user);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //tambahin ke dalam json array voices
            voices.put(voice);
        }

        //tambahin array voices ke dalam object voices
        try {
            voices_obj.put("attempt", String.valueOf(attempt));
            voices_obj.put("id_transaksi", id_transaksi);
            voices_obj.put("id_user", id_user_login);
            voices_obj.put("voices", voices);
            //Toast.makeText(this, voices_obj.toString(), Toast.LENGTH_SHORT).show();
            Log.e("JSON request auth", voices_obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, voices_obj,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        customProgress.hideProgress();
                        try {
                            int response_code = response.getInt("response_code");

                            VolleyLog.wtf(response.toString());

                            //kalo gagal
                            if(response_code != 20){
                                //kalo gagal upload yakni 5 rekaman tidak semuanya terupload dengan benar
                                if(response_code == -1545){
                                    Toast.makeText(VoiceAuthenticationActivity.this, "Gagal menggunduh. Silahkan tekan tombol autentikasi kembali.", Toast.LENGTH_LONG).show();
                                    attempt -= 1;
                                }else{
                                    //cek attempt
                                    if(attempt == 2){
                                        //hapus seluruh rekaman
                                        hapusRekaman();

                                        Toast.makeText(VoiceAuthenticationActivity.this, "Transaksi gagal. Silahkan lakukan pembelian pulsa kembali.", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(VoiceAuthenticationActivity.this, MainActivity.class));
                                        finish();
                                    }else{ // kalo belom > 2, coba rekam kembali
                                        //hapus seluruh rekaman
                                        hapusRekaman();

                                        Toast.makeText(VoiceAuthenticationActivity.this, "Transaksi gagal.  Silahkan coba rekam kembali.", Toast.LENGTH_LONG).show();
                                        setVoiceAuthGridView();
                                    }
                                }
                            }
                            else{
                                //kalo berhasil

                                //hapus seluruh rekaman
                                hapusRekaman();

                                Toast.makeText(VoiceAuthenticationActivity.this, "Transaksi Berhasil.", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(VoiceAuthenticationActivity.this, MainActivity.class));
                                finish();
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
                        Toast.makeText(VoiceAuthenticationActivity.this, "Terjadi kesalahan. Silahkan tekan tombol autentikasi kembali.", Toast.LENGTH_SHORT).show();
                        attempt -= 1;
                    }
                });
        //untuk mencegah dua kali request
        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mQueue.add(request);
    }

    //---------------BUAT CHECK PERMISSION----------------------------------------------------
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO
        }, REQUEST_PERMISSION_CODE);
    }

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
