package com.ecofinance.ecofinance.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;

// Auditoría transaccional mínima: una fila por cada crear/editar/eliminar de
// Gasto o Presupuesto. Suficiente para cubrir el requerimiento del profesor
// sin agregar una arquitectura de eventos ni un módulo aparte: es una tabla
// simple que se llena con un solo INSERT desde los services existentes.
@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // "GASTO" o "PRESUPUESTO"
    @Column(nullable = false, length = 30)
    private String entidad;

    // Id de la fila de Gasto/Presupuesto afectada.
    @Column(nullable = false)
    private Long entidadId;

    // "CREAR", "EDITAR" o "ELIMINAR"
    @Column(nullable = false, length = 20)
    private String accion;

    // Username de quien hizo la acción (o "sistema" si no hay usuario
    // autenticado en el contexto, por ejemplo en pruebas).
    @Column(nullable = false, length = 100)
    private String usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    // Texto corto descriptivo (ej. "Gasto: Supermercado - S/ 120.0"), solo
    // para que la pantalla de auditoría sea legible sin tener que ir a
    // buscar el registro original (que puede ya no existir, si fue borrado).
    @Column(length = 255)
    private String detalle;

    public Auditoria() {
    }

    public Auditoria(String entidad, Long entidadId, String accion, String usuario, String detalle) {
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.accion = accion;
        this.usuario = usuario;
        this.detalle = detalle;
        this.fecha = LocalDateTime.now();
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