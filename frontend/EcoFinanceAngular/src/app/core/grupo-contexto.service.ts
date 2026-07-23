import { Injectable, inject, signal } from '@angular/core';
import { Grupo, GrupoService } from './grupo.service';

@Injectable({
  providedIn: 'root'
})
export class GrupoContextoService {
  private readonly grupoService = inject(GrupoService);

  readonly grupos = signal<Grupo[]>([]);
  readonly grupoSeleccionadoId = signal<number | null>(null);

  cargarGrupos(): void {
    this.grupoService.listar().subscribe({
      next: (grupos) => {
        this.grupos.set(grupos);

        if (grupos.length === 1) {
          this.seleccionarGrupo(grupos[0].id);
          return;
        }

        const tieneSeleccionActual = this.grupoSeleccionadoId() !== null
          && grupos.some(grupo => grupo.id === this.grupoSeleccionadoId());

        if (!tieneSeleccionActual) {
          this.grupoSeleccionadoId.set(null);
        }
      },
      error: () => {
        this.grupos.set([]);
        this.grupoSeleccionadoId.set(null);
      }
    });
  }

  seleccionarGrupo(id: number | null): void {
    if (id === null) {
      this.grupoSeleccionadoId.set(null);
      return;
    }

    const existe = this.grupos().some(grupo => grupo.id === id);
    this.grupoSeleccionadoId.set(existe ? id : null);
  }

  limpiarContexto(): void {
    this.grupos.set([]);
    this.grupoSeleccionadoId.set(null);
  }
}
