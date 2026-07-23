import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api-config';

// Payload de entrada para crear/actualizar un presupuesto.
// categoriaId reemplaza al antiguo campo de texto libre "categoria": debe
// ser el id de una categoría que pertenezca al grupo seleccionado.
// gastoActual NO se envía: el backend lo calcula siempre a partir de los
// gastos de esa categoría dentro del grupo.
export interface PresupuestoDTO {
  categoriaId: number;
  limiteGasto: number;
  grupoId?: number;
}

// Presupuesto tal como lo devuelve el backend: con id, categoriaNombre,
// gastoActual y excedido ya calculados.
export interface Presupuesto extends PresupuestoDTO {
  id: number;
  categoriaNombre?: string;
  gastoActual: number;
  grupoNombre?: string;
  excedido: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class PresupuestoService {

  private readonly url = `${API_BASE_URL}/presupuestos`;

  constructor(private http: HttpClient) {}

  listar(): Observable<Presupuesto[]> {
    return this.http.get<Presupuesto[]>(this.url);
  }

  buscar(id: number): Observable<Presupuesto> {
    return this.http.get<Presupuesto>(`${this.url}/${id}`);
  }

  listarPorGrupo(idGrupo: number): Observable<Presupuesto[]> {
    return this.http.get<Presupuesto[]>(`${this.url}/grupo/${idGrupo}`);
  }

  guardar(presupuesto: PresupuestoDTO): Observable<Presupuesto> {
    return this.http.post<Presupuesto>(this.url, presupuesto);
  }

  actualizar(id: number, presupuesto: PresupuestoDTO): Observable<Presupuesto> {
    return this.http.put<Presupuesto>(`${this.url}/${id}`, presupuesto);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}