package com.example.myapplication;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomAdapterRekam extends BaseAdapter {
    Context mContext;
    LayoutInflater inflater;
    List<PendaftaranSuara> modelList;
    ArrayList<PendaftaranSuara> arrayList;
    WavRecorder wavRecorder;
    String id_user_daftar, id_transaksi;
    ArrayList<String> kumpulan_rekaman_user;
    private long mLastClickTime = 0;
    int jum_rekaman;
    RecordProgress recordProgress;


    public CustomAdapterRekam(Context context, List<PendaftaranSuara> modelList, String id_user_daftar, String id_transaksi){
        mContext = context;
        inflater = LayoutInflater.from(context);
        this.modelList = modelList;
        this.arrayList = new ArrayList<PendaftaranSuara>();
        this.id_user_daftar = id_user_daftar;
        this.id_transaksi = id_transaksi;
        kumpulan_rekaman_user = new ArrayList<String>();

        recordProgress = RecordProgress.getInstance();
    }

    public class ViewHolder{
        TextView txtAngka;
        TextView txtProses;
        Button btnRekam;
        Button btnUlang;
    }

    @Override
    public int getCount() {
        return modelList.size();
    }

    @Override
    public Object getItem(int position) {
        return modelList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if(view == null){
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.listview_daftarsuara, null);

            //locate the listview_daftarsuara.xml
            holder.txtAngka = view.findViewById(R.id.txtAngka);
            holder.txtProses = view.findViewById(R.id.txtProses);
            holder.btnRekam = view.findViewById(R.id.btnRekam);
            holder.btnUlang= view.findViewById(R.id.btnUlang);

            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }

        //set the result
        holder.txtAngka.setText(modelList.get(position).getAngka());

        //set button rekam onclick listener
        holder.btnRekam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // mis-clicking prevention, using threshold of 1000 ms

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                String digit_angka = holder.txtAngka.getText().toString();
                final String nama_audio;

                //cek kalo txtProses ga kosong ga boleh ngerekam
                if(holder.txtProses.getText().toString().equals("Belum merekam.")){
                    //cek dulu id_transaksi kosong ga
                    if(id_transaksi.equals("")){
                        nama_audio = id_user_daftar +"_"+ digit_angka +".wav";
                    }else{
                        nama_audio = id_transaksi + "_" + id_user_daftar +"_"+ digit_angka +".wav";
                    }

                    wavRecorder = new WavRecorder(nama_audio);
                    wavRecorder.startRecording();
                    recordProgress.showProgress(mContext, false);
                    recordProgress.changeAngka(digit_angka);

                    //rekam untuk 2 detik
                    new CountDownTimer(2000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            holder.txtProses.setText("Detik tersisa: " + millisUntilFinished / 1000);
                            recordProgress.changeMessage("Detik tersisa: " + millisUntilFinished / 1000);
                        }

                        public void onFinish() {
                            wavRecorder.stopRecording();
                            String path_file_rekaman = wavRecorder.getFilename();
                            String nama_file = wavRecorder.getAudioFilename();
                            kumpulan_rekaman_user.add(path_file_rekaman); // tambahkan path file audio ke arraylist
                            holder.txtProses.setText(nama_file);

                            jum_rekaman += 1;

                            Toast.makeText(mContext, "Merekam selesai.", Toast.LENGTH_SHORT).show();

                            recordProgress.hideProgress();
                        }
                    }.start();
                }else{
                    Toast.makeText(mContext, "Anda sudah merekam.", Toast.LENGTH_SHORT).show();
                }




            }
        });

        holder.btnUlang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path_delete = holder.txtProses.getText().toString();

                if(kumpulan_rekaman_user.isEmpty()){
                    Toast.makeText(mContext, "Rekaman suara tidak ada.", Toast.LENGTH_SHORT).show();
                }else{
                    //int position = Integer.parseInt(holder.txtAngka.getText().toString()); // ambil posisi yang rekamannya mau dibuang

                    //cek dulu panjang array list sembilan rekaman > dari position blom
//                    if(position > kumpulan_rekaman_user.size()){
//                        Toast.makeText(mContext, "Rekaman suara tidak ada.", Toast.LENGTH_SHORT).show();
//                    }else {
                        //String file_delete = kumpulan_rekaman_user.get(position); // karena yg didapetin digit angka harus dikurang 1 karena index array mulai dari 0

                        //buat file delete
                        String base_path = wavRecorder.getFolderPath();
                        File fdelete = new File(base_path + "/" + path_delete);

                        //cek dulu ada ga
                        if (fdelete.exists()) {
                            if (fdelete.delete()) {
                                kumpulan_rekaman_user.remove(base_path + "/" +path_delete);

                                Toast.makeText(mContext, "Rekaman dihapus", Toast.LENGTH_LONG).show();

                                holder.txtProses.setText("Belum merekam.");
                            } else {
                                Toast.makeText(mContext, "Rekaman suara tidak ada.", Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            Toast.makeText(mContext, "Rekaman suara tidak ada.", Toast.LENGTH_SHORT).show();
                        }
                    //}
                }
            }
        });

        return view;
    }

    public ArrayList<String> getKumpulanRekamanDaftarSuara(){
        return kumpulan_rekaman_user;
    }

}
