import { Component, OnInit, Injector, effect, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { Menu } from '../menu/menu';
import { PresupuestoService, Presupuesto } from '../../core/presupuesto.service';
import { CategoriaService, Categoria } from '../../core/categoria.service';
import { AuthService } from '../../core/auth.service';
import { GrupoContextoService } from '../../core/grupo-contexto.service';

@Component({
  selector: 'app-presupuestos',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    Menu
  ],
  templateUrl: './presupuestos.html',
  styleUrl: './presupuestos.scss'
})
export class Presupuestos implements OnInit {
  protected readonly grupoContextoService = inject(GrupoContextoService);
  private readonly injector = inject(Injector);

  formulario!: FormGroup;

  readonly listaGrupos = this.grupoContextoService.grupos;
  readonly grupoSeleccionadoId = this.grupoContextoService.grupoSeleccionadoId;
  listaPresupuestos = signal<Presupuesto[]>([]);

  categoriasDelGrupo = signal<Categoria[]>([]);

  constructor(
    private presupuestoService: PresupuestoService,
    private categoriaService: CategoriaService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.formulario = new FormGroup({
      grupo: new FormControl('', Validators.required),
      categoria: new FormControl('', Validators.required),
      limiteGasto: new FormControl('', Validators.required)
    });

    this.grupoContextoService.cargarGrupos();

    effect(() => {
      const idGrupo = this.grupoContextoService.grupoSeleccionadoId();
      this.formulario.patchValue({ grupo: idGrupo ?? '' }, { emitEvent: false });

      this.formulario.patchValue(
        { categoria: '' },
        { emitEvent: false }
      );

      if (!idGrupo) {
        this.categoriasDelGrupo.set([]);
        this.listaPresupuestos.set([]);
        return;
      }

      this.categoriaService.listarPorGrupo(idGrupo).subscribe({
        next: (categorias) => this.categoriasDelGrupo.set(categorias),
        error: () => this.categoriasDelGrupo.set([])
      });

      this.listar(idGrupo);
    }, { injector: this.injector });

    this.formulario.get('grupo')!.valueChanges.subscribe((valor) => {
      const idGrupo = valor ? Number(valor) : null;
      this.grupoContextoService.seleccionarGrupo(idGrupo);
    });
  }

  listar(idGrupo?: number): void {
    const grupoActual = idGrupo ?? this.grupoContextoService.grupoSeleccionadoId();

    if (!grupoActual) {
      this.listaPresupuestos.set([]);
      return;
    }

    this.presupuestoService.listarPorGrupo(grupoActual).subscribe({
      next: (presupuestos) => this.listaPresupuestos.set(presupuestos),
      error: () => this.listaPresupuestos.set([])
    });
  }

  guardar(): void {
    if (this.formulario.valid) {
      const valores = this.formulario.value;

      this.presupuestoService.guardar({
        categoriaId: Number(valores.categoria),
        limiteGasto: Number(valores.limiteGasto),
        grupoId: Number(valores.grupo)
      }).subscribe({
        next: () => {
          const grupoActual = valores.grupo;
          this.formulario.reset({ grupo: grupoActual });
          this.listar(Number(grupoActual));
        },
        error: (err) => alert(err.error?.mensaje ?? 'Error al guardar el presupuesto')
      });
    }
  }

  eliminar(id: number): void {
    this.presupuestoService.eliminar(id).subscribe({
      next: () => this.listar(),
      error: (err) => alert(err.error?.mensaje ?? 'Error al eliminar el presupuesto')
    });
  }

  estado(presupuesto: Presupuesto): string {
    return presupuesto.excedido ? 'EXCEDIDO' : 'CONTROLADO';
  }
}