import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { 
  ReporteMovimientosCliente, 
  ReporteMovimientosCuenta, 
  ReporteCuentas,
  ArchivoResponse
} from '../models/reporte.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ReporteService {
  private readonly API_URL = `${environment.apiUrl}/reportes`;

  constructor(private http: HttpClient) { }

  // ===== REPORTES JSON =====

  obtenerReporteMovimientosCliente(
    clienteId: number, 
    fechaInicio: string, 
    fechaFin: string
  ): Observable<ReporteMovimientosCliente> {
    const params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);
    
    return this.http.get<ReporteMovimientosCliente>(
      `${this.API_URL}/movimientos/cliente/${clienteId}`, 
      { params }
    ).pipe(catchError(this.handleError));
  }

  obtenerReporteMovimientosCuenta(
    cuentaId: number, 
    fechaInicio: string, 
    fechaFin: string
  ): Observable<ReporteMovimientosCuenta> {
    const params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);
    
    return this.http.get<ReporteMovimientosCuenta>(
      `${this.API_URL}/movimientos/cuenta/${cuentaId}`, 
      { params }
    ).pipe(catchError(this.handleError));
  }

  obtenerReporteCuentas(
    fechaInicio: string, 
    fechaFin: string
  ): Observable<ReporteCuentas> {
    const params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);
    
    return this.http.get<ReporteCuentas>(
      `${this.API_URL}/cuentas`, 
      { params }
    ).pipe(catchError(this.handleError));
  }

  // ===== DESCARGA DE ARCHIVOS (PDF/EXCEL) =====

  descargarReporteClientePdf(
    clienteId: number, 
    fechaInicio: string, 
    fechaFin: string
  ): Observable<ArchivoResponse> {
    const params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);
    
    return this.http.get<ArchivoResponse>(
      `${this.API_URL}/movimientos/cliente/${clienteId}/pdf`, 
      { params }
    ).pipe(catchError(this.handleError));
  }

  descargarReporteClienteExcel(
    clienteId: number, 
    fechaInicio: string, 
    fechaFin: string
  ): Observable<ArchivoResponse> {
    const params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);
    
    return this.http.get<ArchivoResponse>(
      `${this.API_URL}/movimientos/cliente/${clienteId}/excel`, 
      { params }
    ).pipe(catchError(this.handleError));
  }

  descargarReporteCuentaPdf(
    cuentaId: number, 
    fechaInicio: string, 
    fechaFin: string
  ): Observable<ArchivoResponse> {
    const params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);
    
    return this.http.get<ArchivoResponse>(
      `${this.API_URL}/movimientos/cuenta/${cuentaId}/pdf`, 
      { params }
    ).pipe(catchError(this.handleError));
  }

  descargarReporteCuentaExcel(
    cuentaId: number, 
    fechaInicio: string, 
    fechaFin: string
  ): Observable<ArchivoResponse> {
    const params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);
    
    return this.http.get<ArchivoResponse>(
      `${this.API_URL}/movimientos/cuenta/${cuentaId}/excel`, 
      { params }
    ).pipe(catchError(this.handleError));
  }

  descargarReporteCuentasPdf(
    fechaInicio: string, 
    fechaFin: string
  ): Observable<ArchivoResponse> {
    const params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);
    
    return this.http.get<ArchivoResponse>(
      `${this.API_URL}/cuentas/pdf`, 
      { params }
    ).pipe(catchError(this.handleError));
  }

  descargarReporteCuentasExcel(
    fechaInicio: string, 
    fechaFin: string
  ): Observable<ArchivoResponse> {
    const params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);
    
    return this.http.get<ArchivoResponse>(
      `${this.API_URL}/cuentas/excel`, 
      { params }
    ).pipe(catchError(this.handleError));
  }

  // ===== ENVÍO POR EMAIL =====

  enviarReporteClientePorEmail(
    clienteId: number,
    fechaInicio: string,
    fechaFin: string,
    emailDestinatario: string,
    formato: 'PDF' | 'EXCEL',
    asunto?: string,
    mensajeAdicional?: string
  ): Observable<string> {
    let params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin)
      .set('emailDestinatario', emailDestinatario)
      .set('formato', formato);

    if (asunto) {
      params = params.set('asunto', asunto);
    }
    if (mensajeAdicional) {
      params = params.set('mensajeAdicional', mensajeAdicional);
    }

    return this.http.post<string>(
      `${this.API_URL}/movimientos/cliente/${clienteId}/enviar`,
      null,
      { params, responseType: 'text' as 'json' }
    ).pipe(catchError(this.handleError));
  }

  enviarReporteCuentaPorEmail(
    cuentaId: number,
    fechaInicio: string,
    fechaFin: string,
    emailDestinatario: string,
    formato: 'PDF' | 'EXCEL',
    asunto?: string,
    mensajeAdicional?: string
  ): Observable<string> {
    let params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin)
      .set('emailDestinatario', emailDestinatario)
      .set('formato', formato);

    if (asunto) {
      params = params.set('asunto', asunto);
    }
    if (mensajeAdicional) {
      params = params.set('mensajeAdicional', mensajeAdicional);
    }

    return this.http.post<string>(
      `${this.API_URL}/movimientos/cuenta/${cuentaId}/enviar`,
      null,
      { params, responseType: 'text' as 'json' }
    ).pipe(catchError(this.handleError));
  }

  enviarReporteCuentasPorEmail(
    fechaInicio: string,
    fechaFin: string,
    emailDestinatario: string,
    formato: 'PDF' | 'EXCEL',
    asunto?: string,
    mensajeAdicional?: string
  ): Observable<string> {
    let params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin)
      .set('emailDestinatario', emailDestinatario)
      .set('formato', formato);

    if (asunto) {
      params = params.set('asunto', asunto);
    }
    if (mensajeAdicional) {
      params = params.set('mensajeAdicional', mensajeAdicional);
    }

    return this.http.post<string>(
      `${this.API_URL}/cuentas/enviar`,
      null,
      { params, responseType: 'text' as 'json' }
    ).pipe(catchError(this.handleError));
  }

  // ===== UTILIDADES =====

  /**
   * Descarga un archivo desde base64
   */
  descargarArchivo(archivoResponse: ArchivoResponse): void {
    const byteCharacters = atob(archivoResponse.contenidoBase64);
    const byteNumbers = new Array(byteCharacters.length);
    for (let i = 0; i < byteCharacters.length; i++) {
      byteNumbers[i] = byteCharacters.charCodeAt(i);
    }
    const byteArray = new Uint8Array(byteNumbers);
    const blob = new Blob([byteArray], { type: archivoResponse.tipoMime });
    
    const link = document.createElement('a');
    link.href = window.URL.createObjectURL(blob);
    link.download = archivoResponse.nombreArchivo;
    link.click();
    window.URL.revokeObjectURL(link.href);
  }

  /**
   * Formatea fecha para el backend (dd/MM/yyyy)
   */
  formatearFecha(fecha: Date): string {
    const dia = fecha.getDate().toString().padStart(2, '0');
    const mes = (fecha.getMonth() + 1).toString().padStart(2, '0');
    const anio = fecha.getFullYear();
    return `${dia}/${mes}/${anio}`;
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Ha ocurrido un error desconocido';
    
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      errorMessage = error.error?.message || error.error || `Error ${error.status}: ${error.statusText}`;
    }
    
    console.error('Error en ReporteService:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
