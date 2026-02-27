import { Routes } from '@angular/router';
import { HomeComponent } from './features/home/home.component';
import { ClienteListComponent } from './features/clientes/cliente-list/cliente-list.component';
import { CuentaListComponent } from './features/cuentas/cuenta-list/cuenta-list.component';
import { MovimientoListComponent } from './features/movimientos/movimiento-list/movimiento-list.component';
import { ReporteMainComponent } from './features/reportes/reporte-main/reporte-main.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'clientes', component: ClienteListComponent },
  { path: 'cuentas', component: CuentaListComponent },
  { path: 'movimientos', component: MovimientoListComponent },
  { path: 'reportes', component: ReporteMainComponent },
  { path: '**', redirectTo: '' }
];
