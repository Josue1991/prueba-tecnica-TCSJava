package com.banco.banco_api.application.service;

import com.banco.banco_api.application.dto.MovimientosRequestDTO;
import com.banco.banco_api.application.dto.MovimientosResponseDTO;

import java.util.List;

public interface IMovimientoService {

    MovimientosResponseDTO registrarMovimiento(MovimientosRequestDTO request);

    List<MovimientosResponseDTO> obtenerMovimientosPorCuenta(Long cuentaId);
}
