package com.example.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RecordProgress {

    public static RecordProgress recordProgress = null;
    private Dialog mDialog;
    TextView txt;
    TextView txtKodeAngka;
    TextView txtUcapkan;

    public static RecordProgress getInstance() {
        if(recordProgress == null){
            recordProgress = new RecordProgress();
        }
        return recordProgress;
    }

    public void showProgress(Context context, boolean cancelable){
        mDialog = new Dialog(context);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.recording_loading_dialog);
        //ImageView img = (ImageView) mDialog.findViewById(R.id.imgRecord);
        txtUcapkan = (TextView) mDialog.findViewById(R.id.txtUcapkan);
        txtKodeAngka = (TextView) mDialog.findViewById(R.id.imgRecord);
        txt = (TextView) mDialog.findViewById(R.id.txtSedangMerekam);
        mDialog.setCancelable(cancelable);
        mDialog.setCanceledOnTouchOutside(cancelable);
        mDialog.show();
    }

    public void hideProgress(){
        if(mDialog != null){
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public void changeJudul(String judul){
        txtUcapkan.setText(judul);
    }

    public void changeMessage(String process){
        txt.setText(process);
    }

    public void changeAngka(String angka){
        txtKodeAngka.setText(angka);
    }


}
