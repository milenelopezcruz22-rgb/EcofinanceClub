package com.ecofinance.ecofinance.dto;

public class SaldoMiembroDTO {

    private Long miembroId;
    private String nombre;
    private double pagado;
    private double cuota;
    private double saldo;

    public SaldoMiembroDTO() {
    }

    public SaldoMiembroDTO(Long miembroId, String nombre, double pagado, double cuota) {
        this.miembroId = miembroId;
        this.nombre = nombre;
        this.pagado = pagado;
        this.cuota = cuota;
        this.saldo = pagado - cuota;
    }

    public Long getMiembroId() {
        return miembroId;
    }

    public void setMiembroId(Long miembroId) {
        this.miembroId = miembroId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPagado() {
        return pagado;
    }

    public void setPagado(double pagado) {
        this.pagado = pagado;
    }

    public double getCuota() {
        return cuota;
    }

    public void setCuota(double cuota) {
        this.cuota = cuota;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }
}