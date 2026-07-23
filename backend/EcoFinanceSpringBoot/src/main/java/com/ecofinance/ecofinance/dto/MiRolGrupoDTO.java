package com.ecofinance.ecofinance.dto;

import com.ecofinance.ecofinance.entity.MiembroGrupo;

// El rol de la cuenta autenticada DENTRO de un grupo puntual (MIEMBRO o
// GESTOR). No confundir con el rol global del Usuario (ROLE_ADMIN,
// ROLE_GESTOR, ROLE_MIEMBRO): una misma cuenta puede ser GESTOR de un grupo
// y MIEMBRO de otro. Lo usa el frontend para decidir qué módulos/acciones
// mostrar en cada grupo, sin depender del rol global del JWT.
public class MiRolGrupoDTO {

    private Long grupoId;
    private String grupoNombre;
    private String rolGrupo;

    public MiRolGrupoDTO() {
    }

    public MiRolGrupoDTO(MiembroGrupo miembro) {
        this.grupoId = miembro.getGrupo().getId();
        this.grupoNombre = miembro.getGrupo().getNombre();
        this.rolGrupo = miembro.getRolGrupo();
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

    public String getRolGrupo() {
        return rolGrupo;
    }

    public void setRolGrupo(String rolGrupo) {
        this.rolGrupo = rolGrupo;
    }
}
