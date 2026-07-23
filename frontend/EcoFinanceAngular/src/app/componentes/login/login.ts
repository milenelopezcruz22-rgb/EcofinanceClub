import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { GrupoContextoService } from '../../core/grupo-contexto.service';
import { AuditoriaAccesoService } from '../../core/auditoria-acceso.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login implements OnInit {

  formularioLogin!: FormGroup;

  constructor(
    private router: Router,
    private authService: AuthService,
    private grupoContextoService: GrupoContextoService,
    private auditoriaAccesoService: AuditoriaAccesoService
  ){}

  ngOnInit(): void {

    this.formularioLogin = new FormGroup({

      usuario: new FormControl('',[
        Validators.required,
        Validators.minLength(3)
      ]),

      password: new FormControl('',[
        Validators.required,
        Validators.minLength(6)
      ])

    });

  }

  ingresar(){

    if(this.formularioLogin.invalid){

      alert("Complete correctamente los campos");

      return;

    }

    const usuario=this.formularioLogin.value.usuario;

    const password=this.formularioLogin.value.password;

    this.authService.login(usuario, password).subscribe({

      next: () => {

        this.auditoriaAccesoService.registrar('LOGIN');

        this.grupoContextoService.cargarGrupos();

        if (this.authService.esAdmin()) {
          this.router.navigate(['/auditoria']);
          return;
        }

        this.router.navigate(['/dashboard']);

      },

      error: (err) => {

        const mensaje = err.error?.mensaje ?? 'Usuario o contraseña incorrectos';

        alert(mensaje);

      }

    });

  }

}