import { Component, OnInit, Injector, effect, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { Menu } from '../menu/menu';
import { MiembroService, Miembro } from '../../core/miembro.service';
import { UsuarioService, Usuario } from '../../core/usuario.service';
import { AuthService } from '../../core/auth.service';
import { GrupoContextoService } from '../../core/grupo-contexto.service';

@Component({
  selector: 'app-miembros',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    Menu
  ],
  templateUrl: './miembros.html',
  styleUrl: './miembros.scss'
})
export class Miembros implements OnInit {
  protected readonly grupoContextoService = inject(GrupoContextoService);
  private readonly injector = inject(Injector);

  formulario!: FormGroup;

  readonly listaGrupos = this.grupoContextoService.grupos;
  readonly grupoSeleccionadoId = this.grupoContextoService.grupoSeleccionadoId;
  listaMiembros = signal<Miembro[]>([]);

  // Usuarios registrados (tabla "usuarios") que TODAVÍA no están asignados a
  // ningún grupo. Es lo único que el ADMIN elige para armar la asignación:
  // ya no se escribe nombre/correo a mano, se derivan de esta cuenta.
  usuariosDisponibles = signal<Usuario[]>([]);

  constructor(
    private miembroService: MiembroService,
    private usuarioService: UsuarioService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.formulario = new FormGroup({
      grupo: new FormControl('', Validators.required),
      usuario: new FormControl('', Validators.required),
      rolGrupo: new FormControl('MIEMBRO', Validators.required)
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

    if (this.authService.esAdmin()) {
      this.cargarUsuariosDisponibles();
    }
  }

  cargarUsuariosDisponibles(): void {
    this.usuarioService.listarSinGrupo().subscribe({
      next: (usuarios) => this.usuariosDisponibles.set(usuarios),
      error: (err) => alert(err.error?.mensaje ?? 'Error al cargar los usuarios disponibles')
    });
  }

  listar(): void {
    const idGrupo = this.grupoContextoService.grupoSeleccionadoId();

    if (!idGrupo) {
      this.listaMiembros.set([]);
      return;
    }

    this.miembroService.listarPorGrupo(idGrupo).subscribe({
      next: (miembros) => this.listaMiembros.set(miembros),
      error: () => this.listaMiembros.set([])
    });
  }

  guardar(): void {
    if (this.formulario.valid) {
      const valores = this.formulario.value;

      this.miembroService.guardar({
        rolGrupo: valores.rolGrupo,
        grupoId: Number(valores.grupo),
        usuarioId: Number(valores.usuario)
      }).subscribe({
        next: () => {
          this.formulario.reset({ grupo: valores.grupo, usuario: '', rolGrupo: 'MIEMBRO' });
          this.listar();
          this.cargarUsuariosDisponibles();
        },
        error: (err) => alert(err.error?.mensaje ?? err.error ?? 'Error al asignar el usuario al grupo')
      });
    }
  }

  eliminar(id: number): void {
    this.miembroService.eliminar(id).subscribe({
      next: () => {
        this.listar();
        this.cargarUsuariosDisponibles();
      },
      error: (err) => alert(err.error?.mensaje ?? 'Error al eliminar el miembro')
    });
  }
}
