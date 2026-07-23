package com.ecofinance.ecofinance.dto;

import java.time.LocalDate;

import com.ecofinance.ecofinance.entity.Gasto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class GastoDTO {

    private Long id;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    @Positive(message = "El monto debe ser mayor a 0")
    private double monto;

    // Antes era un String libre. Ahora es la relación con Categoria:
    // el cliente manda el id de una categoría que pertenezca al grupo
    // seleccionado, y el backend devuelve también el nombre para mostrarlo.
    @NotNull(message = "La categoría es obligatoria")
    private Long categoriaId;
    private String categoriaNombre;

    // Antes era un String libre. Ahora es la relación con MiembroGrupo:
    // el cliente manda el id de un miembro que pertenezca al grupo
    // seleccionado (el pagador), y el backend devuelve también su nombre.
    @NotNull(message = "El pagador es obligatorio")
    private Long pagadorId;
    private String pagadorNombre;

    // Ya no se valida como obligatorio: el backend la calcula siempre a
    // partir de la cantidad de miembros del grupo, se ignora si el cliente
    // manda un valor.
    private int cantidadMiembros;

    @NotBlank(message = "El impacto es obligatorio")
    private String impacto;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    private Double montoPorPersona;

    @NotNull(message = "El grupo es obligatorio")
    private Long grupoId;
    private String grupoNombre;

    public GastoDTO() {
    }

    public GastoDTO(Gasto gasto) {
        this.id = gasto.getId();
        this.descripcion = gasto.getDescripcion();
        this.monto = gasto.getMonto();
        if (gasto.getCategoria() != null) {
            this.categoriaId = gasto.getCategoria().getId();
            this.categoriaNombre = gasto.getCategoria().getNombre();
        }
        if (gasto.getPagador() != null) {
            this.pagadorId = gasto.getPagador().getId();
            this.pagadorNombre = gasto.getPagador().getNombre();
        }
        this.cantidadMiembros = gasto.getCantidadMiembros();
        this.impacto = gasto.getImpacto();
        this.fecha = gasto.getFecha();
        this.montoPorPersona = gasto.getMontoPorPersona();
        if (gasto.getGrupo() != null) {
            this.grupoId = gasto.getGrupo().getId();
            this.grupoNombre = gasto.getGrupo().getNombre();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public Long getPagadorId() {
        return pagadorId;
    }

    public void setPagadorId(Long pagadorId) {
        this.pagadorId = pagadorId;
    }

    public String getPagadorNombre() {
        return pagadorNombre;
    }

    public void setPagadorNombre(String pagadorNombre) {
        this.pagadorNombre = pagadorNombre;
    }

    public int getCantidadMiembros() {
        return cantidadMiembros;
    }

    public void setCantidadMiembros(int cantidadMiembros) {
        this.cantidadMiembros = cantidadMiembros;
    }

    public String getImpacto() {
        return impacto;
    }

    public void setImpacto(String impacto) {
        this.impacto = impacto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Double getMontoPorPersona() {
        return montoPorPersona;
    }

    public void setMontoPorPersona(Double montoPorPersona) {
        this.montoPorPersona = montoPorPersona;
    }

    public Long getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(Long grupoId) {
        this.grupoId = grupoId;
    }

    public String getGrupoNombre() {
        return grupoNombre;
    }

    public void setGrupoNombre(String grupoNombre) {
        this.grupoNombre = grupoNombre;
    }
}