import { Component, OnInit, Injector, effect, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Menu } from '../menu/menu';
import { GastoService, Gasto } from '../../core/gasto.service';
import { CategoriaService, Categoria } from '../../core/categoria.service';
import { MiembroService, Miembro } from '../../core/miembro.service';
import { AuthService } from '../../core/auth.service';
import { GrupoContextoService } from '../../core/grupo-contexto.service';

@Component({
  selector: 'app-gastos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, Menu],
  templateUrl: './gastos.html',
  styleUrl: './gastos.scss'
})
export class Gastos implements OnInit {
  protected readonly grupoContextoService = inject(GrupoContextoService);
  private readonly injector = inject(Injector);

  formulario!: FormGroup;

  readonly listaGrupos = this.grupoContextoService.grupos;
  readonly grupoSeleccionadoId = this.grupoContextoService.grupoSeleccionadoId;
  listaGastos = signal<Gasto[]>([]);

  categoriasDelGrupo = signal<Categoria[]>([]);
  miembrosDelGrupo = signal<Miembro[]>([]);

  constructor(
    private gastoService: GastoService,
    private categoriaService: CategoriaService,
    private miembroService: MiembroService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.formulario = new FormGroup({
      grupo: new FormControl('', Validators.required),
      descripcion: new FormControl('', Validators.required),
      monto: new FormControl('', Validators.required),
      categoria: new FormControl('', Validators.required),
      pagador: new FormControl('', Validators.required),
      fecha: new FormControl('', Validators.required)
    });

    this.grupoContextoService.cargarGrupos();

    effect(() => {
      const idGrupo = this.grupoContextoService.grupoSeleccionadoId();
      this.formulario.patchValue({ grupo: idGrupo ?? '' }, { emitEvent: false });

      this.formulario.patchValue(
        { categoria: '', pagador: '' },
        { emitEvent: false }
      );

      if (!idGrupo) {
        this.categoriasDelGrupo.set([]);
        this.miembrosDelGrupo.set([]);
        this.listaGastos.set([]);
        return;
      }

      this.categoriaService.listarPorGrupo(idGrupo).subscribe({
        next: (categorias) => this.categoriasDelGrupo.set(categorias),
        error: () => this.categoriasDelGrupo.set([])
      });

      this.miembroService.listarPorGrupo(idGrupo).subscribe({
        next: (miembros) => this.miembrosDelGrupo.set(miembros),
        error: () => this.miembrosDelGrupo.set([])
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
      this.listaGastos.set([]);
      return;
    }

    this.gastoService.listarPorGrupo(grupoActual).subscribe({
      next: (gastos) => this.listaGastos.set(gastos),
      error: () => this.listaGastos.set([])
    });
  }

  guardar(): void {
    if (this.formulario.valid) {
      const valores = this.formulario.value;

      this.gastoService.guardar({
        descripcion: valores.descripcion,
        monto: Number(valores.monto),
        categoriaId: Number(valores.categoria),
        pagadorId: Number(valores.pagador),
        impacto: this.impactoDeCategoria(valores.categoria),
        fecha: valores.fecha,
        grupoId: Number(valores.grupo)
      }).subscribe({
        next: () => {
          const grupoActual = valores.grupo;
          this.formulario.reset({ grupo: grupoActual });
          this.listar(Number(grupoActual));
        },
        error: (err) => alert(err.error?.mensaje ?? 'Error al guardar el gasto')
      });
    }
  }

  impactoDeCategoria(categoriaId: string | number | null | undefined): string {
    if (!categoriaId) {
      return 'Sin clasificar';
    }
    const categoria = this.categoriasDelGrupo().find(c => c.id === Number(categoriaId));
    return categoria?.impactoAmbiental ?? 'Sin clasificar';
  }

  eliminar(id: number): void {
    this.gastoService.eliminar(id).subscribe({
      next: () => this.listar(),
      error: (err) => alert(err.error?.mensaje ?? 'Error al eliminar el gasto')
    });
  }
}