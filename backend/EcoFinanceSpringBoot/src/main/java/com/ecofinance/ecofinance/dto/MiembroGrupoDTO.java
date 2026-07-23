package com.ecofinance.ecofinance.dto;

import com.ecofinance.ecofinance.entity.MiembroGrupo;
import jakarta.validation.constraints.NotBlank;

public class MiembroGrupoDTO {

    private Long id;

    // Ya NO se escriben a mano: se derivan automáticamente del Usuario
    // vinculado (usuarioId) al momento de guardar. Se quitó @NotBlank a
    // propósito -- si el cliente no los manda, MiembroGrupoRestController los
    // completa desde la cuenta real en "usuarios".
    private String nombre;

    private String correo;

    @NotBlank(message = "El rol dentro del grupo es obligatorio")
    private String rolGrupo;

    private Long grupoId;
    private String grupoNombre;

    // Vínculo con una cuenta de login (Usuario del sistema). Ahora es
    // OBLIGATORIO: el flujo correcto es "asignar una cuenta ya registrada a
    // un grupo", no crear un registro de miembro suelto sin cuenta. Se valida
    // en el controller (usuarioId nulo -> 400).
    private Long usuarioId;
    private String usuarioUsername;

    public MiembroGrupoDTO() {
    }

    public MiembroGrupoDTO(MiembroGrupo miembro) {
        this.id = miembro.getId();
        this.nombre = miembro.getNombre();
        this.correo = miembro.getCorreo();
        this.rolGrupo = miembro.getRolGrupo();
        if (miembro.getGrupo() != null) {
            this.grupoId = miembro.getGrupo().getId();
            this.grupoNombre = miembro.getGrupo().getNombre();
        }
        if (miembro.getUsuario() != null) {
            this.usuarioId = miembro.getUsuario().getId();
            this.usuarioUsername = miembro.getUsuario().getUsername();
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

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getRolGrupo() {
        return rolGrupo;
    }

    public void setRolGrupo(String rolGrupo) {
        this.rolGrupo = rolGrupo;
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

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioUsername() {
        return usuarioUsername;
    }

    public void setUsuarioUsername(String usuarioUsername) {
        this.usuarioUsername = usuarioUsername;
    }
}