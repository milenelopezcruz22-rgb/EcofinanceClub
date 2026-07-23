import { Component, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/auth.service';

@Component({

selector:'app-home',

standalone:true,

imports:[RouterModule, CommonModule],

templateUrl:'./home.html',

styleUrls:['./home.scss']

})

export class Home{

  readonly authService = inject(AuthService);

}
