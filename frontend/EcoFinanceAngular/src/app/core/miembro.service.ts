import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api-config';

// Payload de entrada para asignar un usuario ya registrado a un grupo.
// nombre/correo YA NO se escriben a mano: el backend los deriva de la
// cuenta real en "usuarios" a partir de usuarioId. Se dejan opcionales acá
// solo para no romper el tipo Miembro (que sí los devuelve, ya derivados).
export interface MiembroDTO {
  nombre?: string;
  correo?: string;
  // 'MIEMBRO' o 'GESTOR': el rol que esa cuenta tiene DENTRO de este grupo.
  rolGrupo: string;
  grupoId: number;
  // Obligatorio en el flujo actual: se asigna una cuenta ya registrada,
  // nunca se crea un "miembro de papel" sin cuenta.
  usuarioId: number;
}

// Miembro tal como lo devuelve el backend: siempre trae id y grupoNombre
export interface Miembro extends MiembroDTO {
  id: number;
  grupoNombre?: string;
  usuarioUsername?: string;
}

@Injectable({
  providedIn: 'root'
})
export class MiembroService {

  private readonly url = `${API_BASE_URL}/miembros`;

  constructor(private http: HttpClient) {}

  listar(): Observable<Miembro[]> {
    return this.http.get<Miembro[]>(this.url);
  }

  buscar(id: number): Observable<Miembro> {
    return this.http.get<Miembro>(`${this.url}/${id}`);
  }

  listarPorGrupo(idGrupo: number): Observable<Miembro[]> {
    return this.http.get<Miembro[]>(`${this.url}/grupo/${idGrupo}`);
  }

  guardar(miembro: MiembroDTO): Observable<Miembro> {
    return this.http.post<Miembro>(this.url, miembro);
  }

  actualizar(id: number, miembro: MiembroDTO): Observable<Miembro> {
    return this.http.put<Miembro>(`${this.url}/${id}`, miembro);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}