import { Component, OnInit, inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Menu } from '../menu/menu';
import { GrupoService } from '../../core/grupo.service';
import { AuthService } from '../../core/auth.service';
import { GrupoContextoService } from '../../core/grupo-contexto.service';

@Component({
  selector: 'app-grupos',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    Menu
  ],
  templateUrl: './grupos.html',
  styleUrl: './grupos.scss'
})
export class Grupos implements OnInit {
  protected readonly grupoContextoService = inject(GrupoContextoService);

  formularioGrupo!: FormGroup;

  readonly listaGrupos = this.grupoContextoService.grupos;

  constructor(
    private grupoService: GrupoService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.formularioGrupo = new FormGroup({
      nombre: new FormControl('', [Validators.required]),
      descripcion: new FormControl('', [Validators.required])
    });

    this.grupoContextoService.cargarGrupos();
  }

  guardar() {
    if (this.formularioGrupo.valid) {
      this.grupoService.guardar(this.formularioGrupo.value).subscribe({
        next: () => {
          this.formularioGrupo.reset();
          this.grupoContextoService.cargarGrupos();
        },
        error: (err) => alert(err.error?.mensaje ?? 'Error al guardar el grupo')
      });
    }
  }

  eliminar(id: number) {
    this.grupoService.eliminar(id).subscribe({
      next: () => this.grupoContextoService.cargarGrupos(),
      error: (err) => alert(err.error?.mensaje ?? 'Error al eliminar el grupo')
    });
  }
}