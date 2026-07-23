package com.ecofinance.ecofinance.controller.rest;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ecofinance.ecofinance.dto.GrupoDTO;
import com.ecofinance.ecofinance.entity.Grupo;
import com.ecofinance.ecofinance.entity.Usuario;
import com.ecofinance.ecofinance.service.GrupoService;
import com.ecofinance.ecofinance.service.MiembroGrupoService;
import com.ecofinance.ecofinance.service.UsuarioService;

@RestController
@RequestMapping("/api/grupos")
public class GrupoRestController {

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private MiembroGrupoService miembroGrupoService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<GrupoDTO> listar(Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);
        return filtrarAccesibles(grupoService.listar(), usuario).stream()
                .map(GrupoDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GrupoDTO> buscar(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);
        if (!miembroGrupoService.tieneAccesoAGrupo(usuario, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return grupoService.buscar(id)
                .map(grupo -> ResponseEntity.ok(new GrupoDTO(grupo)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public List<GrupoDTO> buscarPorNombre(@RequestParam String nombre, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);
        return filtrarAccesibles(grupoService.buscarNombre(nombre), usuario).stream()
                .map(GrupoDTO::new)
                .collect(Collectors.toList());
    }

    // --- Gestión de usuarios y permisos ---
    // Administrador General: ve todos los grupos, sin filtrar. Administrador
    // de Grupo / Miembro: solo los grupos donde tienen una fila de
    // MiembroGrupo vinculada a su cuenta (usuario_id).
    private List<Grupo> filtrarAccesibles(List<Grupo> grupos, Usuario usuario) {
        if (usuarioService.esAdminGeneral(usuario)) {
            return grupos;
        }
        List<Long> idsAccesibles = miembroGrupoService.idsGruposDeUsuario(usuario);
        return grupos.stream()
                .filter(grupo -> idsAccesibles.contains(grupo.getId()))
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<GrupoDTO> guardar(@Valid @RequestBody GrupoDTO grupoDTO) {
        Grupo grupo = new Grupo();
        grupo.setNombre(grupoDTO.getNombre());
        grupo.setDescripcion(grupoDTO.getDescripcion());

        Grupo guardado = grupoService.guardar(grupo);
        return ResponseEntity.status(HttpStatus.CREATED).body(new GrupoDTO(guardado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GrupoDTO> actualizar(@PathVariable Long id, @Valid @RequestBody GrupoDTO grupoDTO) {
        return grupoService.buscar(id)
                .map(grupoExistente -> {
                    grupoExistente.setNombre(grupoDTO.getNombre());
                    grupoExistente.setDescripcion(grupoDTO.getDescripcion());
                    Grupo actualizado = grupoService.guardar(grupoExistente);
                    return ResponseEntity.ok(new GrupoDTO(actualizado));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (grupoService.buscar(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        grupoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}