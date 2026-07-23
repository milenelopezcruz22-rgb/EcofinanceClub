package com.ecofinance.ecofinance.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="grupos")
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false,length=100)
    private String nombre;

    @Column(length=250)
    private String descripcion;

    @OneToMany(mappedBy = "grupo")
    private List<Gasto> gastos = new ArrayList<>();

    @OneToMany(mappedBy = "grupo")
    private List<Presupuesto> presupuestos = new ArrayList<>();

    @OneToMany(mappedBy = "grupo")
    private List<Categoria> categorias = new ArrayList<>();

    @Transient
    private Double disponible;

    @Transient
    private Boolean excedido;
    @Transient
    private Double totalGastado;

    @Transient
    private Double promedioGastoPorMiembro;

    @Transient
    private Double mayorGasto;
    public Grupo(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id=id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre=nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion=descripcion;
    }

    public List<Gasto> getGastos() {
        return gastos;
    }

    public void setGastos(List<Gasto> gastos) {
        this.gastos=gastos;
    }

    public List<Presupuesto> getPresupuestos() {
        return presupuestos;
    }

    public void setPresupuestos(List<Presupuesto> presupuestos) {
        this.presupuestos=presupuestos;
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
    }

    public Double getDisponible() {
        return disponible;
    }

    public void setDisponible(Double disponible) {
        this.disponible=disponible;
    }

    public Boolean getExcedido() {
        return excedido;
    }

    public void setExcedido(Boolean excedido) {
        this.excedido=excedido;
    }

    public Double getTotalGastado() {
        return totalGastado;
    }

    public void setTotalGastado(Double totalGastado) {
        this.totalGastado = totalGastado;
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