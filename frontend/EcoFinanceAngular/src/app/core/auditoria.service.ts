import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api-config';

// Registro de auditoría tal como lo devuelve el backend (AuditoriaDTO en
// Java). Se llama AuditoriaDTO acá también (y no "Auditoria") para no chocar
// con el nombre de la clase del componente Angular que lo consume.
export interface AuditoriaDTO {
  id: number;
  entidad: string;
  entidadId: number;
  accion: string;
  usuario: string;
  fecha: string;
  detalle?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuditoriaService {

  private readonly url = `${API_BASE_URL}/auditoria`;

  constructor(private http: HttpClient) {}

  listar(): Observable<AuditoriaDTO[]> {
    return this.http.get<AuditoriaDTO[]>(this.url);
  }
}
