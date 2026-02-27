package com.banco.banco_api.application.service.impl;

import com.banco.banco_api.application.dto.MovimientosResponseDTO;
import com.banco.banco_api.application.dto.ReporteMovimientosClienteDTO;
import com.banco.banco_api.application.dto.ReporteMovimientosCuentaDTO;
import com.banco.banco_api.application.dto.ReporteCuentasDTO;
import com.banco.banco_api.application.service.IReporteService;
import com.banco.banco_api.domain.model.entity.Cliente;
import com.banco.banco_api.domain.model.entity.Cuenta;
import com.banco.banco_api.domain.model.entity.Movimiento;
import com.banco.banco_api.domain.repository.IClienteRepository;
import com.banco.banco_api.domain.repository.ICuentaRepository;
import com.banco.banco_api.domain.repository.IMovimientoRepository;
import com.banco.banco_api.infrastructure.exception.BusinessException;
import com.banco.banco_api.infrastructure.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReporteServiceImpl implements IReporteService {

    private final IMovimientoRepository movimientoRepository;
    private final ICuentaRepository cuentaRepository;
    private final IClienteRepository clienteRepository;

    public ReporteServiceImpl(IMovimientoRepository movimientoRepository,
                             ICuentaRepository cuentaRepository,
                             IClienteRepository clienteRepository) {
        this.movimientoRepository = movimientoRepository;
        this.cuentaRepository = cuentaRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    @SuppressWarnings("null")
    public ReporteMovimientosClienteDTO generarReporteMovimientosPorCliente(
            Long clienteId, LocalDate fechaInicio, LocalDate fechaFin) {
        
        // Validar fechas
        validarRangoFechas(fechaInicio, fechaFin);
        
        // Validar que el cliente existe
        Cliente cliente = clienteRepository.findById(clienteId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Cliente no encontrado con ID: " + clienteId
            ));
        
        // Convertir fechas a LocalDateTime
        LocalDateTime fechaInicioDateTime = fechaInicio.atStartOfDay();
        LocalDateTime fechaFinDateTime = fechaFin.atTime(LocalTime.MAX);
        
        // Obtener movimientos del cliente en el rango de fechas
        List<Movimiento> movimientos = movimientoRepository
            .findByClienteIdAndFechaMovimientoBetween(clienteId, fechaInicioDateTime, fechaFinDateTime);
        
        // Agrupar movimientos por cuenta
        Map<Long, List<Movimiento>> movimientosPorCuenta = movimientos.stream()
            .collect(Collectors.groupingBy(m -> m.getCuenta().getId()));
        
        // Construir lista de cuentas con movimientos
        List<ReporteMovimientosClienteDTO.CuentaConMovimientos> cuentasConMovimientos = 
            movimientosPorCuenta.entrySet().stream()
                .map(entry -> {
                    Long cuentaId = entry.getKey();
                    List<Movimiento> movimientosCuenta = entry.getValue();
                    Cuenta cuenta = movimientosCuenta.get(0).getCuenta();
                    
                    List<MovimientosResponseDTO> movimientosDTO = movimientosCuenta.stream()
                        .map(this::convertirMovimientoADTO)
                        .collect(Collectors.toList());
                    
                    return new ReporteMovimientosClienteDTO.CuentaConMovimientos(
                        cuentaId,
                        cuenta.getNumeroCuenta(),
                        cuenta.getTipoCuenta(),
                        cuenta.getEstado(),
                        movimientosDTO
                    );
                })
                .collect(Collectors.toList());
        
        return new ReporteMovimientosClienteDTO(
            clienteId,
            cliente.getNombre(),
            cliente.getIdentificacion(),
            fechaInicio,
            fechaFin,
            movimientos.size(),
            cuentasConMovimientos
        );
    }

    @Override
    @SuppressWarnings("null")
    public ReporteMovimientosCuentaDTO generarReporteMovimientosPorCuenta(
            Long cuentaId, LocalDate fechaInicio, LocalDate fechaFin) {
        
        // Validar fechas
        validarRangoFechas(fechaInicio, fechaFin);
        
        // Validar que la cuenta existe
        Cuenta cuenta = cuentaRepository.findById(cuentaId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Cuenta no encontrada con ID: " + cuentaId
            ));
        
        // Convertir fechas a LocalDateTime
        LocalDateTime fechaInicioDateTime = fechaInicio.atStartOfDay();
        LocalDateTime fechaFinDateTime = fechaFin.atTime(LocalTime.MAX);
        
        // Obtener movimientos de la cuenta en el rango de fechas
        List<Movimiento> movimientos = movimientoRepository
            .findByCuentaIdAndFechaMovimientoBetween(cuentaId, fechaInicioDateTime, fechaFinDateTime);
        
        // Convertir movimientos a DTO
        List<MovimientosResponseDTO> movimientosDTO = movimientos.stream()
            .map(this::convertirMovimientoADTO)
            .collect(Collectors.toList());
        
        return new ReporteMovimientosCuentaDTO(
            cuentaId,
            cuenta.getNumeroCuenta(),
            cuenta.getTipoCuenta(),
            cuenta.getCliente().getNombre(),
            cuenta.getSaldoInicial(),
            cuenta.getSaldoActual(),
            fechaInicio,
            fechaFin,
            movimientos.size(),
            movimientosDTO
        );
    }

    @Override
    public ReporteCuentasDTO generarReporteCuentas(LocalDate fechaInicio, LocalDate fechaFin) {
        
        // Validar fechas
        validarRangoFechas(fechaInicio, fechaFin);
        
        // Convertir fechas a LocalDateTime
        LocalDateTime fechaInicioDateTime = fechaInicio.atStartOfDay();
        LocalDateTime fechaFinDateTime = fechaFin.atTime(LocalTime.MAX);
        
        // Obtener todos los movimientos en el rango de fechas
        List<Movimiento> movimientos = movimientoRepository
            .findByFechaMovimientoBetween(fechaInicioDateTime, fechaFinDateTime);
        
        // Agrupar movimientos por cuenta
        Map<Long, List<Movimiento>> movimientosPorCuenta = movimientos.stream()
            .collect(Collectors.groupingBy(m -> m.getCuenta().getId()));
        
        // Si no hay movimientos, incluir todas las cuentas activas sin movimientos
        List<Cuenta> todasLasCuentas = cuentaRepository.findAll();
        
        List<ReporteCuentasDTO.CuentaResumen> cuentasResumen = todasLasCuentas.stream()
            .filter(c -> !c.getDeleted()) // Solo cuentas no eliminadas
            .map(cuenta -> {
                List<Movimiento> movimientosCuenta = movimientosPorCuenta.getOrDefault(
                    cuenta.getId(), 
                    new ArrayList<>()
                );
                
                return new ReporteCuentasDTO.CuentaResumen(
                    cuenta.getId(),
                    cuenta.getNumeroCuenta(),
                    cuenta.getTipoCuenta(),
                    cuenta.getCliente().getNombre(),
                    cuenta.getSaldoInicial(),
                    cuenta.getSaldoActual(),
                    cuenta.getEstado(),
                    movimientosCuenta.size(),
                    cuenta.getCreatedAt()
                );
            })
            .collect(Collectors.toList());
        
        return new ReporteCuentasDTO(
            fechaInicio,
            fechaFin,
            cuentasResumen.size(),
            cuentasResumen
        );
    }
    
    // Método helper para convertir Movimiento a DTO
    private MovimientosResponseDTO convertirMovimientoADTO(Movimiento movimiento) {
        return new MovimientosResponseDTO(
            movimiento.getId(),
            movimiento.getCuenta().getId(),
            movimiento.getTipoMovimiento().name(),
            movimiento.getValor(),
            movimiento.getSaldo(),
            movimiento.getFechaMovimiento(),
            movimiento.getCreatedAt()
        );
    }
    
    // Método helper para validar rango de fechas
    private void validarRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio.isAfter(fechaFin)) {
            throw new BusinessException(
                "La fecha inicial no puede ser posterior a la fecha final"
            );
        }
        
        if (fechaInicio.isAfter(LocalDate.now())) {
            throw new BusinessException(
                "La fecha inicial no puede ser futura"
            );
        }
    }
}
