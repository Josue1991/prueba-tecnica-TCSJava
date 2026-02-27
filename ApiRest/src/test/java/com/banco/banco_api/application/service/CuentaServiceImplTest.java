package com.banco.banco_api.application.service;

import com.banco.banco_api.application.dto.CuentaRequestDTO;
import com.banco.banco_api.application.dto.CuentaResponseDTO;
import com.banco.banco_api.application.mapper.CuentaMapper;
import com.banco.banco_api.application.service.impl.CuentaServiceImpl;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CuentaServiceImpl - Pruebas Unitarias")
class CuentaServiceImplTest {

    @Mock private ICuentaRepository cuentaRepository;
    @Mock private IClienteRepository clienteRepository;
    @Mock private IMovimientoService movimientoService;
    @Mock private CuentaMapper cuentaMapper;

    @InjectMocks
    private CuentaServiceImpl cuentaService;

    private Cliente clienteActivo;
    private Cliente clienteInactivo;
    private Cuenta cuentaActiva;
    private Cuenta cuentaInactiva;
    private CuentaRequestDTO requestDTO;
    private CuentaResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        clienteActivo = new Cliente("Juan Pérez", "Masculino", 30,
                "1234567890", "Calle 1", "0987654321", "pw123456");
        clienteActivo.setEstado(true);

        clienteInactivo = new Cliente("Ana García", "Femenino", 25,
                "0987654321", "Calle 2", "0998877665", "pw654321");
        clienteInactivo.setEstado(false);

        cuentaActiva = new Cuenta("5520366226", "AHORRO", BigDecimal.valueOf(1000), clienteActivo);
        cuentaActiva.setEstado(true);

        cuentaInactiva = new Cuenta("6631477337", "CORRIENTE", BigDecimal.valueOf(500), clienteActivo);
        cuentaInactiva.setEstado(false);

        requestDTO = new CuentaRequestDTO("5520366226", "AHORRO", BigDecimal.valueOf(1000), 1L);

