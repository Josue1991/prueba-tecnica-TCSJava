import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Cuenta, CuentaRequest } from '../models/cuenta.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CuentaService {
  private readonly API_URL = `${environment.apiUrl}/cuentas`;

  constructor(private http: HttpClient) { }

  crearCuenta(cuenta: CuentaRequest): Observable<Cuenta> {
    return this.http.post<Cuenta>(this.API_URL, cuenta)
      .pipe(catchError(this.handleError));
  }

  obtenerCuentaPorId(id: number): Observable<Cuenta> {
    return this.http.get<Cuenta>(`${this.API_URL}/${id}`)
      .pipe(catchError(this.handleError));
  }

  obtenerCuentaPorNumero(numeroCuenta: string): Observable<Cuenta> {
    return this.http.get<Cuenta>(`${this.API_URL}/numero/${numeroCuenta}`)
      .pipe(catchError(this.handleError));
  }

  obtenerTodasLasCuentas(): Observable<Cuenta[]> {
    return this.http.get<Cuenta[]>(this.API_URL)
      .pipe(catchError(this.handleError));
  }

  obtenerCuentasPorCliente(clienteId: number): Observable<Cuenta[]> {
    return this.http.get<Cuenta[]>(`${this.API_URL}/cliente/${clienteId}`)
      .pipe(catchError(this.handleError));
  }

  depositar(numeroCuenta: string, monto: number): Observable<Cuenta> {
    const params = new HttpParams().set('monto', monto.toString());
    return this.http.post<Cuenta>(`${this.API_URL}/${numeroCuenta}/depositar`, null, { params })
      .pipe(catchError(this.handleError));
  }

  retirar(numeroCuenta: string, monto: number): Observable<Cuenta> {
    const params = new HttpParams().set('monto', monto.toString());
    return this.http.post<Cuenta>(`${this.API_URL}/${numeroCuenta}/retirar`, null, { params })
      .pipe(catchError(this.handleError));
  }

  desactivarCuenta(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API_URL}/${id}/desactivar`, {})
      .pipe(catchError(this.handleError));
  }

  activarCuenta(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API_URL}/${id}/activar`, {})
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Ha ocurrido un error desconocido';
    
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      errorMessage = error.error?.message || `Error ${error.status}: ${error.statusText}`;
    }
    
    console.error('Error en CuentaService:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
