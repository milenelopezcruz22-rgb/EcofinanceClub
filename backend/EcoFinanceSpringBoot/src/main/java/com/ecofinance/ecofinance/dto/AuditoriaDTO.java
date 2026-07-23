package com.ecofinance.ecofinance.dto;

import java.time.LocalDateTime;
import com.ecofinance.ecofinance.entity.Auditoria;

public class AuditoriaDTO {

    private Long id;
    private String entidad;
    private Long entidadId;
    private String accion;
    private String usuario;
    private LocalDateTime fecha;
    private String detalle;

    public AuditoriaDTO() {
    }

    public AuditoriaDTO(Auditoria auditoria) {
        this.id = auditoria.getId();
        this.entidad = auditoria.getEntidad();
        this.entidadId = auditoria.getEntidadId();
        this.accion = auditoria.getAccion();
        this.usuario = auditoria.getUsuario();
        this.fecha = auditoria.getFecha();
        this.detalle = auditoria.getDetalle();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public Long getEntidadId() {
        return entidadId;
    }

    public void setEntidadId(Long entidadId) {
        this.entidadId = entidadId;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }
}