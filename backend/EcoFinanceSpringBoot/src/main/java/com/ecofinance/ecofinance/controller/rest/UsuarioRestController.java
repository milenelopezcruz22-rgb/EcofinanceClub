package com.ecofinance.ecofinance.controller.rest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecofinance.ecofinance.dto.UsuarioDTO;
import com.ecofinance.ecofinance.service.UsuarioService;

// --- Gestión de usuarios y permisos ---
// Controller de solo lectura. Su único propósito es que el Administrador
// General pueda listar las cuentas existentes desde la pantalla de Miembros,
// para elegir a cuál vincular un MiembroGrupo (columna usuario_id). No es un
// CRUD de usuarios: no hay POST/PUT/DELETE acá, y el acceso completo queda
// restringido a ROLE_ADMIN en SecurityConfig (todos los verbos, no solo
// escritura), porque ni GESTOR ni MIEMBRO deben poder ver este listado.
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<UsuarioDTO> listar() {
        return usuarioService.listar().stream()
                .map(UsuarioDTO::new)
                .collect(Collectors.toList());
    }

    // Usuarios (GESTOR o MIEMBRO) que todavía no están asignados a ningún
    // grupo. Llena el selector de "usuario a asignar" en el módulo Miembros
    // del panel de ADMIN, para no tener que volver a escribir a mano el
    // nombre/correo de una cuenta que ya existe en "usuarios".
    @GetMapping("/sin-grupo")
    public List<UsuarioDTO> listarSinGrupo() {
        return usuarioService.listarSinGrupo().stream()
                .map(UsuarioDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> buscar(@PathVariable Long id) {
        return usuarioService.buscarPorId(id)
                .map(usuario -> ResponseEntity.ok(new UsuarioDTO(usuario)))
                .orElse(ResponseEntity.notFound().build());
    }
}