package com.ecofinance.ecofinance.entity;

import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "gastos")

public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private double monto;

    // Se usa una columna NUEVA (categoria_id) en vez de reutilizar la vieja
    // columna "categoria" (varchar), para que Hibernate con ddl-auto=update
    // solo agregue la columna nueva y no intente alterar el tipo de la
    // existente. La columna vieja "categoria" queda en la tabla sin uso.
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    // Mismo criterio: columna nueva "pagador_id" en vez de reutilizar el
    // viejo "pagador" (varchar).
    @ManyToOne
    @JoinColumn(name = "pagador_id")
    private MiembroGrupo pagador;

    // Ya no la ingresa el usuario: el backend la calcula siempre a partir
    // de la cantidad de miembros del grupo del gasto.
    @Column(nullable = false)
    private int cantidadMiembros;

    @Column(nullable = false)
    private String impacto;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "division", nullable = false)
    private Double montoPersona;

    @ManyToOne
    @JoinColumn(name="grupo_id")
    private Grupo grupo;

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public Gasto() {
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

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public MiembroGrupo getPagador() {
        return pagador;
    }

    public void setPagador(MiembroGrupo pagador) {
        this.pagador = pagador;
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
        return montoPersona;
    }

    public void setMontoPorPersona(Double montoPorPersona) {
        this.montoPersona = montoPorPersona;
    }

    
}