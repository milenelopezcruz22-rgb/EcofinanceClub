import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { API_BASE_URL } from './api-config';

// Respuesta que devuelve el backend en /api/auth/login y /api/auth/registro
export interface AuthResponse {
  token: string;
  username: string;
  rol: string;
}

const TOKEN_KEY = 'ecofinance_token';
const USERNAME_KEY = 'ecofinance_username';
const ROL_KEY = 'ecofinance_rol';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly authUrl = `${API_BASE_URL}/auth`;

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.authUrl}/login`, { username, password })
      .pipe(tap(respuesta => this.guardarSesion(respuesta)));
  }

  registrar(username: string, password: string, email: string, nombreCompleto?: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.authUrl}/registro`, { username, password, email, nombreCompleto })
      .pipe(tap(respuesta => this.guardarSesion(respuesta)));
  }

  private guardarSesion(respuesta: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, respuesta.token);
    localStorage.setItem(USERNAME_KEY, respuesta.username);
    localStorage.setItem(ROL_KEY, respuesta.rol);
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USERNAME_KEY);
    localStorage.removeItem(ROL_KEY);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  getUsername(): string | null {
    return localStorage.getItem(USERNAME_KEY);
  }

  getRol(): string | null {
    return localStorage.getItem(ROL_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  // --- Gestión de usuarios y permisos ---
  // Helpers de rol para que los componentes de pantalla oculten botones y
  // formularios que ese usuario no puede usar. Es solo comodidad visual: el
  // backend (SecurityConfig + controllers) sigue siendo quien realmente
  // valida los permisos en cada petición.
  esAdmin(): boolean {
    return this.getRol() === 'ROLE_ADMIN';
  }

  esGestor(): boolean {
    return this.getRol() === 'ROLE_GESTOR';
  }

  esMiembro(): boolean {
    return this.getRol() === 'ROLE_MIEMBRO';
  }

  // Administrador General o Administrador de Grupo: quienes administran
  // Miembros, Categorías y Presupuestos (crear/editar/eliminar).
  puedeGestionar(): boolean {
    return this.esAdmin() || this.esGestor();
  }
}