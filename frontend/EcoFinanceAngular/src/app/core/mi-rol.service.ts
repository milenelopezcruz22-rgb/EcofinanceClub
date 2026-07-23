import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api-config';

// Rol de la cuenta autenticada DENTRO de cada grupo (MIEMBRO o GESTOR).
// No es el rol global del Usuario (ROLE_ADMIN/ROLE_GESTOR/ROLE_MIEMBRO): una
// misma cuenta puede ser GESTOR de un grupo y MIEMBRO de otro.
export interface MiRolGrupo {
  grupoId: number;
  grupoNombre: string;
  rolGrupo: string;
}

@Injectable({
  providedIn: 'root'
})
export class MiRolService {

  private readonly url = `${API_BASE_URL}/miembros/mi-rol`;

  constructor(private http: HttpClient) {}

  obtenerMisRoles(): Observable<MiRolGrupo[]> {
    return this.http.get<MiRolGrupo[]>(this.url);
  }
}
