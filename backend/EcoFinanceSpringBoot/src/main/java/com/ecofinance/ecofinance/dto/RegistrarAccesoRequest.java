package com.ecofinance.ecofinance.dto;

import jakarta.validation.constraints.NotBlank;

// Lo que manda Angular cada vez que un usuario entra a un módulo. El
// usuario y el rol NO viajan acá: se toman del JWT ya validado, así nadie
// puede registrar un acceso "a nombre de otro". El grupo sí lo manda el
// frontend porque es solo el grupo que el usuario tiene seleccionado en ese
// momento (contexto de UI), no un dato que haya que validar contra la BD.
public class RegistrarAccesoRequest {

    @NotBlank
    private String modulo;

    private String grupo;

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }
}
