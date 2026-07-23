package com.ecofinance.ecofinance.controller.rest;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ecofinance.ecofinance.dto.PresupuestoDTO;
import com.ecofinance.ecofinance.entity.Categoria;
import com.ecofinance.ecofinance.entity.Grupo;
import com.ecofinance.ecofinance.entity.Presupuesto;
import com.ecofinance.ecofinance.entity.Usuario;
import com.ecofinance.ecofinance.service.CategoriaService;
import com.ecofinance.ecofinance.service.GrupoService;
import com.ecofinance.ecofinance.service.MiembroGrupoService;
import com.ecofinance.ecofinance.service.PresupuestoService;
import com.ecofinance.ecofinance.service.UsuarioService;

@RestController
@RequestMapping("/api/presupuestos")
public class PresupuestoRestController {

    @Autowired
    private PresupuestoService presupuestoService;

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private MiembroGrupoService miembroGrupoService;

    @Autowired
    private UsuarioService usuarioService;

    // --- Gestión de usuarios y permisos ---
    private List<Presupuesto> filtrarAccesibles(List<Presupuesto> presupuestos, Usuario usuario) {
        if (usuarioService.esAdminGeneral(usuario)) {
            return presupuestos;
        }
        List<Long> idsAccesibles = miembroGrupoService.idsGruposDeUsuario(usuario);
        return presupuestos.stream()
                .filter(p -> p.getGrupo() != null && idsAccesibles.contains(p.getGrupo().getId()))
                .collect(Collectors.toList());
    }

    @GetMapping
    public List<PresupuestoDTO> listar(Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);
        return filtrarAccesibles(presupuestoService.listarPresupuestos(), usuario).stream()
                .map(PresupuestoDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PresupuestoDTO> buscar(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);
        return presupuestoService.buscarPorId(id)
                .map(presupuesto -> {
                    if (presupuesto.getGrupo() != null
                            && !miembroGrupoService.tieneAccesoAGrupo(usuario, presupuesto.getGrupo().getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<PresupuestoDTO>build();
                    }
                    return ResponseEntity.ok(new PresupuestoDTO(presupuesto));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/grupo/{idGrupo}")
    public ResponseEntity<List<PresupuestoDTO>> listarPorGrupo(@PathVariable Long idGrupo, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);
        if (!miembroGrupoService.tieneAccesoAGrupo(usuario, idGrupo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<PresupuestoDTO> lista = presupuestoService.listarPorGrupo(idGrupo).stream()
                .map(PresupuestoDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<PresupuestoDTO> guardar(@Valid @RequestBody PresupuestoDTO presupuestoDTO, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);

        Grupo grupo = grupoService.buscar(presupuestoDTO.getGrupoId()).orElse(null);
        if (grupo == null) {
            return ResponseEntity.badRequest().build();
        }

        if (!miembroGrupoService.esGestorDeGrupo(usuario, grupo.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Categoria categoria = categoriaService.buscarPorId(presupuestoDTO.getCategoriaId()).orElse(null);
        if (categoria == null || categoria.getGrupo() == null
                || !categoria.getGrupo().getId().equals(grupo.getId())) {
            return ResponseEntity.badRequest().build();
        }

        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setCategoria(categoria);
        presupuesto.setLimiteGasto(presupuestoDTO.getLimiteGasto());
        presupuesto.setGrupo(grupo);

        Presupuesto guardado = presupuestoService.guardarPresupuesto(presupuesto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PresupuestoDTO(guardado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PresupuestoDTO> actualizar(@PathVariable Long id, @Valid @RequestBody PresupuestoDTO presupuestoDTO,
                                                      Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);

        return presupuestoService.buscarPorId(id)
                .map(presupuestoExistente -> {
                    if (presupuestoExistente.getGrupo() != null
                            && !miembroGrupoService.esGestorDeGrupo(usuario, presupuestoExistente.getGrupo().getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<PresupuestoDTO>build();
                    }

                    Grupo grupo = grupoService.buscar(presupuestoDTO.getGrupoId()).orElse(presupuestoExistente.getGrupo());

                    if (grupo != null && !miembroGrupoService.esGestorDeGrupo(usuario, grupo.getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<PresupuestoDTO>build();
                    }

                    Categoria categoria = categoriaService.buscarPorId(presupuestoDTO.getCategoriaId()).orElse(null);
                    if (categoria == null || categoria.getGrupo() == null
                            || !categoria.getGrupo().getId().equals(grupo.getId())) {
                        return ResponseEntity.badRequest().<PresupuestoDTO>build();
                    }

                    presupuestoExistente.setCategoria(categoria);
                    presupuestoExistente.setLimiteGasto(presupuestoDTO.getLimiteGasto());
                    presupuestoExistente.setGrupo(grupo);

                    Presupuesto actualizado = presupuestoService.guardarPresupuesto(presupuestoExistente);
                    return ResponseEntity.ok(new PresupuestoDTO(actualizado));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);

        return presupuestoService.buscarPorId(id)
                .map(presupuesto -> {
                    if (presupuesto.getGrupo() != null
                            && !miembroGrupoService.esGestorDeGrupo(usuario, presupuesto.getGrupo().getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
                    }
                    presupuestoService.eliminarPresupuesto(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}