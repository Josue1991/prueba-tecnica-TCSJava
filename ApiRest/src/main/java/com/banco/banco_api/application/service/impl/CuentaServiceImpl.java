package com.banco.banco_api.application.service.impl;

import com.banco.banco_api.application.dto.CuentaRequestDTO;
import com.banco.banco_api.application.dto.CuentaResponseDTO;
import com.banco.banco_api.application.dto.MovimientosRequestDTO;
import com.banco.banco_api.application.mapper.CuentaMapper;
import com.banco.banco_api.application.service.ICuentaService;
import com.banco.banco_api.application.service.IMovimientoService;
import com.banco.banco_api.domain.model.entity.Cliente;
import com.banco.banco_api.domain.model.entity.Cuenta;
import com.banco.banco_api.domain.model.enums.TipoMovimiento;
import com.banco.banco_api.domain.repository.IClienteRepository;
import com.banco.banco_api.domain.repository.ICuentaRepository;
import com.banco.banco_api.infrastructure.exception.BusinessException;
import com.banco.banco_api.infrastructure.exception.DuplicateResourceException;
import com.banco.banco_api.infrastructure.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class CuentaServiceImpl implements ICuentaService {

    private final ICuentaRepository cuentaRepository;
    private final IClienteRepository clienteRepository;
    private final IMovimientoService movimientoService;
    private final CuentaMapper cuentaMapper;

    public CuentaServiceImpl(ICuentaRepository cuentaRepository, 
                            IClienteRepository clienteRepository,
                            IMovimientoService movimientoService,
                            CuentaMapper cuentaMapper) {
        this.cuentaRepository = cuentaRepository;
        this.clienteRepository = clienteRepository;
        this.movimientoService = movimientoService;
        this.cuentaMapper = cuentaMapper;
    }

    @Override
    public CuentaResponseDTO crearCuenta(CuentaRequestDTO cuentaDTO) {
        if (cuentaRepository.existsByNumeroCuenta(cuentaDTO.getNumeroCuenta())) {
            throw new DuplicateResourceException(
                "Ya existe una cuenta con el número: " + cuentaDTO.getNumeroCuenta()
            );
        }

        Cliente cliente = clienteRepository.findById(cuentaDTO.getClienteId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Cliente no encontrado con ID: " + cuentaDTO.getClienteId()
            ));

        if (!cliente.getEstado()) {
            throw new BusinessException(
                "No se puede crear una cuenta para un cliente inactivo"
            );
        }

        Cuenta cuenta = cuentaMapper.toEntity(cuentaDTO, cliente);
        Cuenta cuentaGuardada = cuentaRepository.save(cuenta);
        
        return cuentaMapper.toResponseDTO(cuentaGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaResponseDTO obtenerCuentaPorId(Long id) {
        Cuenta cuenta = cuentaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Cuenta no encontrada con ID: " + id
            ));
        
        return cuentaMapper.toResponseDTO(cuenta);
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaResponseDTO obtenerCuentaPorNumero(String numeroCuenta) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Cuenta no encontrada con número: " + numeroCuenta
            ));
        
        return cuentaMapper.toResponseDTO(cuenta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResponseDTO> obtenerTodasLasCuentas() {
        List<Cuenta> cuentas = cuentaRepository.findAll();
        return cuentaMapper.toResponseDTOList(cuentas);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaResponseDTO> obtenerCuentasPorCliente(Long clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new ResourceNotFoundException(
                "Cliente no encontrado con ID: " + clienteId
            );
        }
        
        List<Cuenta> cuentas = cuentaRepository.findByClienteId(clienteId);
        return cuentaMapper.toResponseDTOList(cuentas);
    }

    @Override
    public CuentaResponseDTO depositar(String numeroCuenta, BigDecimal monto) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Cuenta no encontrada con número: " + numeroCuenta
            ));

        if (!cuenta.getEstado()) {
            throw new BusinessException("La cuenta está inactiva");
        }

        cuenta.depositar(monto);
        Cuenta cuentaActualizada = cuentaRepository.save(cuenta);
        
        return cuentaMapper.toResponseDTO(cuentaActualizada);
    }

    @Override
    public CuentaResponseDTO retirar(String numeroCuenta, BigDecimal monto) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Cuenta no encontrada con número: " + numeroCuenta
            ));

        if (!cuenta.getEstado()) {
            throw new BusinessException("La cuenta está inactiva");
        }

        cuenta.retirar(monto);
        Cuenta cuentaActualizada = cuentaRepository.save(cuenta);
        
        return cuentaMapper.toResponseDTO(cuentaActualizada);
    }

    @Override
    public void desactivarCuenta(Long id) {
        Cuenta cuenta = cuentaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Cuenta no encontrada con ID: " + id
            ));
        
        if (cuenta.getDeleted()) {
            throw new BusinessException("No se puede desactivar una cuenta eliminada");
        }
        
        if (!cuenta.getEstado()) {
            throw new BusinessException("La cuenta ya está desactivada");
        }
        
        // Crear movimiento de desactivación
        MovimientosRequestDTO movimientoRequest = new MovimientosRequestDTO(
            cuenta.getId(),
            TipoMovimiento.DESACTIVAR,
            null
        );
        
        movimientoService.registrarMovimiento(movimientoRequest);
    }

    @Override
    public void activarCuenta(Long id) {
        Cuenta cuenta = cuentaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Cuenta no encontrada con ID: " + id
            ));
        
        if (cuenta.getDeleted()) {
            throw new BusinessException("No se puede activar una cuenta eliminada");
        }
        
        if (cuenta.getEstado()) {
            throw new BusinessException("La cuenta ya está activa");
        }
        
        // Crear movimiento de activación
        MovimientosRequestDTO movimientoRequest = new MovimientosRequestDTO(
            cuenta.getId(),
            TipoMovimiento.ACTIVAR,
            null
        );
        
        movimientoService.registrarMovimiento(movimientoRequest);
    }
}
