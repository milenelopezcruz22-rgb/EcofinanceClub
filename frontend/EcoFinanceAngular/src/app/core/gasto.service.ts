import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api-config';

// Payload de entrada para crear/actualizar un gasto.
// categoriaId y pagadorId reemplazan a los antiguos campos de texto libre
// "categoria" y "pagador". cantidadMiembros ya NO se envía: el backend
// siempre la calcula a partir de los miembros del grupo.
export interface GastoDTO {
  descripcion: string;
  monto: number;
  categoriaId: number;
  pagadorId: number;
  impacto: string;
  fecha: string; // formato ISO yyyy-MM-dd
  grupoId?: number;
}

// Gasto tal como lo devuelve el backend: siempre trae id, montoPorPersona,
// cantidadMiembros calculada, grupoNombre, categoriaNombre y pagadorNombre.
export interface Gasto extends GastoDTO {
  id: number;
  montoPorPersona?: number;
  cantidadMiembros?: number;
  grupoNombre?: string;
  categoriaNombre?: string;
  pagadorNombre?: string;
}

// RF23: saldo neto de un miembro del grupo (lo que pagó vs. lo que le
// corresponde pagar).
export interface SaldoMiembro {
  miembroId: number;
  nombre: string;
  pagado: number;
  cuota: number;
  saldo: number;
}

// RF24: "quién le debe a quién", ya simplificado por el backend.
export interface Deuda {
  deudorId: number;
  deudorNombre: string;
  acreedorId: number;
  acreedorNombre: string;
  monto: number;
}

@Injectable({
  providedIn: 'root'
})
export class GastoService {

  private readonly url = `${API_BASE_URL}/gastos`;

  constructor(private http: HttpClient) {}

  listar(): Observable<Gasto[]> {
    return this.http.get<Gasto[]>(this.url);
  }

  buscar(id: number): Observable<Gasto> {
    return this.http.get<Gasto>(`${this.url}/${id}`);
  }

  listarPorGrupo(idGrupo: number): Observable<Gasto[]> {
    return this.http.get<Gasto[]>(`${this.url}/grupo/${idGrupo}`);
  }

  guardar(gasto: GastoDTO): Observable<Gasto> {
    return this.http.post<Gasto>(this.url, gasto);
  }

  actualizar(id: number, gasto: GastoDTO): Observable<Gasto> {
    return this.http.put<Gasto>(`${this.url}/${id}`, gasto);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  obtenerSaldos(idGrupo: number): Observable<SaldoMiembro[]> {
    return this.http.get<SaldoMiembro[]>(`${this.url}/grupo/${idGrupo}/saldos`);
  }

  obtenerDeudas(idGrupo: number): Observable<Deuda[]> {
    return this.http.get<Deuda[]>(`${this.url}/grupo/${idGrupo}/deudas`);
  }
}