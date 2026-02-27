package com.banco.banco_api.application.service;

import com.banco.banco_api.application.dto.CuentaRequestDTO;
import com.banco.banco_api.application.dto.CuentaResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public interface ICuentaService {

    CuentaResponseDTO crearCuenta(CuentaRequestDTO cuentaDTO);

    CuentaResponseDTO obtenerCuentaPorId(Long id);

    CuentaResponseDTO obtenerCuentaPorNumero(String numeroCuenta);

    List<CuentaResponseDTO> obtenerTodasLasCuentas();

    List<CuentaResponseDTO> obtenerCuentasPorCliente(Long clienteId);

    CuentaResponseDTO depositar(String numeroCuenta, BigDecimal monto);

    CuentaResponseDTO retirar(String numeroCuenta, BigDecimal monto);

    void desactivarCuenta(Long id);

    void activarCuenta(Long id);
}
