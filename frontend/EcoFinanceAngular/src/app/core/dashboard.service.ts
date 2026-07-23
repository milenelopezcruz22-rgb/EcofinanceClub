import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api-config';

// Item genérico (nombre + monto), tal como lo devuelve el backend tanto para
// gasto por categoría/miembro como para categorías de alto impacto.
export interface ItemDashboard {
  nombre: string;
  monto: number;
}

// Recomendación puntual del motor de reglas (Bloque C). El backend solo
// manda el mensaje ya redactado y el nivel de severidad como texto plano;
// el color/ícono de cada nivel se resuelve acá en Angular, no en el backend.
export interface Recomendacion {
  mensaje: string;
  nivel: 'ALTA' | 'MEDIA' | 'INFO';
}

// Resumen financiero y de sostenibilidad de un grupo. Todo viene calculado
// desde el backend (DashboardRestController): Angular solo lo pinta.
export interface DashboardData {
  grupoId: number;
  grupoNombre: string;

  gastoTotal: number;
  presupuestoTotal: number;
  presupuestoDisponible: number;

  cantidadMiembros: number;
  cantidadGastos: number;

  categoriaMayorGastoNombre?: string;
  categoriaMayorGastoMonto: number;

  miembroMayorGastoNombre?: string;
  miembroMayorGastoMonto: number;

  gastoPorCategoria: ItemDashboard[];
  gastoPorMiembro: ItemDashboard[];

  // --- Sostenibilidad (Bloque B) ---
  gastoImpactoBajo: number;
  gastoImpactoMedio: number;
  gastoImpactoAlto: number;
  porcentajeImpactoAlto: number;
  categoriasMayorImpacto: ItemDashboard[];

  // --- Recomendaciones (Bloque C) ---
  recomendaciones: Recomendacion[];

  // --- Tendencia mensual (RF27) ---
  // Mismo shape que ItemDashboard: "nombre" acá es el mes ("2026-05") y
  // "monto" el total gastado ese mes.
  tendenciaMensual: ItemDashboard[];

  // --- Liquidación de deudas (RF25) ---
  montoPendienteLiquidar: number;
  montoLiquidado: number;
  cantidadDeudasPendientes: number;
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  private readonly url = `${API_BASE_URL}/dashboard`;

  constructor(private http: HttpClient) {}

  obtenerPorGrupo(idGrupo: number): Observable<DashboardData> {
    return this.http.get<DashboardData>(`${this.url}/grupo/${idGrupo}`);
  }
}