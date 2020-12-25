package com.example.myapplication;

public class PendaftaranSuara {
    private String angka;
    private String proses = "Belum rekam";

    public PendaftaranSuara(String angka){
        this.angka = angka;
    }

    public String getAngka() {
        return angka;
    }

    public void setAngka(String angka) {
        this.angka = angka;
    }

    public String getProses() {
        return proses;
    }

    public void setProses(String proses) {
        this.proses = proses;
    }
}
