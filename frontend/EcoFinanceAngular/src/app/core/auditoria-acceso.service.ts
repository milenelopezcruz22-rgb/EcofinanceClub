import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api-config';
import { GrupoContextoService } from './grupo-contexto.service';

// Registro de acceso a un módulo, tal como lo devuelve el backend
// (AuditoriaAccesoDTO en Java).
export interface AuditoriaAccesoDTO {
  id: number;
  usuario: string;
  rol: string;
  grupo?: string;
  modulo: string;
  fecha: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuditoriaAccesoService {

  private readonly url = `${API_BASE_URL}/auditoria-accesos`;
  private readonly grupoContextoService = inject(GrupoContextoService);

  constructor(private http: HttpClient) {}

  // Registra que el usuario actual entró a "modulo". No se espera
  // respuesta ni se bloquea la pantalla por esto: si falla (por ejemplo,
  // sin conexión), simplemente no queda el registro, pero el módulo igual
  // debe abrirse con normalidad.
  registrar(modulo: string): void {
    const idGrupo = this.grupoContextoService.grupoSeleccionadoId();
    const grupoSeleccionado = this.grupoContextoService.grupos()
      .find(grupo => grupo.id === idGrupo);

    this.http.post(this.url, {
      modulo,
      grupo: grupoSeleccionado?.nombre
    }).subscribe({ error: () => {} });
  }

  // Historial completo de accesos. Solo el Administrador General puede
  // llamar esto (el backend responde 403 a cualquier otro rol); los
  // filtros por usuario, rol y fecha se aplican en pantalla sobre esta
  // misma lista.
  listar(): Observable<AuditoriaAccesoDTO[]> {
    return this.http.get<AuditoriaAccesoDTO[]>(this.url);
  }
}
