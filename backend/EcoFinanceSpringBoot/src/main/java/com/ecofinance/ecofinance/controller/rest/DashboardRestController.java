package com.ecofinance.ecofinance.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecofinance.ecofinance.dto.DashboardDTO;
import com.ecofinance.ecofinance.entity.Usuario;
import com.ecofinance.ecofinance.service.DashboardService;
import com.ecofinance.ecofinance.service.MiembroGrupoService;
import com.ecofinance.ecofinance.service.UsuarioService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardRestController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private MiembroGrupoService miembroGrupoService;

    @Autowired
    private UsuarioService usuarioService;

    // GET /api/dashboard/grupo/{idGrupo} -> resumen financiero completo de ese
    // grupo. Mismo criterio de acceso que el resto de los módulos: ADMIN ve
    // cualquier grupo, GESTOR/MIEMBRO solo el suyo (según MiembroGrupo.usuario).
    @GetMapping("/grupo/{idGrupo}")
    public ResponseEntity<DashboardDTO> obtenerDashboard(@PathVariable Long idGrupo, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);
        if (!miembroGrupoService.tieneAccesoAGrupo(usuario, idGrupo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return dashboardService.obtenerDashboard(idGrupo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}