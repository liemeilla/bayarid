package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity{
    BerandaFragment berandaFragment;
    boolean doubleTapExitOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        //setiap mulai aplikasi langsung buka fragment beranda
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new BerandaFragment()).commit();

        berandaFragment = (BerandaFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_beranda);

    }

    @Override
    public void onBackPressed() {
        if(doubleTapExitOnce){
            super.onBackPressed();
            return;
        }

        this.doubleTapExitOnce = true;
        Toast.makeText(this, "Tekan sekali lagi untuk keluar.", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleTapExitOnce=false;
                finish();
            }
        }, 5000);
    }

    //pas activity lagi berjalan
    @Override
    protected void onResume() {
        super.onResume();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener(){
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment = null;

                    switch (menuItem.getItemId()){
                        case R.id.navigation_beranda:
                            selectedFragment= new BerandaFragment();
                            break;
                        case R.id.navigation_transaksi:
                            selectedFragment= new TransaksiFragment();
                            break;
                        case R.id.navigation_profil:
                            selectedFragment= new ProfilFragment();
                            break;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();

                    return true;
                }
            };
}
