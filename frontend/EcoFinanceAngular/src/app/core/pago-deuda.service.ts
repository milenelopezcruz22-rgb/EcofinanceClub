import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api-config';

// RF25: payload para registrar un pago que liquida (total o parcialmente)
// la deuda de un miembro (deudor) hacia otro (acreedor) dentro de un grupo.
export interface PagoDeudaDTO {
  grupoId: number;
  deudorId: number;
  acreedorId: number;
  monto: number;
  fecha: string; // formato ISO yyyy-MM-dd
  nota?: string;
}

// Pago tal como lo devuelve el backend: siempre trae id y los nombres de
// deudor/acreedor ya resueltos, listos para mostrar en tablas.
export interface PagoDeuda extends PagoDeudaDTO {
  id: number;
  deudorNombre?: string;
  acreedorNombre?: string;
}

@Injectable({
  providedIn: 'root'
})
export class PagoDeudaService {

  private readonly url = `${API_BASE_URL}/pagos-deuda`;

  constructor(private http: HttpClient) {}

  listarPorGrupo(idGrupo: number): Observable<PagoDeuda[]> {
    return this.http.get<PagoDeuda[]>(`${this.url}/grupo/${idGrupo}`);
  }

  registrar(pago: PagoDeudaDTO): Observable<PagoDeuda> {
    return this.http.post<PagoDeuda>(this.url, pago);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}