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
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovimientoService implements IMovimientoService {

    private final ICuentaRepository cuentaRepository;
    private final IMovimientoRepository movimientoRepository;

    @Override
    @Transactional
    public MovimientosResponseDTO registrarMovimiento(MovimientosRequestDTO request) {
        
        Cuenta cuenta = cuentaRepository.findById(request.cuentaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con ID: " + request.cuentaId()));

        // Validar cuenta eliminada
        if (cuenta.getDeleted()) {
            throw new BusinessException("La cuenta está eliminada y no puede realizar operaciones");
        }

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
        
        return new MovimientosResponseDTO(
            movimientoGuardado.getId(),
            movimientoGuardado.getCuenta().getId(),
            movimientoGuardado.getTipoMovimiento().name(),
            movimientoGuardado.getValor(),
            movimientoGuardado.getSaldo(),
            movimientoGuardado.getFechaMovimiento(),
            movimientoGuardado.getCreatedAt()
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MovimientosResponseDTO> obtenerMovimientosPorCuenta(Long cuentaId) {
        List<Movimiento> movimientos = movimientoRepository.findByCuentaId(cuentaId);
        
        return movimientos.stream()
            .map(m -> new MovimientosResponseDTO(
                m.getId(),
                m.getCuenta().getId(),
                m.getTipoMovimiento().name(),
                m.getValor(),
                m.getSaldo(),
                m.getFechaMovimiento(),
                m.getCreatedAt()
            ))
            .collect(Collectors.toList());
    }
}
