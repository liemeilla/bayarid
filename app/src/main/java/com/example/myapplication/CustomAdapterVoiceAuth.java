package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapterVoiceAuth extends BaseAdapter {

    private Context mContext;
    LayoutInflater inflater;
    private List<PendaftaranSuara> listKodeAngka; //tampungan pas mau set
    public ArrayList<PendaftaranSuara> arrayListKodeAngka;
    public ArrayList<String> kumpulan_rekaman;
    String id_user, id_transaksi;
    WavRecorder wavRecorder;
    int jum_rekaman;

    public CustomAdapterVoiceAuth(Context context, List listKodeAngka, String id_user_daftar, String id_transaksi){
        this.mContext = context;
        inflater = LayoutInflater.from(context);
        this.listKodeAngka = listKodeAngka;
        this.arrayListKodeAngka = new ArrayList<PendaftaranSuara>();
        this.id_user = id_user_daftar;
        this.id_transaksi = id_transaksi;
        kumpulan_rekaman = new ArrayList<String>();

    }

    public class ViewHolder{
        TextView txtKodeAngka;
        TextView txtTandaRekam;
    }

    @Override
    public int getCount() {
        return listKodeAngka.size();
    }

    @Override
    public Object getItem(int position) {
        return listKodeAngka.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if(view == null){
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.gridview_voiceauth, null);

            //locate the elements
            holder.txtKodeAngka = (TextView) view.findViewById(R.id.txtKodeAngka);
            holder.txtTandaRekam = (TextView) view.findViewById(R.id.txtTandaRekam);

            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }

        //set the result
        holder.txtKodeAngka.setText(listKodeAngka.get(position).getAngka());

        return view;
    }
}
