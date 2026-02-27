package com.banco.banco_api.application.service;

import com.banco.banco_api.application.dto.MovimientosRequestDTO;
import com.banco.banco_api.application.dto.MovimientosResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface IMovimientoService {

    MovimientosResponseDTO registrarMovimiento(MovimientosRequestDTO request);

    List<MovimientosResponseDTO> obtenerTodosLosMovimientos();

    List<MovimientosResponseDTO> obtenerMovimientosPorCuenta(Long cuentaId);

    List<MovimientosResponseDTO> obtenerMovimientosPorCliente(Long clienteId);

    List<MovimientosResponseDTO> obtenerMovimientosPorCuentaYFechas(Long cuentaId, LocalDate fechaInicio, LocalDate fechaFin);

    List<MovimientosResponseDTO> obtenerMovimientosPorClienteYFechas(Long clienteId, LocalDate fechaInicio, LocalDate fechaFin);
}
