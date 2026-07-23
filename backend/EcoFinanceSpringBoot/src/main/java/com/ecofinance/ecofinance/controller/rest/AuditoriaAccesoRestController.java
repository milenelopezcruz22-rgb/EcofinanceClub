package com.ecofinance.ecofinance.controller.rest;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecofinance.ecofinance.dto.AuditoriaAccesoDTO;
import com.ecofinance.ecofinance.dto.RegistrarAccesoRequest;
import com.ecofinance.ecofinance.entity.Usuario;
import com.ecofinance.ecofinance.service.AuditoriaAccesoService;
import com.ecofinance.ecofinance.service.UsuarioService;

// Auditoría de navegación (distinta de /api/auditoria, que es la auditoría
// de operaciones sobre Gasto/Presupuesto). Cualquier usuario autenticado
// (ADMIN, GESTOR o MIEMBRO) puede registrar su propio acceso a un módulo;
// solo el Administrador General puede ver el historial completo.
@RestController
@RequestMapping("/api/auditoria-accesos")
public class AuditoriaAccesoRestController {

    @Autowired
    private AuditoriaAccesoService auditoriaAccesoService;

    @Autowired
    private UsuarioService usuarioService;

    // POST /api/auditoria-accesos -> se llama una vez por cada entrada a un
    // módulo (lo dispara el propio Angular al iniciar cada pantalla).
    @PostMapping
    public ResponseEntity<Void> registrar(@Valid @RequestBody RegistrarAccesoRequest request) {
        auditoriaAccesoService.registrar(request.getModulo(), request.getGrupo());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // GET /api/auditoria-accesos -> historial completo, solo para la
    // pantalla de Accesos del Administrador General.
    @GetMapping
    public ResponseEntity<List<AuditoriaAccesoDTO>> listar(Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);
        if (!usuarioService.esAdminGeneral(usuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<AuditoriaAccesoDTO> lista = auditoriaAccesoService.listar().stream()
                .map(AuditoriaAccesoDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }
}
