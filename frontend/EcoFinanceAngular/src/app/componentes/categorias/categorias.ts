import { Component, OnInit, Injector, effect, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { Menu } from '../menu/menu';
import { CategoriaService, Categoria } from '../../core/categoria.service';
import { AuthService } from '../../core/auth.service';
import { GrupoContextoService } from '../../core/grupo-contexto.service';

@Component({
  selector: 'app-categorias',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    Menu
  ],
  templateUrl: './categorias.html',
  styleUrl: './categorias.scss'
})
export class Categorias implements OnInit {
  protected readonly grupoContextoService = inject(GrupoContextoService);
  private readonly injector = inject(Injector);

  formulario!: FormGroup;

  readonly listaGrupos = this.grupoContextoService.grupos;
  readonly grupoSeleccionado = this.grupoContextoService.grupoSeleccionadoId;
  listaCategorias = signal<Categoria[]>([]);

  constructor(
    private categoriaService: CategoriaService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.formulario = new FormGroup({
      grupo: new FormControl('', Validators.required),
      nombre: new FormControl('', Validators.required),
      impactoAmbiental: new FormControl('', Validators.required)
    });

    this.grupoContextoService.cargarGrupos();

    effect(() => {
      const idGrupo = this.grupoContextoService.grupoSeleccionadoId();
      this.formulario.patchValue({ grupo: idGrupo ?? '' }, { emitEvent: false });

      this.listar();
    }, { injector: this.injector });

    this.formulario.get('grupo')!.valueChanges.subscribe((valor) => {
      const idGrupo = valor ? Number(valor) : null;
      this.grupoContextoService.seleccionarGrupo(idGrupo);
    });
  }

  listar(): void {
    const idGrupo = this.grupoContextoService.grupoSeleccionadoId();

    if (!idGrupo) {
      this.listaCategorias.set([]);
      return;
    }

    this.categoriaService.listarPorGrupo(idGrupo).subscribe({
      next: (categorias) => this.listaCategorias.set(categorias),
      error: () => this.listaCategorias.set([])
    });
  }

  guardar(): void {
    if (this.formulario.valid) {
      const valores = this.formulario.value;

      this.categoriaService.guardar({
        nombre: valores.nombre,
        impactoAmbiental: valores.impactoAmbiental,
        grupoId: Number(valores.grupo)
      }).subscribe({
        next: () => {
          this.formulario.patchValue({ nombre: '', impactoAmbiental: '' });
          this.listar();
        },
        error: (err) => alert(err.error?.mensaje ?? 'Error al guardar la categoría')
      });
    }
  }

  eliminar(id: number): void {
    this.categoriaService.eliminar(id).subscribe({
      next: () => this.listar(),
      error: (err) => alert(err.error?.mensaje ?? 'Error al eliminar la categoría')
    });
  }

  claseImpacto(nivel?: string): string {
    if (nivel === 'Alto') return 'impacto-alto';
    if (nivel === 'Medio') return 'impacto-medio';
    if (nivel === 'Bajo') return 'impacto-bajo';
    return 'impacto-desconocido';
  }
}