import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api-config';

// Usuario tal como lo devuelve el backend (solo lectura, sin password).
// Se usa únicamente para que el Administrador General elija a qué cuenta
// vincula un MiembroGrupo desde la pantalla de Miembros.
export interface Usuario {
  id: number;
  username: string;
  email: string;
  nombreCompleto?: string;
  rol: string;
}

@Injectable({
  providedIn: 'root'
})
export class UsuarioService {

  private readonly url = `${API_BASE_URL}/usuarios`;

  constructor(private http: HttpClient) {}

  // Solo lectura: no hay guardar/actualizar/eliminar a propósito, no es un
  // CRUD de usuarios. El backend además restringe todo /api/usuarios/** a
  // ROLE_ADMIN, así que solo el Administrador General puede llamar esto.
  listar(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(this.url);
  }

  // Usuarios (GESTOR o MIEMBRO) que todavía no están asignados a ningún
  // grupo. Llena el selector de "usuario a asignar" en Miembros.
  listarSinGrupo(): Observable<Usuario[]> {
    return this.http.get<Usuario[]>(`${this.url}/sin-grupo`);
  }
}