package com.ecofinance.ecofinance.controller.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecofinance.ecofinance.dto.AuditoriaDTO;
import com.ecofinance.ecofinance.entity.Usuario;
import com.ecofinance.ecofinance.service.AuditoriaService;
import com.ecofinance.ecofinance.service.UsuarioService;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaRestController {

    @Autowired
    private AuditoriaService auditoriaService;

    @Autowired
    private UsuarioService usuarioService;

    // Solo el Administrador General puede ver el historial completo: es una
    // pantalla de control interno, no de operación diaria de un grupo.
    @GetMapping
    public ResponseEntity<List<AuditoriaDTO>> listar(Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);
        if (!usuarioService.esAdminGeneral(usuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<AuditoriaDTO> lista = auditoriaService.listar().stream()
                .map(AuditoriaDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }
}