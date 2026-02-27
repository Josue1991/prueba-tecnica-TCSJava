import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { CuentaService } from './cuenta.service';
import { Cuenta, CuentaRequest, TipoCuenta } from '../models/cuenta.model';

describe('CuentaService', () => {
  let service: CuentaService;
  let httpMock: HttpTestingController;
  const API = 'http://localhost:8080/api/cuentas';

  const mockCuenta: Cuenta = {
    id: 1,
    numeroCuenta: '5520366226',
    tipoCuenta: TipoCuenta.AHORRO,
    saldoInicial: 1000,
    saldoActual: 1000,
    clienteId: 1,
    nombreCliente: 'Juan Pérez',
    estado: true
  };

  const mockRequest: CuentaRequest = {
    numeroCuenta: '5520366226',
    tipoCuenta: TipoCuenta.AHORRO,
    clienteId: 1
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        CuentaService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(CuentaService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  // ─── crearCuenta ─────────────────────────────────────────────────────────────

  it('crearCuenta: realiza POST a /cuentas con el body correcto', () => {
    service.crearCuenta(mockRequest).subscribe(res => {
      expect(res.numeroCuenta).toBe('5520366226');
      expect(res.estado).toBe(true);
    });

    const req = httpMock.expectOne(API);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockRequest);
    req.flush(mockCuenta);
  });

  // ─── obtenerCuentaPorId ──────────────────────────────────────────────────────

  it('obtenerCuentaPorId: realiza GET a /cuentas/:id', () => {
    service.obtenerCuentaPorId(1).subscribe(res => {
      expect(res.id).toBe(1);
    });

    const req = httpMock.expectOne(`${API}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(mockCuenta);
  });

  // ─── obtenerCuentaPorNumero ──────────────────────────────────────────────────

  it('obtenerCuentaPorNumero: realiza GET a /cuentas/numero/:numeroCuenta', () => {
    service.obtenerCuentaPorNumero('5520366226').subscribe(res => {
      expect(res.numeroCuenta).toBe('5520366226');
    });

    const req = httpMock.expectOne(`${API}/numero/5520366226`);
    expect(req.request.method).toBe('GET');
    req.flush(mockCuenta);
  });

  // ─── obtenerTodasLasCuentas ──────────────────────────────────────────────────

  it('obtenerTodasLasCuentas: realiza GET a /cuentas y devuelve lista', () => {
    service.obtenerTodasLasCuentas().subscribe(res => {
      expect(res.length).toBe(1);
    });

    const req = httpMock.expectOne(API);
    expect(req.request.method).toBe('GET');
    req.flush([mockCuenta]);
  });

  // ─── obtenerCuentasPorCliente ────────────────────────────────────────────────

  it('obtenerCuentasPorCliente: realiza GET a /cuentas/cliente/:clienteId', () => {
    service.obtenerCuentasPorCliente(1).subscribe(res => {
      expect(res).toEqual([mockCuenta]);
    });

    const req = httpMock.expectOne(`${API}/cliente/1`);
    expect(req.request.method).toBe('GET');
    req.flush([mockCuenta]);
  });

  // ─── depositar ───────────────────────────────────────────────────────────────

  it('depositar: realiza POST a /cuentas/:numero/depositar con el monto en query param', () => {
    service.depositar('5520366226', 200).subscribe(res => {
      expect(res.saldoActual).toBe(1200);
    });

    const req = httpMock.expectOne(r =>
      r.url === `${API}/5520366226/depositar` && r.params.get('monto') === '200'
    );
    expect(req.request.method).toBe('POST');
    req.flush({ ...mockCuenta, saldoActual: 1200 });
  });

  // ─── retirar ─────────────────────────────────────────────────────────────────

  it('retirar: realiza POST a /cuentas/:numero/retirar con el monto en query param', () => {
    service.retirar('5520366226', 100).subscribe();

    const req = httpMock.expectOne(r =>
      r.url === `${API}/5520366226/retirar` && r.params.get('monto') === '100'
    );
    expect(req.request.method).toBe('POST');
    req.flush({ ...mockCuenta, saldoActual: 900 });
  });

  // ─── desactivarCuenta ────────────────────────────────────────────────────────

  it('desactivarCuenta: realiza PATCH a /cuentas/:id/desactivar', () => {
    service.desactivarCuenta(1).subscribe();

    const req = httpMock.expectOne(`${API}/1/desactivar`);
    expect(req.request.method).toBe('PATCH');
    req.flush(null);
  });

  // ─── activarCuenta ───────────────────────────────────────────────────────────

  it('activarCuenta: realiza PATCH a /cuentas/:id/activar', () => {
    service.activarCuenta(1).subscribe();

    const req = httpMock.expectOne(`${API}/1/activar`);
    expect(req.request.method).toBe('PATCH');
    req.flush(null);
  });

  // ─── manejo de errores ───────────────────────────────────────────────────────

  it('crearCuenta: propaga error del backend si el número de cuenta existe', () => {
    let errorCapturado: Error | undefined;

    service.crearCuenta(mockRequest).subscribe({
      error: (err: Error) => { errorCapturado = err; }
    });

    const req = httpMock.expectOne(API);
    req.flush({ message: 'Ya existe una cuenta con el número: 5520366226' },
               { status: 409, statusText: 'Conflict' });

    expect(errorCapturado).toBeDefined();
    expect(errorCapturado?.message).toContain('5520366226');
  });

  it('activarCuenta: propaga error si la cuenta ya está activa (400)', () => {
    let errorCapturado: Error | undefined;

    service.activarCuenta(1).subscribe({
      error: (err: Error) => { errorCapturado = err; }
    });

    const req = httpMock.expectOne(`${API}/1/activar`);
    req.flush({ message: 'La cuenta ya está activa' },
               { status: 400, statusText: 'Bad Request' });

    expect(errorCapturado).toBeDefined();
    expect(errorCapturado?.message).toContain('activa');
  });
});