        responseDTO = new CuentaResponseDTO(1L, "5520366226", "AHORRO",
                BigDecimal.valueOf(1000), BigDecimal.valueOf(1000),
                1L, "Juan Pérez", true, LocalDateTime.now());
    }

    // ─── crearCuenta ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("crearCuenta: guarda y devuelve el DTO con datos válidos")
    void crearCuenta_exitoso() {
        when(cuentaRepository.existsByNumeroCuenta("5520366226")).thenReturn(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));
        when(cuentaMapper.toEntity(requestDTO, clienteActivo)).thenReturn(cuentaActiva);
        when(cuentaRepository.save(cuentaActiva)).thenReturn(cuentaActiva);
        when(cuentaMapper.toResponseDTO(cuentaActiva)).thenReturn(responseDTO);

        CuentaResponseDTO resultado = cuentaService.crearCuenta(requestDTO);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNumeroCuenta()).isEqualTo("5520366226");
        verify(cuentaRepository).save(cuentaActiva);
    }

    @Test
    @DisplayName("crearCuenta: lanza DuplicateResourceException si el número ya existe")
    void crearCuenta_numeroDuplicado_lanzaExcepcion() {
        when(cuentaRepository.existsByNumeroCuenta("5520366226")).thenReturn(true);

        assertThatThrownBy(() -> cuentaService.crearCuenta(requestDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("5520366226");

        verify(cuentaRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearCuenta: lanza ResourceNotFoundException si el cliente no existe")
    void crearCuenta_clienteNoExiste_lanzaExcepcion() {
        when(cuentaRepository.existsByNumeroCuenta("5520366226")).thenReturn(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cuentaService.crearCuenta(requestDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    @DisplayName("crearCuenta: lanza BusinessException si el cliente está inactivo")
    void crearCuenta_clienteInactivo_lanzaExcepcion() {
        when(cuentaRepository.existsByNumeroCuenta("5520366226")).thenReturn(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteInactivo));

        assertThatThrownBy(() -> cuentaService.crearCuenta(requestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inactivo");
    }

    // ─── obtenerCuentaPorId ───────────────────────────────────────────────────

    @Test
    @DisplayName("obtenerCuentaPorId: devuelve DTO cuando la cuenta existe")
    void obtenerCuentaPorId_encontrada() {
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaActiva));
        when(cuentaMapper.toResponseDTO(cuentaActiva)).thenReturn(responseDTO);

        CuentaResponseDTO resultado = cuentaService.obtenerCuentaPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNumeroCuenta()).isEqualTo("5520366226");
    }

    @Test
    @DisplayName("obtenerCuentaPorId: lanza ResourceNotFoundException cuando no existe")
    void obtenerCuentaPorId_noEncontrada_lanzaExcepcion() {
        when(cuentaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cuentaService.obtenerCuentaPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ─── obtenerCuentasPorCliente ─────────────────────────────────────────────

    @Test
    @DisplayName("obtenerCuentasPorCliente: devuelve lista cuando el cliente existe")
    void obtenerCuentasPorCliente_clienteExiste() {
        when(clienteRepository.existsById(1L)).thenReturn(true);
        when(cuentaRepository.findByClienteId(1L)).thenReturn(List.of(cuentaActiva, cuentaInactiva));
        when(cuentaMapper.toResponseDTOList(any())).thenReturn(List.of(responseDTO));

        List<CuentaResponseDTO> resultado = cuentaService.obtenerCuentasPorCliente(1L);

        assertThat(resultado).isNotEmpty();
    }

    @Test
    @DisplayName("obtenerCuentasPorCliente: lanza ResourceNotFoundException si el cliente no existe")
    void obtenerCuentasPorCliente_clienteNoExiste_lanzaExcepcion() {
        when(clienteRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> cuentaService.obtenerCuentasPorCliente(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── depositar ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("depositar: incrementa el saldo y devuelve DTO actualizado")
    void depositar_exitoso() {
        when(cuentaRepository.findByNumeroCuenta("5520366226")).thenReturn(Optional.of(cuentaActiva));
        when(cuentaRepository.save(cuentaActiva)).thenReturn(cuentaActiva);
        when(cuentaMapper.toResponseDTO(cuentaActiva)).thenReturn(responseDTO);

        CuentaResponseDTO resultado = cuentaService.depositar("5520366226", BigDecimal.valueOf(200));

        assertThat(resultado).isNotNull();
        verify(cuentaRepository).save(cuentaActiva);
    }

    @Test
    @DisplayName("depositar: lanza BusinessException si la cuenta está inactiva")
    void depositar_cuentaInactiva_lanzaExcepcion() {
        when(cuentaRepository.findByNumeroCuenta("6631477337")).thenReturn(Optional.of(cuentaInactiva));

        assertThatThrownBy(() -> cuentaService.depositar("6631477337", BigDecimal.valueOf(200)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inactiva");
    }

    // ─── retirar ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("retirar: reduce el saldo y devuelve DTO actualizado")
    void retirar_exitoso() {
        when(cuentaRepository.findByNumeroCuenta("5520366226")).thenReturn(Optional.of(cuentaActiva));
        when(cuentaRepository.save(cuentaActiva)).thenReturn(cuentaActiva);
        when(cuentaMapper.toResponseDTO(cuentaActiva)).thenReturn(responseDTO);

        CuentaResponseDTO resultado = cuentaService.retirar("5520366226", BigDecimal.valueOf(100));

        assertThat(resultado).isNotNull();
        verify(cuentaRepository).save(cuentaActiva);
    }

    @Test
    @DisplayName("retirar: lanza BusinessException si la cuenta está inactiva")
    void retirar_cuentaInactiva_lanzaExcepcion() {
        when(cuentaRepository.findByNumeroCuenta("6631477337")).thenReturn(Optional.of(cuentaInactiva));

        assertThatThrownBy(() -> cuentaService.retirar("6631477337", BigDecimal.valueOf(100)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inactiva");
    }

    @Test
    @DisplayName("retirar: lanza ResourceNotFoundException si la cuenta no existe")
    void retirar_cuentaNoExiste_lanzaExcepcion() {
        when(cuentaRepository.findByNumeroCuenta("0000000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cuentaService.retirar("0000000000", BigDecimal.valueOf(50)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── desactivarCuenta ──────────────────────────────────────────────────────

    @Test
    @DisplayName("desactivarCuenta: genera movimiento de desactivación correctamente")
    void desactivarCuenta_exitoso() {
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaActiva));

        cuentaService.desactivarCuenta(1L);

        verify(movimientoService).registrarMovimiento(any());
    }

    @Test
    @DisplayName("desactivarCuenta: lanza BusinessException si la cuenta ya está desactivada")
    void desactivarCuenta_yaDesactivada_lanzaExcepcion() {
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaInactiva));

        assertThatThrownBy(() -> cuentaService.desactivarCuenta(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("desactivada");

        verify(movimientoService, never()).registrarMovimiento(any());
    }

    @Test
    @DisplayName("desactivarCuenta: lanza ResourceNotFoundException si la cuenta no existe")
    void desactivarCuenta_noExiste_lanzaExcepcion() {
        when(cuentaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cuentaService.desactivarCuenta(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── activarCuenta ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("activarCuenta: genera movimiento de activación correctamente")
    void activarCuenta_exitoso() {
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaInactiva));

        cuentaService.activarCuenta(1L);

        verify(movimientoService).registrarMovimiento(any());
    }

    @Test
    @DisplayName("activarCuenta: lanza BusinessException si la cuenta ya está activa")
    void activarCuenta_yaActiva_lanzaExcepcion() {
        when(cuentaRepository.findById(1L)).thenReturn(Optional.of(cuentaActiva));

        assertThatThrownBy(() -> cuentaService.activarCuenta(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("activa");

        verify(movimientoService, never()).registrarMovimiento(any());
    }

    @Test
    @DisplayName("activarCuenta: lanza ResourceNotFoundException si la cuenta no existe")
    void activarCuenta_noExiste_lanzaExcepcion() {
        when(cuentaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cuentaService.activarCuenta(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
