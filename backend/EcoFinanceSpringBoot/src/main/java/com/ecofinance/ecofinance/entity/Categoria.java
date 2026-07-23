package com.ecofinance.ecofinance.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    // Nivel de impacto ambiental de esta categoría: "Bajo", "Medio" o "Alto".
    // Nullable a nivel de columna a propósito: las categorías creadas ANTES de
    // este cambio quedan con impactoAmbiental = null ("Sin clasificar") hasta
    // que se editen; el Dashboard las excluye de la distribución por nivel
    // mientras tanto, sin que esto rompa nada de lo que ya funcionaba.
    @Column(length = 20)
    private String impactoAmbiental;

    @ManyToOne
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    public Categoria() {
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

    public String getImpactoAmbiental() {
        return impactoAmbiental;
    }

    public void setImpactoAmbiental(String impactoAmbiental) {
        this.impactoAmbiental = impactoAmbiental;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }
}