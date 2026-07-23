import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api-config';

// Payload de entrada para crear/actualizar una categoría.
// grupoId es obligatorio: toda categoría pertenece a un grupo.
// impactoAmbiental: "Bajo", "Medio" o "Alto".
export interface CategoriaDTO {
  nombre: string;
  impactoAmbiental: string;
  grupoId: number;
}

// Categoría tal como la devuelve el backend: siempre trae id y grupoNombre.
// impactoAmbiental puede venir undefined en categorías creadas antes de este
// campo (todavía sin clasificar).
//
// Nota sobre el "Omit": no se puede hacer `extends CategoriaDTO` y volver
// opcional `impactoAmbiental` al mismo tiempo, porque TypeScript exige que
// las propiedades heredadas mantengan un tipo asignable al de la interfaz
// base (error TS2430: "incorrectly extends interface"). Por eso acá se
// excluye esa propiedad de la base con Omit<...> y se vuelve a declarar,
// ahora sí como opcional, sin tocar CategoriaDTO (que sigue exigiendo
// impactoAmbiental obligatorio al crear/editar, como corresponde).
export interface Categoria extends Omit<CategoriaDTO, 'impactoAmbiental'> {
  id: number;
  impactoAmbiental?: string;
  grupoNombre?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CategoriaService {

  private readonly url = `${API_BASE_URL}/categorias`;

  constructor(private http: HttpClient) {}

  listar(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.url);
  }

  buscar(id: number): Observable<Categoria> {
    return this.http.get<Categoria>(`${this.url}/${id}`);
  }

  listarPorGrupo(idGrupo: number): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(`${this.url}/grupo/${idGrupo}`);
  }

  guardar(categoria: CategoriaDTO): Observable<Categoria> {
    return this.http.post<Categoria>(this.url, categoria);
  }

  actualizar(id: number, categoria: CategoriaDTO): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.url}/${id}`, categoria);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}