package com.ecofinance.ecofinance.entity;

import jakarta.persistence.*;

@Entity
public class MiembroGrupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String correo;
    private String rolGrupo;

    @ManyToOne
    @JoinColumn(name="grupo_id")
    private Grupo grupo;

    // Vínculo opcional con una cuenta de login real (Usuario). Si es null,
    // esta fila sigue siendo un simple registro informativo de miembro, como
    // hasta ahora. Si tiene valor, esa cuenta (con rol ROLE_GESTOR o
    // ROLE_MIEMBRO) queda autorizada a acceder a este Grupo puntual.
    @ManyToOne
    @JoinColumn(name="usuario_id", nullable = true)
    private Usuario usuario;

    public MiembroGrupo() {
    }

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

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo=correo;
    }

    public String getRolGrupo() {
        return rolGrupo;
    }

    public void setRolGrupo(String rolGrupo) {
        this.rolGrupo=rolGrupo;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo=grupo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

}