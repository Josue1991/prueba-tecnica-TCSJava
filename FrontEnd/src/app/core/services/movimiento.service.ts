import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Movimiento } from '../models/movimiento.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MovimientoService {
  private readonly API_URL = `${environment.apiUrl}/movimientos`;

  constructor(private http: HttpClient) { }

  obtenerMovimientoPorId(id: number): Observable<Movimiento> {
    return this.http.get<Movimiento>(`${this.API_URL}/${id}`)
      .pipe(catchError(this.handleError));
  }

  obtenerTodosLosMovimientos(): Observable<Movimiento[]> {
    return this.http.get<Movimiento[]>(this.API_URL)
      .pipe(catchError(this.handleError));
  }

  obtenerMovimientosPorCuenta(cuentaId: number): Observable<Movimiento[]> {
    return this.http.get<Movimiento[]>(`${this.API_URL}/cuenta/${cuentaId}`)
      .pipe(catchError(this.handleError));
  }

  obtenerMovimientosPorCliente(clienteId: number): Observable<Movimiento[]> {
    return this.http.get<Movimiento[]>(`${this.API_URL}/cliente/${clienteId}`)
      .pipe(catchError(this.handleError));
  }

  obtenerMovimientosPorCuentaYFechas(
    cuentaId: number, 
    fechaInicio: string, 
    fechaFin: string
  ): Observable<Movimiento[]> {
    const params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);
    
    return this.http.get<Movimiento[]>(`${this.API_URL}/cuenta/${cuentaId}/rango`, { params })
      .pipe(catchError(this.handleError));
  }

  obtenerMovimientosPorClienteYFechas(
    clienteId: number, 
    fechaInicio: string, 
    fechaFin: string
  ): Observable<Movimiento[]> {
    const params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);
    
    return this.http.get<Movimiento[]>(`${this.API_URL}/cliente/${clienteId}/rango`, { params })
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Ha ocurrido un error desconocido';
    
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      errorMessage = error.error?.message || `Error ${error.status}: ${error.statusText}`;
    }
    
    console.error('Error en MovimientoService:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
