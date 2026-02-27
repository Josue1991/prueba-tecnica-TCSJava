export interface Cliente {
  id?: number;
  nombre: string;
  genero: string;
  edad: number;
  identificacion: string;
  direccion: string;
  telefono?: string;
  estado?: boolean;
  createdAt?: Date;
}

export interface ClienteRequest {
  nombre: string;
  genero: string;
  edad: number;
  identificacion: string;
  direccion: string;
  telefono?: string;
  password: string;
}

export interface ClienteActivacionValidacion {
  clienteActivo: boolean;
  cuentas: CuentaActivacion[];
  mensaje: string;
}

export interface CuentaActivacion {
  id: number;
  numeroCuenta: string;
  tipoCuenta: string;
  estado: boolean;
  deleted: boolean;
}

export interface ClienteActivacionRequest {
  cuentasIds: number[];
}
