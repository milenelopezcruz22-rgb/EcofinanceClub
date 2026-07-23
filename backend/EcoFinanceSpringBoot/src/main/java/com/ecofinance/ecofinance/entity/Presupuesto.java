package com.ecofinance.ecofinance.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "presupuestos")

public class Presupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Se usa una columna NUEVA (categoria_id) en vez de reutilizar la vieja
    // columna "categoria" (varchar), por el mismo motivo que en Gasto: con
    // ddl-auto=update, Hibernate solo agrega la columna nueva y no toca la
    // existente. La columna vieja "categoria" queda en la tabla sin uso.
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(nullable = false)
    private double limiteGasto;

    @Column(nullable = false)
    private double gastoActual;

    public Presupuesto() {
    }

    @ManyToOne
    @JoinColumn(name="grupo_id")
    private Grupo grupo;

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
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

}