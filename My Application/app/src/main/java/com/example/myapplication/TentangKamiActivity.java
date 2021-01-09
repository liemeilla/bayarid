package com.example.myapplication;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class TentangKamiActivity extends AppCompatActivity {

    private static final int JUSTIFICATION_MODE_INTER_WORD = 1000;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tentang_kami);

        //ini untuk pake tombol back di action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        TextView txtDeskripsi = (TextView) findViewById(R.id.txtKeteranganPembuat);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            txtDeskripsi.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
        }
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
