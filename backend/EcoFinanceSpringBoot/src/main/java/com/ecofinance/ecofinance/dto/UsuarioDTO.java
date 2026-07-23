package com.ecofinance.ecofinance.dto;

import com.ecofinance.ecofinance.entity.Usuario;

// DTO de salida, solo lectura. Se usa únicamente para que el Administrador
// General pueda listar las cuentas existentes y elegir a cuál vincular un
// MiembroGrupo. A propósito no incluye el password ni ningún dato sensible.
public class UsuarioDTO {

    private Long id;
    private String username;
    private String email;
    private String nombreCompleto;
    private String rol;

    public UsuarioDTO() {
    }

    public UsuarioDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.username = usuario.getUsername();
        this.email = usuario.getEmail();
        this.nombreCompleto = usuario.getNombreCompleto();
        this.rol = usuario.getRol().getNombre();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}