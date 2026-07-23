package com.ecofinance.ecofinance.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;

// Auditoría de navegación: una fila por cada vez que un usuario ENTRA a un
// módulo del sistema (Login, Dashboard, Grupos, Miembros, Categorías,
// Presupuestos, Gastos, Perfil, Logout). Es una tabla distinta de
// "auditoria" (que registra crear/editar/eliminar de Gasto y Presupuesto):
// acá el evento es "el usuario abrió esta pantalla", no una operación sobre
// un dato. Se llena con un solo INSERT, igual de simple que el resto del
// proyecto.
@Entity
@Table(name = "auditoria_acceso")
public class AuditoriaAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Username de quien entró al módulo (viene del JWT ya validado).
    @Column(nullable = false, length = 100)
    private String usuario;

    // Rol global de esa cuenta en el momento del acceso, ej. "ROLE_ADMIN".
    @Column(nullable = false, length = 30)
    private String rol;

    // Nombre del grupo activo en ese momento (si aplica). ADMIN, o un
    // usuario que todavía no seleccionó grupo, no tienen uno: queda null.
    @Column(length = 100)
    private String grupo;

    // Módulo visitado: LOGIN, DASHBOARD, GRUPOS, MIEMBROS, CATEGORIAS,
    // PRESUPUESTOS, GASTOS, PERFIL o LOGOUT.
    @Column(nullable = false, length = 30)
    private String modulo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    public AuditoriaAcceso() {
    }

    public AuditoriaAcceso(String usuario, String rol, String grupo, String modulo) {
        this.usuario = usuario;
        this.rol = rol;
        this.grupo = grupo;
        this.modulo = modulo;
        this.fecha = LocalDateTime.now();
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
