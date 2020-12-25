package com.example.myapplication;

class Transaksi {
    final private String pembelianPulsa = "Pembelian Pulsa";
    private String providerYangDibeli;
    private String pulsaYangDibeli;
    private String statusPembelian;
    private String tanggalPembelian;
    private String idTransaksi;
    private String noHpBeli;

    public Transaksi(String strIdTransaksi, String strProviderYangDibeli,
                     String strPulsaYangDibeli, String strStatusPembelian,
                     String strTanggalPembelian, String noHpBeli){
        this.idTransaksi = strIdTransaksi;
        this.providerYangDibeli = strProviderYangDibeli;
        this.pulsaYangDibeli = strPulsaYangDibeli;
        this.statusPembelian = strStatusPembelian;
        this.tanggalPembelian = strTanggalPembelian;
        this.noHpBeli= noHpBeli;
    }

    public String getNoHpBeli() {
        return noHpBeli;
    }

    public void setNoHpBeli(String noHpBeli) {
        this.noHpBeli = noHpBeli;
    }

    public String getIdTransaksi() {
        return idTransaksi;
    }

    public void setIdTransaksi(String idTransaksi) {
        this.idTransaksi = idTransaksi;
    }

    public String getPembelianPulsa() {
        return pembelianPulsa;
    }

    public String getProviderYangDibeli() {
        return providerYangDibeli;
    }

    public void setProviderYangDibeli(String providerYangDibeli) {
        this.providerYangDibeli = providerYangDibeli;
    }

    public String getPulsaYangDibeli() {
        return pulsaYangDibeli;
    }

    public void setPulsaYangDibeli(String pulsaYangDibeli) {
        this.pulsaYangDibeli = pulsaYangDibeli;
    }

    public String getStatusPembelian() {
        return statusPembelian;
    }

    public void setStatusPembelian(String statusPembelian) {
        this.statusPembelian = statusPembelian;
    }

    public String getTanggalPembelian() {
        return tanggalPembelian;
    }

    public void setTanggalPembelian(String tanggalPembelian) {
        this.tanggalPembelian = tanggalPembelian;
    }
}
