import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { 
  Cliente, 
  ClienteRequest, 
  ClienteActivacionValidacion, 
  ClienteActivacionRequest 
} from '../models/cliente.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ClienteService {
  private readonly API_URL = `${environment.apiUrl}/clientes`;

  constructor(private http: HttpClient) { }

  crearCliente(cliente: ClienteRequest): Observable<Cliente> {
    return this.http.post<Cliente>(this.API_URL, cliente)
      .pipe(catchError(this.handleError));
  }

  obtenerClientePorId(id: number): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.API_URL}/${id}`)
      .pipe(catchError(this.handleError));
  }

  obtenerClientePorDocumento(documentoIdentidad: string): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.API_URL}/documento/${documentoIdentidad}`)
      .pipe(catchError(this.handleError));
  }

  obtenerTodosLosClientes(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(this.API_URL)
      .pipe(catchError(this.handleError));
  }

  obtenerClientesActivos(): Observable<Cliente[]> {
    return this.http.get<Cliente[]>(`${this.API_URL}/activos`)
      .pipe(catchError(this.handleError));
  }

  actualizarCliente(id: number, cliente: ClienteRequest): Observable<Cliente> {
    return this.http.put<Cliente>(`${this.API_URL}/${id}`, cliente)
      .pipe(catchError(this.handleError));
  }

  // Desactiva el cliente y TODAS sus cuentas activas automáticamente
  desactivarCliente(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API_URL}/${id}/desactivar`, {})
      .pipe(catchError(this.handleError));
  }

  // Activa solo el cliente (sin afectar cuentas)
  activarCliente(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API_URL}/${id}/activar`, {})
      .pipe(catchError(this.handleError));
  }

  // Valida qué cuentas tiene el cliente para activarlas selectivamente
  validarActivacion(id: number): Observable<ClienteActivacionValidacion> {
    return this.http.get<ClienteActivacionValidacion>(`${this.API_URL}/${id}/validar-activacion`)
      .pipe(catchError(this.handleError));
  }

  // Activa el cliente y las cuentas seleccionadas
  activarClienteConCuentas(id: number, request: ClienteActivacionRequest): Observable<void> {
    return this.http.patch<void>(`${this.API_URL}/${id}/activar-con-cuentas`, request)
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Ha ocurrido un error desconocido';
    
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      errorMessage = error.error?.message || `Error ${error.status}: ${error.statusText}`;
    }
    
    console.error('Error en ClienteService:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
