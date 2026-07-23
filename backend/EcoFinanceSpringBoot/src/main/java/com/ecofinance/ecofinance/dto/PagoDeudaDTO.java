package com.ecofinance.ecofinance.dto;

import java.time.LocalDate;

import com.ecofinance.ecofinance.entity.PagoDeuda;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PagoDeudaDTO {

    private Long id;

    @NotNull(message = "El grupo es obligatorio")
    private Long grupoId;

    @NotNull(message = "El deudor es obligatorio")
    private Long deudorId;
    private String deudorNombre;

    @NotNull(message = "El acreedor es obligatorio")
    private Long acreedorId;
    private String acreedorNombre;

    @Positive(message = "El monto debe ser mayor a 0")
    private double monto;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    private String nota;

    public PagoDeudaDTO() {
    }

    public PagoDeudaDTO(PagoDeuda pago) {
        this.id = pago.getId();
        if (pago.getGrupo() != null) {
            this.grupoId = pago.getGrupo().getId();
        }
        if (pago.getDeudor() != null) {
            this.deudorId = pago.getDeudor().getId();
            this.deudorNombre = pago.getDeudor().getNombre();
        }
        if (pago.getAcreedor() != null) {
            this.acreedorId = pago.getAcreedor().getId();
            this.acreedorNombre = pago.getAcreedor().getNombre();
        }
        this.monto = pago.getMonto();
        this.fecha = pago.getFecha();
        this.nota = pago.getNota();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(Long grupoId) {
        this.grupoId = grupoId;
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

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }
}