package com.ecofinance.ecofinance.dto;

import java.time.LocalDateTime;
import com.ecofinance.ecofinance.entity.AuditoriaAcceso;

public class AuditoriaAccesoDTO {

    private Long id;
    private String usuario;
    private String rol;
    private String grupo;
    private String modulo;
    private LocalDateTime fecha;

    public AuditoriaAccesoDTO() {
    }

    public AuditoriaAccesoDTO(AuditoriaAcceso acceso) {
        this.id = acceso.getId();
        this.usuario = acceso.getUsuario();
        this.rol = acceso.getRol();
        this.grupo = acceso.getGrupo();
        this.modulo = acceso.getModulo();
        this.fecha = acceso.getFecha();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
