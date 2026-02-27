import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ClienteService } from './cliente.service';
import {
  Cliente,
  ClienteRequest,
  ClienteActivacionValidacion,
  ClienteActivacionRequest
} from '../models/cliente.model';

describe('ClienteService', () => {
  let service: ClienteService;
  let httpMock: HttpTestingController;
  const API = 'http://localhost:8080/api/clientes';

  const mockCliente: Cliente = {
    id: 1,
    nombre: 'Juan Pérez',
    genero: 'Masculino',
    edad: 30,
    identificacion: '1234567890',
    direccion: 'Calle 1',
    telefono: '0987654321',
    estado: true
  };

  const mockRequest: ClienteRequest = {
    nombre: 'Juan Pérez',
    genero: 'Masculino',
    edad: 30,
    identificacion: '1234567890',
    direccion: 'Calle 1',
    telefono: '0987654321',
    password: 'pw123456'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ClienteService]
    });
    service = TestBed.inject(ClienteService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  // ─── crearCliente ────────────────────────────────────────────────────────────

  it('crearCliente: realiza POST a /clientes con el body correcto', () => {
    service.crearCliente(mockRequest).subscribe(res => {
      expect(res.nombre).toBe('Juan Pérez');
    });

    const req = httpMock.expectOne(API);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockRequest);
    req.flush(mockCliente);
  });

  // ─── obtenerClientePorId ─────────────────────────────────────────────────────

  it('obtenerClientePorId: realiza GET a /clientes/:id', () => {
    service.obtenerClientePorId(1).subscribe(res => {
      expect(res.id).toBe(1);
    });

    const req = httpMock.expectOne(`${API}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockCliente);
  });

  // ─── obtenerTodosLosClientes ─────────────────────────────────────────────────

  it('obtenerTodosLosClientes: realiza GET a /clientes y devuelve lista', () => {
    service.obtenerTodosLosClientes().subscribe(res => {
      expect(res.length).toBe(1);
      expect(res[0].nombre).toBe('Juan Pérez');
    });

    const req = httpMock.expectOne(API);
    expect(req.request.method).toBe('GET');
    req.flush([mockCliente]);
  });

  // ─── obtenerClientesActivos ──────────────────────────────────────────────────

  it('obtenerClientesActivos: realiza GET a /clientes/activos', () => {
    service.obtenerClientesActivos().subscribe(res => {
      expect(res).toEqual([mockCliente]);
    });

    const req = httpMock.expectOne(`${API}/activos`);
    expect(req.request.method).toBe('GET');
    req.flush([mockCliente]);
  });

  // ─── actualizarCliente ───────────────────────────────────────────────────────

  it('actualizarCliente: realiza PUT a /clientes/:id con el body correcto', () => {
    service.actualizarCliente(1, mockRequest).subscribe(res => {
      expect(res.nombre).toBe('Juan Pérez');
    });

    const req = httpMock.expectOne(`${API}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(mockRequest);
    req.flush(mockCliente);
  });

  // ─── desactivarCliente ───────────────────────────────────────────────────────

  it('desactivarCliente: realiza PATCH a /clientes/:id/desactivar', () => {
    service.desactivarCliente(1).subscribe();

    const req = httpMock.expectOne(`${API}/1/desactivar`);
    expect(req.request.method).toBe('PATCH');
    req.flush(null);
  });

  // ─── activarCliente ──────────────────────────────────────────────────────────

  it('activarCliente: realiza PATCH a /clientes/:id/activar', () => {
    service.activarCliente(1).subscribe();

    const req = httpMock.expectOne(`${API}/1/activar`);
    expect(req.request.method).toBe('PATCH');
    req.flush(null);
  });

  // ─── validarActivacion ───────────────────────────────────────────────────────

  it('validarActivacion: realiza GET a /clientes/:id/validar-activacion', () => {
    const mockValidacion: ClienteActivacionValidacion = {
      clienteActivo: false,
      cuentas: [
        { id: 1, numeroCuenta: '111', tipoCuenta: 'AHORRO', estado: false, deleted: false }
      ],
      mensaje: 'El cliente está inactivo.'
    };

    service.validarActivacion(1).subscribe(res => {
      expect(res.clienteActivo).toBeFalse();
      expect(res.cuentas.length).toBe(1);
    });

    const req = httpMock.expectOne(`${API}/1/validar-activacion`);
    expect(req.request.method).toBe('GET');
    req.flush(mockValidacion);
  });

  // ─── activarClienteConCuentas ────────────────────────────────────────────────

  it('activarClienteConCuentas: realiza PATCH a /clientes/:id/activar-con-cuentas con cuentasIds', () => {
    const activacionRequest: ClienteActivacionRequest = { cuentasIds: [1, 2] };

    service.activarClienteConCuentas(1, activacionRequest).subscribe();

    const req = httpMock.expectOne(`${API}/1/activar-con-cuentas`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ cuentasIds: [1, 2] });
    req.flush(null);
  });

  // ─── manejo de errores ───────────────────────────────────────────────────────

  it('crearCliente: propaga error del backend al suscriptor', () => {
    let errorCapturado: Error | undefined;

    service.crearCliente(mockRequest).subscribe({
      error: (err: Error) => { errorCapturado = err; }
    });

    const req = httpMock.expectOne(API);
    req.flush({ message: 'Identificación duplicada' }, { status: 409, statusText: 'Conflict' });

    expect(errorCapturado).toBeDefined();
    expect(errorCapturado?.message).toContain('Identificación duplicada');
  });
});
