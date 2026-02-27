package com.banco.banco_api.application.service;

import com.banco.banco_api.application.dto.ClienteActivacionRequestDTO;
import com.banco.banco_api.application.dto.ClienteActivacionResponseDTO;
import com.banco.banco_api.application.dto.ClienteRequestDTO;
import com.banco.banco_api.application.dto.ClienteResponseDTO;
import com.banco.banco_api.application.mapper.ClienteMapper;
import com.banco.banco_api.application.service.impl.ClienteServiceImpl;
import com.banco.banco_api.domain.model.entity.Cliente;
import com.banco.banco_api.domain.model.entity.Cuenta;
import com.banco.banco_api.domain.repository.IClienteRepository;
import com.banco.banco_api.domain.repository.ICuentaRepository;
import com.banco.banco_api.infrastructure.exception.BusinessException;
import com.banco.banco_api.infrastructure.exception.DuplicateResourceException;
import com.banco.banco_api.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteServiceImpl - Pruebas Unitarias")
class ClienteServiceImplTest {

    @Mock
    private IClienteRepository clienteRepository;

    @Mock
    private ICuentaRepository cuentaRepository;

    @Mock
    private IMovimientoService movimientoService;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private Cliente clienteActivo;
    private Cliente clienteInactivo;
    private ClienteRequestDTO requestDTO;
    private ClienteResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        clienteActivo = new Cliente("Juan Pérez", "Masculino", 30,
                "1234567890", "Calle 1", "0987654321", "pw123456");
        clienteActivo.setEstado(true);

        clienteInactivo = new Cliente("Ana García", "Femenino", 25,
                "0987654321", "Calle 2", "0998877665", "pw654321");
        clienteInactivo.setEstado(false);

        requestDTO = new ClienteRequestDTO("Juan Pérez", "Masculino", 30,
                "1234567890", "Calle 1", "0987654321", "pw123456");

