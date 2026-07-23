package com.ecofinance.ecofinance.dto;

import com.ecofinance.ecofinance.entity.Categoria;
import jakarta.validation.constraints.NotBlank;

// DTO de Categoria: se usa para recibir datos del cliente (crear/actualizar,
// donde solo se envía grupoId en vez del objeto Grupo completo) y para
// devolver datos al cliente (incluyendo el nombre del grupo).
public class CategoriaDTO {

    private Long id;

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    private String nombre;

    // "Bajo", "Medio" o "Alto". Obligatorio para categorías NUEVAS; las que ya
    // existían antes de este campo pueden llegar en null hasta que se editen.
    @NotBlank(message = "El impacto ambiental es obligatorio")
    private String impactoAmbiental;

    private Long grupoId;
    private String grupoNombre;

    public CategoriaDTO() {
    }

    public CategoriaDTO(Categoria categoria) {
        this.id = categoria.getId();
        this.nombre = categoria.getNombre();
        this.impactoAmbiental = categoria.getImpactoAmbiental();
        if (categoria.getGrupo() != null) {
            this.grupoId = categoria.getGrupo().getId();
            this.grupoNombre = categoria.getGrupo().getNombre();
        }
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