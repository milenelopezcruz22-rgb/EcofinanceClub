package com.ecofinance.ecofinance.controller.rest;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.ecofinance.ecofinance.dto.CategoriaDTO;
import com.ecofinance.ecofinance.entity.Categoria;
import com.ecofinance.ecofinance.entity.Grupo;
import com.ecofinance.ecofinance.entity.Usuario;
import com.ecofinance.ecofinance.service.CategoriaService;
import com.ecofinance.ecofinance.service.GrupoService;
import com.ecofinance.ecofinance.service.MiembroGrupoService;
import com.ecofinance.ecofinance.service.UsuarioService;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaRestController {

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private GrupoService grupoService;

    @Autowired
    private MiembroGrupoService miembroGrupoService;

    @Autowired
    private UsuarioService usuarioService;

    // --- Gestión de usuarios y permisos ---
    private List<Categoria> filtrarAccesibles(List<Categoria> categorias, Usuario usuario) {
        if (usuarioService.esAdminGeneral(usuario)) {
            return categorias;
        }
        List<Long> idsAccesibles = miembroGrupoService.idsGruposDeUsuario(usuario);
        return categorias.stream()
                .filter(c -> c.getGrupo() != null && idsAccesibles.contains(c.getGrupo().getId()))
                .collect(Collectors.toList());
    }

    @GetMapping
    public List<CategoriaDTO> listar(Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);
        return filtrarAccesibles(categoriaService.listar(), usuario).stream()
                .map(CategoriaDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> buscar(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);
        return categoriaService.buscarPorId(id)
                .map(categoria -> {
                    if (categoria.getGrupo() != null
                            && !miembroGrupoService.tieneAccesoAGrupo(usuario, categoria.getGrupo().getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<CategoriaDTO>build();
                    }
                    return ResponseEntity.ok(new CategoriaDTO(categoria));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/grupo/{idGrupo}")
    public ResponseEntity<List<CategoriaDTO>> listarPorGrupo(@PathVariable Long idGrupo, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);
        if (!miembroGrupoService.tieneAccesoAGrupo(usuario, idGrupo)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<CategoriaDTO> lista = categoriaService.listarGrupo(idGrupo).stream()
                .map(CategoriaDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<CategoriaDTO> guardar(@Valid @RequestBody CategoriaDTO categoriaDTO, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);

        if (categoriaDTO.getGrupoId() == null) {
            return ResponseEntity.badRequest().build();
        }

        Grupo grupo = grupoService.buscar(categoriaDTO.getGrupoId()).orElse(null);
        if (grupo == null) {
            return ResponseEntity.badRequest().build();
        }

        if (!miembroGrupoService.esGestorDeGrupo(usuario, grupo.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(categoriaDTO.getNombre());
        categoria.setImpactoAmbiental(categoriaDTO.getImpactoAmbiental());
        categoria.setGrupo(grupo);

        Categoria guardada = categoriaService.guardar(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(new CategoriaDTO(guardada));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDTO> actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaDTO categoriaDTO,
                                                    Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);

        return categoriaService.buscarPorId(id)
                .map(categoriaExistente -> {
                    if (categoriaExistente.getGrupo() != null
                            && !miembroGrupoService.esGestorDeGrupo(usuario, categoriaExistente.getGrupo().getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<CategoriaDTO>build();
                    }

                    Grupo grupo = categoriaExistente.getGrupo();
                    if (categoriaDTO.getGrupoId() != null) {
                        grupo = grupoService.buscar(categoriaDTO.getGrupoId()).orElse(grupo);
                    }

                    if (grupo != null && !miembroGrupoService.esGestorDeGrupo(usuario, grupo.getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<CategoriaDTO>build();
                    }

                    categoriaExistente.setNombre(categoriaDTO.getNombre());
                    categoriaExistente.setImpactoAmbiental(categoriaDTO.getImpactoAmbiental());
                    categoriaExistente.setGrupo(grupo);

                    Categoria actualizada = categoriaService.guardar(categoriaExistente);
                    return ResponseEntity.ok(new CategoriaDTO(actualizada));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = usuarioService.obtenerPorAuthentication(authentication);

        return categoriaService.buscarPorId(id)
                .map(categoria -> {
                    if (categoria.getGrupo() != null
                            && !miembroGrupoService.esGestorDeGrupo(usuario, categoria.getGrupo().getId())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
                    }
                    categoriaService.eliminar(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}