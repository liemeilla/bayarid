package com.example.myapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends BaseAdapter {
    Context mContext;
    LayoutInflater inflater;
    List<Transaksi> modelList;
    ArrayList<Transaksi> arrayList;

    public CustomAdapter(Context context, List<Transaksi> modelList){
        mContext = context;
        this.modelList = modelList;
        inflater = LayoutInflater.from(context);
        this.arrayList = new ArrayList<Transaksi>();
        this.arrayList.addAll(modelList);
    }

    public class ViewHolder{
        TextView txtPembelianPulsa;
        TextView txtIdTransaksi;
        TextView txtProviderYangDibeli;
        TextView txtPulsaYangDibeli;
        TextView txtStatusPembelian;
        TextView txtTanggalPembelian;
        TextView txtNoHpBeli;
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

    @NonNull
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if(view == null){
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.listview_transaksi, null);

            //locate the view in listview_transaksi.xml
            holder.txtPembelianPulsa = view.findViewById(R.id.txtPembelianPulsa);
            holder.txtIdTransaksi = view.findViewById(R.id.txtIdTransaksi);
            holder.txtProviderYangDibeli = view.findViewById(R.id.txtProviderYangDibeli);
            holder.txtPulsaYangDibeli = view.findViewById(R.id.txtPulsaYangDibeli);
            holder.txtStatusPembelian = view.findViewById(R.id.txtStatusPembelian);
            holder.txtTanggalPembelian = view.findViewById(R.id.txtTanggalPembelian);
            holder.txtNoHpBeli = view.findViewById(R.id.txtNoHpBeli);

            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }

        //set the result into text view
        holder.txtPembelianPulsa.setText(modelList.get(position).getPembelianPulsa());
        holder.txtIdTransaksi.setText(modelList.get(position).getIdTransaksi());
        holder.txtProviderYangDibeli.setText(modelList.get(position).getProviderYangDibeli());
        holder.txtPulsaYangDibeli.setText(modelList.get(position).getPulsaYangDibeli());
        holder.txtStatusPembelian.setText(modelList.get(position).getStatusPembelian());
        holder.txtTanggalPembelian.setText(modelList.get(position).getTanggalPembelian());
        holder.txtNoHpBeli.setText(modelList.get(position).getNoHpBeli());

        return view;
    }
}