        responseDTO = new ClienteResponseDTO(1L, "Juan Pérez", "Masculino", 30,
                "1234567890", "Calle 1", "0987654321", true, LocalDateTime.now());
    }

    // ─── crearCliente ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("crearCliente: guarda y devuelve el DTO cuando la identificación no existe")
    void crearCliente_exitoso() {
        when(clienteRepository.existsByIdentificacion("1234567890")).thenReturn(false);
        when(clienteMapper.toEntity(requestDTO)).thenReturn(clienteActivo);
        when(clienteRepository.save(clienteActivo)).thenReturn(clienteActivo);
        when(clienteMapper.toResponseDTO(clienteActivo)).thenReturn(responseDTO);

        ClienteResponseDTO resultado = clienteService.crearCliente(requestDTO);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Juan Pérez");
        verify(clienteRepository).save(clienteActivo);
    }

    @Test
    @DisplayName("crearCliente: lanza DuplicateResourceException si la identificación ya existe")
    void crearCliente_identificacionDuplicada_lanzaExcepcion() {
        when(clienteRepository.existsByIdentificacion("1234567890")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.crearCliente(requestDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("1234567890");

        verify(clienteRepository, never()).save(any());
    }

    // ─── obtenerClientePorId ───────────────────────────────────────────────────

    @Test
    @DisplayName("obtenerClientePorId: devuelve DTO cuando el cliente existe")
    void obtenerClientePorId_clienteEncontrado() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));
        when(clienteMapper.toResponseDTO(clienteActivo)).thenReturn(responseDTO);

        ClienteResponseDTO resultado = clienteService.obtenerClientePorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("obtenerClientePorId: lanza ResourceNotFoundException cuando el cliente no existe")
    void obtenerClientePorId_clienteNoEncontrado_lanzaExcepcion() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.obtenerClientePorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ─── obtenerTodosLosClientes ───────────────────────────────────────────────

    @Test
    @DisplayName("obtenerTodosLosClientes: devuelve lista de DTOs")
    void obtenerTodosLosClientes_devuelveLista() {
        List<Cliente> clientes = List.of(clienteActivo, clienteInactivo);
        List<ClienteResponseDTO> dtos = List.of(responseDTO,
                new ClienteResponseDTO(2L, "Ana García", "Femenino", 25,
                        "0987654321", "Calle 2", "0998877665", false, LocalDateTime.now()));

        when(clienteRepository.findAll()).thenReturn(clientes);
        when(clienteMapper.toResponseDTOList(clientes)).thenReturn(dtos);

        List<ClienteResponseDTO> resultado = clienteService.obtenerTodosLosClientes();

        assertThat(resultado).hasSize(2);
    }

    // ─── actualizarCliente ─────────────────────────────────────────────────────

    @Test
    @DisplayName("actualizarCliente: actualiza y devuelve DTO cuando los datos son válidos")
    void actualizarCliente_exitoso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));
        // La identificación no cambia → existsByIdentificacion no se invoca
        when(clienteRepository.save(clienteActivo)).thenReturn(clienteActivo);
        when(clienteMapper.toResponseDTO(clienteActivo)).thenReturn(responseDTO);

        ClienteResponseDTO resultado = clienteService.actualizarCliente(1L, requestDTO);

        assertThat(resultado).isNotNull();
        verify(clienteMapper).updateEntityFromDTO(requestDTO, clienteActivo);
    }

    @Test
    @DisplayName("actualizarCliente: lanza ResourceNotFoundException si el cliente no existe")
    void actualizarCliente_clienteNoExiste_lanzaExcepcion() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.actualizarCliente(99L, requestDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── desactivarCliente ─────────────────────────────────────────────────────

    @Test
    @DisplayName("desactivarCliente: desactiva el cliente y sus cuentas activas")
    void desactivarCliente_desactivaCuentasActivas() {
        Cuenta cuentaActiva = new Cuenta("111", "AHORRO", java.math.BigDecimal.valueOf(500), clienteActivo);
        cuentaActiva.setEstado(true);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));
        when(cuentaRepository.findByClienteId(1L)).thenReturn(List.of(cuentaActiva));

        // Necesitamos que el cliente tenga id para que findByClienteId funcione
        clienteService.desactivarCliente(1L);

        verify(movimientoService, times(1)).registrarMovimiento(any());
        verify(clienteRepository).save(argThat(c -> !c.getEstado()));
    }

    @Test
    @DisplayName("desactivarCliente: lanza ResourceNotFoundException si cliente no existe")
    void desactivarCliente_clienteNoExiste_lanzaExcepcion() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.desactivarCliente(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(movimientoService, never()).registrarMovimiento(any());
    }

    // ─── activarCliente ────────────────────────────────────────────────────────

    @Test
    @DisplayName("activarCliente: activa el cliente correctamente")
    void activarCliente_exitoso() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteInactivo));

        clienteService.activarCliente(1L);

        verify(clienteRepository).save(argThat(c -> c.getEstado()));
    }

    @Test
    @DisplayName("activarCliente: lanza ResourceNotFoundException si cliente no existe")
    void activarCliente_clienteNoExiste_lanzaExcepcion() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.activarCliente(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── validarActivacionCliente ──────────────────────────────────────────────

    @Test
    @DisplayName("validarActivacionCliente: devuelve DTO con estado del cliente e info de cuentas")
    void validarActivacionCliente_devuelveDTO() {
        Cuenta cuentaActiva = new Cuenta("111", "AHORRO", java.math.BigDecimal.valueOf(500), clienteActivo);
        cuentaActiva.setEstado(true);
        Cuenta cuentaInactiva = new Cuenta("222", "CORRIENTE", java.math.BigDecimal.valueOf(200), clienteActivo);
        cuentaInactiva.setEstado(false);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));
        when(cuentaRepository.findByClienteId(1L)).thenReturn(List.of(cuentaActiva, cuentaInactiva));

        ClienteActivacionResponseDTO resultado = clienteService.validarActivacionCliente(1L);

        assertThat(resultado.clienteActivo()).isTrue();
        assertThat(resultado.cuentas()).hasSize(2);
        assertThat(resultado.mensaje()).isNotBlank();
    }

    // ─── activarClienteConCuentas ──────────────────────────────────────────────

    @Test
    @DisplayName("activarClienteConCuentas: activa cliente y solo las cuentas seleccionadas")
    void activarClienteConCuentas_exitoso() {
        Cuenta cuentaInactiva = new Cuenta("111", "AHORRO", java.math.BigDecimal.valueOf(500), clienteInactivo);
        cuentaInactiva.setEstado(false);

        // Simulamos que la cuenta tiene id = 1
        Cuenta cuentaSpy = spy(cuentaInactiva);
        doReturn(1L).when(cuentaSpy).getId();

        ClienteActivacionRequestDTO activacionDTO = new ClienteActivacionRequestDTO(List.of(1L));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteInactivo));
        when(cuentaRepository.findByClienteId(1L)).thenReturn(List.of(cuentaSpy));
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaSpy));

        clienteService.activarClienteConCuentas(1L, activacionDTO);

        verify(movimientoService, times(1)).registrarMovimiento(any());
        verify(clienteRepository).save(argThat(c -> c.getEstado()));
    }

    @Test
    @DisplayName("activarClienteConCuentas: lanza BusinessException si la cuenta no pertenece al cliente")
    void activarClienteConCuentas_cuentaNoPertenece_lanzaExcepcion() {
        Cuenta cuentaOtroCliente = new Cuenta("999", "AHORRO", java.math.BigDecimal.valueOf(100), clienteInactivo);
        Cuenta cuentaSpy = spy(cuentaOtroCliente);
        doReturn(5L).when(cuentaSpy).getId();

        ClienteActivacionRequestDTO activacionDTO = new ClienteActivacionRequestDTO(List.of(99L));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteInactivo));
        when(cuentaRepository.findByClienteId(1L)).thenReturn(List.of(cuentaSpy));

        assertThatThrownBy(() -> clienteService.activarClienteConCuentas(1L, activacionDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("99");

        verify(clienteRepository, never()).save(any());
    }
}
