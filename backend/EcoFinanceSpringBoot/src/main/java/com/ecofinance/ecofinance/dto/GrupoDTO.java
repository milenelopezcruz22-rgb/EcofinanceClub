package com.ecofinance.ecofinance.dto;

import com.ecofinance.ecofinance.entity.Grupo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GrupoDTO {

    private Long id;

    @NotBlank(message = "El nombre del grupo es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @Size(max = 250, message = "La descripción no puede superar los 250 caracteres")
    private String descripcion;
    private Double totalGastado;
    private Double disponible;
    private Boolean excedido;
    private Double promedioGastoPorMiembro;
    private Double mayorGasto;

    public GrupoDTO() {
    }

    public GrupoDTO(Grupo grupo) {
        this.id = grupo.getId();
        this.nombre = grupo.getNombre();
        this.descripcion = grupo.getDescripcion();
        this.totalGastado = grupo.getTotalGastado();
        this.disponible = grupo.getDisponible();
        this.excedido = grupo.getExcedido();
        this.promedioGastoPorMiembro = grupo.getPromedioGastoPorMiembro();
        this.mayorGasto = grupo.getMayorGasto();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getTotalGastado() {
        return totalGastado;
    }

    public void setTotalGastado(Double totalGastado) {
        this.totalGastado = totalGastado;
    }

    public Double getDisponible() {
        return disponible;
    }

    public void setDisponible(Double disponible) {
        this.disponible = disponible;
    }

    public Boolean getExcedido() {
        return excedido;
    }

    public void setExcedido(Boolean excedido) {
        this.excedido = excedido;
    }

    public Double getPromedioGastoPorMiembro() {
        return promedioGastoPorMiembro;
    }

    public void setPromedioGastoPorMiembro(Double promedioGastoPorMiembro) {
        this.promedioGastoPorMiembro = promedioGastoPorMiembro;
    }

    public Double getMayorGasto() {
        return mayorGasto;
    }

    public void setMayorGasto(Double mayorGasto) {
        this.mayorGasto = mayorGasto;
    }
}