package com.ecofinance.ecofinance.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ecofinance.ecofinance.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse cuerpo = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Datos inválidos en la petición",
                errores
        );
        return ResponseEntity.badRequest().body(cuerpo);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> manejarIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse cuerpo = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return ResponseEntity.badRequest().body(cuerpo);
    }

    // Se dispara cuando un service valida ANTES de tocar la base de datos que
    // una operación dejaría datos huérfanos (ej. borrar un MiembroGrupo que
    // todavía tiene Gastos o Pagos de Deuda a su nombre). El mensaje ya viene
    // armado y específico desde el service, así que solo se envuelve en 409.
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> manejarEstadoInvalido(IllegalStateException ex) {
        ErrorResponse cuerpo = new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(cuerpo);
    }

    // Se dispara cuando la base de datos rechaza una operación por una
    // restricción de integridad — el caso más común en este proyecto es
    // intentar ELIMINAR un Grupo, Categoría o Miembro que todavía tiene
    // Gastos/Presupuestos asociados (la clave foránea lo impide). Antes caía
    // en el catch-all genérico de abajo y mostraba un mensaje sin sentido;
    // ahora se explica la causa real con un 409 (conflicto).
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> manejarIntegridad(DataIntegrityViolationException ex) {
        ErrorResponse cuerpo = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "No se puede completar la operación: el registro tiene datos asociados (grupos, categorías, presupuestos o gastos relacionados)."
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(cuerpo);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarGenerico(Exception ex) {
        ErrorResponse cuerpo = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocurrió un error inesperado en el servidor"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(cuerpo);
    }
}