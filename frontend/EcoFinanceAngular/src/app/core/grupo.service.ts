import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api-config';

// Payload de entrada para crear/actualizar un grupo (el id lo asigna el backend)
export interface GrupoDTO {
  nombre: string;
  descripcion?: string;
}

// Grupo tal como lo devuelve el backend: siempre trae id y las métricas calculadas
export interface Grupo extends GrupoDTO {
  id: number;
  totalGastado?: number;
  disponible?: number;
  excedido?: boolean;
  promedioGastoPorMiembro?: number;
  mayorGasto?: number;
}

@Injectable({
  providedIn: 'root'
})
export class GrupoService {

  private readonly url = `${API_BASE_URL}/grupos`;

  constructor(private http: HttpClient) {}

  listar(): Observable<Grupo[]> {
    return this.http.get<Grupo[]>(this.url);
  }

  buscar(id: number): Observable<Grupo> {
    return this.http.get<Grupo>(`${this.url}/${id}`);
  }

  guardar(grupo: GrupoDTO): Observable<Grupo> {
    return this.http.post<Grupo>(this.url, grupo);
  }

  actualizar(id: number, grupo: GrupoDTO): Observable<Grupo> {
    return this.http.put<Grupo>(`${this.url}/${id}`, grupo);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}