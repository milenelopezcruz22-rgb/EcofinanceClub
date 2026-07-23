package com.ecofinance.ecofinance.dto;

public class DeudaDTO {

    private Long deudorId;
    private String deudorNombre;
    private Long acreedorId;
    private String acreedorNombre;
    private double monto;

    public DeudaDTO() {
    }

    public DeudaDTO(Long deudorId, String deudorNombre, Long acreedorId, String acreedorNombre, double monto) {
        this.deudorId = deudorId;
        this.deudorNombre = deudorNombre;
        this.acreedorId = acreedorId;
        this.acreedorNombre = acreedorNombre;
        this.monto = monto;
    }

    public Long getDeudorId() {
        return deudorId;
    }

    public void setDeudorId(Long deudorId) {
        this.deudorId = deudorId;
    }

    public String getDeudorNombre() {
        return deudorNombre;
    }

    public void setDeudorNombre(String deudorNombre) {
        this.deudorNombre = deudorNombre;
    }

    public Long getAcreedorId() {
        return acreedorId;
    }

    public void setAcreedorId(Long acreedorId) {
        this.acreedorId = acreedorId;
    }

    public String getAcreedorNombre() {
        return acreedorNombre;
    }

    public void setAcreedorNombre(String acreedorNombre) {
        this.acreedorNombre = acreedorNombre;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }
}