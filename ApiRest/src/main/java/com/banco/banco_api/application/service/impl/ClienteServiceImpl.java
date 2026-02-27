package com.banco.banco_api.application.service.impl;

import com.banco.banco_api.application.dto.ClienteRequestDTO;
import com.banco.banco_api.application.dto.ClienteResponseDTO;
import com.banco.banco_api.application.dto.ClienteActivacionRequestDTO;
import com.banco.banco_api.application.dto.ClienteActivacionResponseDTO;
import com.banco.banco_api.application.dto.MovimientosRequestDTO;
import com.banco.banco_api.application.mapper.ClienteMapper;
import com.banco.banco_api.application.service.IClienteService;
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

import java.util.List;

@Service
@Transactional
public class ClienteServiceImpl implements IClienteService {

    private final IClienteRepository clienteRepository;
    private final ICuentaRepository cuentaRepository;
    private final IMovimientoService movimientoService;
    private final ClienteMapper clienteMapper;

    public ClienteServiceImpl(IClienteRepository clienteRepository, 
                             ICuentaRepository cuentaRepository,
                             IMovimientoService movimientoService,
                             ClienteMapper clienteMapper) {
        this.clienteRepository = clienteRepository;
        this.cuentaRepository = cuentaRepository;
        this.movimientoService = movimientoService;
        this.clienteMapper = clienteMapper;
    }

    @Override
    @SuppressWarnings("null")
    public ClienteResponseDTO crearCliente(ClienteRequestDTO clienteDTO) {
        if (clienteRepository.existsByIdentificacion(clienteDTO.getIdentificacion())) {
            throw new DuplicateResourceException(
                "Ya existe un cliente con la identificación: " + clienteDTO.getIdentificacion()
            );
        }

        Cliente cliente = clienteMapper.toEntity(clienteDTO);
        Cliente clienteGuardado = clienteRepository.save(cliente);
        
        return clienteMapper.toResponseDTO(clienteGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public ClienteResponseDTO obtenerClientePorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Cliente no encontrado con ID: " + id
            ));
        
        return clienteMapper.toResponseDTO(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerClientePorDocumento(String identificacion) {
        Cliente cliente = clienteRepository.findByIdentificacion(identificacion)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Cliente no encontrado con identificación: " + identificacion
            ));
        
        return clienteMapper.toResponseDTO(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> obtenerTodosLosClientes() {
        List<Cliente> clientes = clienteRepository.findAll();
        return clienteMapper.toResponseDTOList(clientes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> obtenerClientesActivos() {
        List<Cliente> clientes = clienteRepository.findByEstadoTrue();
        return clienteMapper.toResponseDTOList(clientes);
    }

    @Override
    @SuppressWarnings("null")
    public ClienteResponseDTO actualizarCliente(Long id, ClienteRequestDTO clienteDTO) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Cliente no encontrado con ID: " + id
            ));

        // Validar identificación duplicada si cambió
        if (!cliente.getIdentificacion().equals(clienteDTO.getIdentificacion()) && 
            clienteRepository.existsByIdentificacion(clienteDTO.getIdentificacion())) {
            throw new DuplicateResourceException(
                "Ya existe un cliente con la identificación: " + clienteDTO.getIdentificacion()
            );
        }

        clienteMapper.updateEntityFromDTO(clienteDTO, cliente);
        Cliente clienteActualizado = clienteRepository.save(cliente);
        
        return clienteMapper.toResponseDTO(clienteActualizado);
    }

    @Override
    @SuppressWarnings("null")
    public void desactivarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Cliente no encontrado con ID: " + id
            ));
        
        // Obtener todas las cuentas activas del cliente
        List<Cuenta> cuentasActivas = cuentaRepository.findByClienteId(id).stream()
            .filter(c -> c.getEstado())
            .toList();
        
        // Desactivar cada cuenta activa y generar movimiento
        for (Cuenta cuenta : cuentasActivas) {
            // Crear movimiento de desactivación
            MovimientosRequestDTO movimientoRequest = new MovimientosRequestDTO(
                cuenta.getId(),
                TipoMovimiento.DESACTIVAR,
                null
            );
            
            movimientoService.registrarMovimiento(movimientoRequest);
        }
        
        // Desactivar el cliente
        cliente.setEstado(false);
        clienteRepository.save(cliente);
    }

    @Override
    @SuppressWarnings("null")
    public void activarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Cliente no encontrado con ID: " + id
            ));
        
        cliente.setEstado(true);
        clienteRepository.save(cliente);
    }
    
    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public ClienteActivacionResponseDTO validarActivacionCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Cliente no encontrado con ID: " + id
            ));
        
        // Obtener todas las cuentas del cliente (activas e inactivas)
        List<Cuenta> cuentas = cuentaRepository.findByClienteId(id);
        
        List<ClienteActivacionResponseDTO.CuentaInfo> cuentasInfo = cuentas.stream()
            .map(c -> new ClienteActivacionResponseDTO.CuentaInfo(
                c.getId(),
                c.getNumeroCuenta(),
                c.getTipoCuenta(),
                c.getEstado(),
                false
            ))
            .toList();
        
        String mensaje = cliente.getEstado()
            ? "El cliente ya está activo. Puede seleccionar cuentas adicionales para activar."
            : "El cliente está inactivo. Seleccione las cuentas que desea activar.";
        
        return new ClienteActivacionResponseDTO(cliente.getEstado(), cuentasInfo, mensaje);
    }
    
    @Override
    @SuppressWarnings("null")
    public void activarClienteConCuentas(Long id, ClienteActivacionRequestDTO request) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Cliente no encontrado con ID: " + id
            ));
        
        // Validar que las cuentas pertenecen al cliente
        List<Cuenta> todasLasCuentas = cuentaRepository.findByClienteId(id);
        List<Long> cuentasDelCliente = todasLasCuentas.stream()
            .map(Cuenta::getId)
            .toList();
        
        for (Long cuentaId : request.cuentasIds()) {
            if (!cuentasDelCliente.contains(cuentaId)) {
                throw new BusinessException("La cuenta con ID " + cuentaId + " no pertenece al cliente");
            }
        }
        
        // Activar las cuentas seleccionadas y generar movimientos
        for (Long cuentaId : request.cuentasIds()) {
            Cuenta cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con ID: " + cuentaId));
            
            // Solo activar si está inactiva
            if (!cuenta.getEstado()) {
                // Crear movimiento de activación
                MovimientosRequestDTO movimientoRequest = new MovimientosRequestDTO(
                    cuenta.getId(),
                    TipoMovimiento.ACTIVAR,
                    null
                );
                
                movimientoService.registrarMovimiento(movimientoRequest);
            }
        }
        
        // Activar el cliente
        cliente.setEstado(true);
        clienteRepository.save(cliente);
    }
}
