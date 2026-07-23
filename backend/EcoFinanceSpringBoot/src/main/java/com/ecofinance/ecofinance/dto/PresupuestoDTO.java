package com.ecofinance.ecofinance.dto;

import com.ecofinance.ecofinance.entity.Presupuesto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PresupuestoDTO {

    private Long id;

    // Antes era un String libre. Ahora es la relación con Categoria:
    // el cliente manda el id de una categoría que pertenezca al grupo
    // seleccionado, y el backend devuelve también el nombre para mostrarlo.
    @NotNull(message = "La categoría es obligatoria")
    private Long categoriaId;
    private String categoriaNombre;

    @Positive(message = "El límite de gasto debe ser mayor a 0")
    private double limiteGasto;

    private double gastoActual;

    @NotNull(message = "El grupo es obligatorio")
    private Long grupoId;
    private String grupoNombre;
    private boolean excedido;

    public PresupuestoDTO() {
    }

    public PresupuestoDTO(Presupuesto presupuesto) {
        this.id = presupuesto.getId();
        if (presupuesto.getCategoria() != null) {
            this.categoriaId = presupuesto.getCategoria().getId();
            this.categoriaNombre = presupuesto.getCategoria().getNombre();
        }
        this.limiteGasto = presupuesto.getLimiteGasto();
        this.gastoActual = presupuesto.getGastoActual();
        this.excedido = presupuesto.getGastoActual() > presupuesto.getLimiteGasto();
        if (presupuesto.getGrupo() != null) {
            this.grupoId = presupuesto.getGrupo().getId();
            this.grupoNombre = presupuesto.getGrupo().getNombre();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public double getLimiteGasto() {
        return limiteGasto;
    }

    public void setLimiteGasto(double limiteGasto) {
        this.limiteGasto = limiteGasto;
    }

    public double getGastoActual() {
        return gastoActual;
    }

    public void setGastoActual(double gastoActual) {
        this.gastoActual = gastoActual;
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

    public boolean isExcedido() {
        return excedido;
    }

    public void setExcedido(boolean excedido) {
        this.excedido = excedido;
    }
}