package com.banco.banco_api.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.banco.banco_api.application.dto.MovimientosRequestDTO;
import com.banco.banco_api.application.dto.MovimientosResponseDTO;
import com.banco.banco_api.application.service.IMovimientoService;
import com.banco.banco_api.domain.model.entity.Cuenta;
import com.banco.banco_api.domain.model.entity.Movimiento;
import com.banco.banco_api.domain.model.enums.TipoMovimiento;
import com.banco.banco_api.domain.repository.ICuentaRepository;
import com.banco.banco_api.domain.repository.IMovimientoRepository;
import com.banco.banco_api.infrastructure.exception.BusinessException;
import com.banco.banco_api.infrastructure.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovimientoService implements IMovimientoService {

    private final ICuentaRepository cuentaRepository;
    private final IMovimientoRepository movimientoRepository;

    @Override
    @Transactional
    @SuppressWarnings({"null", "incomplete-switch"})
    public MovimientosResponseDTO registrarMovimiento(MovimientosRequestDTO request) {
        
        Cuenta cuenta = cuentaRepository.findById(request.cuentaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con ID: " + request.cuentaId()));

        // Guardar saldo anterior
        BigDecimal saldoAnterior = cuenta.getSaldoActual();

        // Validar estado según tipo de movimiento
        if (!cuenta.getEstado() && request.tipoMovimiento() != TipoMovimiento.ACTIVAR) {
            throw new BusinessException("La cuenta está inactiva. Solo se puede activar.");
        }

        BigDecimal valorMovimiento;

        // Aplicar la operación según el tipo de movimiento
        switch (request.tipoMovimiento()) {
            case DEPOSITO:
                if (request.valor() == null || request.valor().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessException("El valor debe ser mayor a cero para depósitos");
                }
                try {
                    cuenta.depositar(request.valor());
                } catch (IllegalArgumentException e) {
                    throw new BusinessException(e.getMessage());
                }
                valorMovimiento = request.valor();
                break;
            case RETIRO:
                if (request.valor() == null || request.valor().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessException("El valor debe ser mayor a cero para retiros");
                }
                try {
                    cuenta.retirar(request.valor());
                } catch (IllegalArgumentException e) {
                    throw new BusinessException(e.getMessage());
                }
                valorMovimiento = request.valor();
                break;
            case ACTIVAR:
                cuenta.setEstado(true);
                valorMovimiento = BigDecimal.ZERO;
                break;
            case DESACTIVAR:
                cuenta.setEstado(false);
                valorMovimiento = BigDecimal.ZERO;
                break;
            default:
                throw new BusinessException("Tipo de movimiento no válido: " + request.tipoMovimiento());
        }

        BigDecimal nuevoSaldo = cuenta.getSaldoActual();

        // Guardar la cuenta actualizada primero
        Cuenta cuentaActualizada = cuentaRepository.save(cuenta);

        // Crear el movimiento
        Movimiento movimiento = new Movimiento();
        movimiento.setCuenta(cuentaActualizada);
        movimiento.setTipoMovimiento(request.tipoMovimiento());
        movimiento.setValor(valorMovimiento);
        movimiento.setSaldo(nuevoSaldo);
        movimiento.setFechaMovimiento(LocalDateTime.now());

        // Guardar el movimiento
        Movimiento movimientoGuardado = movimientoRepository.save(movimiento);
        
        return mapToDTO(movimientoGuardado, saldoAnterior);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MovimientosResponseDTO> obtenerTodosLosMovimientos() {
        List<Movimiento> movimientos = movimientoRepository.findAllOrdered();
        return convertirListaMovimientos(movimientos);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MovimientosResponseDTO> obtenerMovimientosPorCuenta(Long cuentaId) {
        List<Movimiento> movimientos = movimientoRepository.findByCuentaId(cuentaId);
        return convertirListaMovimientos(movimientos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientosResponseDTO> obtenerMovimientosPorCliente(Long clienteId) {
        List<Movimiento> movimientos = movimientoRepository.findByCuentaClienteId(clienteId);
        return convertirListaMovimientos(movimientos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientosResponseDTO> obtenerMovimientosPorCuentaYFechas(Long cuentaId, LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);
        List<Movimiento> movimientos = movimientoRepository.findByCuentaIdAndFechaMovimientoBetween(cuentaId, inicio, fin);
        return convertirListaMovimientos(movimientos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientosResponseDTO> obtenerMovimientosPorClienteYFechas(Long clienteId, LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);
        List<Movimiento> movimientos = movimientoRepository.findByClienteIdAndFechaMovimientoBetween(clienteId, inicio, fin);
        return convertirListaMovimientos(movimientos);
    }

    // Método helper para convertir lista de movimientos con cálculo de saldo anterior
    private List<MovimientosResponseDTO> convertirListaMovimientos(List<Movimiento> movimientos) {
        return movimientos.stream()
            .map(m -> {
                // Calcular saldo anterior: saldo actual - valor del movimiento
                BigDecimal saldoAnterior = calcularSaldoAnterior(m);
                return mapToDTO(m, saldoAnterior);
            })
            .collect(Collectors.toList());
    }

    // Calcular el saldo anterior basado en el tipo de movimiento
    private BigDecimal calcularSaldoAnterior(Movimiento movimiento) {
        BigDecimal saldoActual = movimiento.getSaldo();
        BigDecimal valor = movimiento.getValor();
        
        switch (movimiento.getTipoMovimiento()) {
            case DEPOSITO:
                return saldoActual.subtract(valor);
            case RETIRO:
                return saldoActual.add(valor);
            case ACTIVAR:
            case DESACTIVAR:
            default:
                return saldoActual;
        }
    }

    // Método helper para mapear Movimiento a DTO
    private MovimientosResponseDTO mapToDTO(Movimiento movimiento, BigDecimal saldoAnterior) {
        Cuenta cuenta = movimiento.getCuenta();
        String nombreCliente = cuenta.getCliente().getNombre();
        
        return new MovimientosResponseDTO(
            movimiento.getId(),
            cuenta.getId(),
            cuenta.getNumeroCuenta(),
            nombreCliente,
            movimiento.getTipoMovimiento().name(),
            movimiento.getValor(),
            saldoAnterior,
            movimiento.getSaldo(),
            movimiento.getFechaMovimiento(),
            movimiento.getCreatedAt()
        );
    }
}
