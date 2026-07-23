package com.ecofinance.ecofinance.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponse {

    private LocalDateTime fecha;
    private int status;
    private String mensaje;
    private Map<String, String> errores;

    public ErrorResponse() {
    }

    public ErrorResponse(int status, String mensaje) {
        this.fecha = LocalDateTime.now();
        this.status = status;
        this.mensaje = mensaje;
    }

    public ErrorResponse(int status, String mensaje, Map<String, String> errores) {
        this.fecha = LocalDateTime.now();
        this.status = status;
        this.mensaje = mensaje;
        this.errores = errores;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Map<String, String> getErrores() {
        return errores;
    }

    public void setErrores(Map<String, String> errores) {
        this.errores = errores;
    }
}