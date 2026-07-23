import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/auth.service';

// Registro de usuario: SOLO datos de acceso (username, password, email,
// nombreCompleto). No pide rol ni grupo -- el backend asigna ROLE_MIEMBRO
// por defecto, y la asignación a un grupo la hace el ADMIN después, desde
// el módulo Miembros. Así se evita duplicar el concepto de "usuario" en más
// de un lugar: esta pantalla solo crea la fila en "usuarios".
@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterModule],
  templateUrl: './registro.html',
  styleUrl: './registro.scss'
})
export class Registro implements OnInit {

  formularioRegistro!: FormGroup;

  constructor(
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.formularioRegistro = new FormGroup({
      nombreCompleto: new FormControl('', [Validators.required]),
      email: new FormControl('', [Validators.required, Validators.email]),
      username: new FormControl('', [Validators.required, Validators.minLength(3)]),
      password: new FormControl('', [Validators.required, Validators.minLength(6)])
    });
  }

  registrar(): void {
    if (this.formularioRegistro.invalid) {
      this.formularioRegistro.markAllAsTouched();
      return;
    }

    const { username, password, email, nombreCompleto } = this.formularioRegistro.value;

    this.authService.registrar(username, password, email, nombreCompleto).subscribe({
      next: () => {
        alert('Cuenta creada correctamente. Ahora un administrador debe asignarte a un grupo para que puedas ver datos.');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        const mensaje = err.error?.mensaje ?? err.error ?? 'No se pudo completar el registro';
        alert(mensaje);
      }
    });
  }
}
