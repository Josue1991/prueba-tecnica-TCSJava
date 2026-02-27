package com.banco.banco_api.infrastructure.controller;

import com.banco.banco_api.application.dto.CuentaRequestDTO;
import com.banco.banco_api.application.dto.CuentaResponseDTO;
import com.banco.banco_api.application.service.ICuentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
@Tag(name = "Cuentas", description = "Gestión de cuentas bancarias")
public class CuentaController {

    private final ICuentaService cuentaService;

    public CuentaController(ICuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @Operation(summary = "Crear una nueva cuenta", description = "Registra una nueva cuenta bancaria asociada a un cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cuenta creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
            @ApiResponse(responseCode = "409", description = "Número de cuenta ya existe")
    })
    @PostMapping
    public ResponseEntity<CuentaResponseDTO> crearCuenta(@Valid @RequestBody CuentaRequestDTO cuentaDTO) {
        CuentaResponseDTO cuentaCreada = cuentaService.crearCuenta(cuentaDTO);
        return new ResponseEntity<>(cuentaCreada, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener cuenta por ID", description = "Retorna una cuenta específica por su identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cuenta encontrada"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CuentaResponseDTO> obtenerCuentaPorId(
            @Parameter(description = "ID de la cuenta") @PathVariable Long id) {
        CuentaResponseDTO cuenta = cuentaService.obtenerCuentaPorId(id);
        return ResponseEntity.ok(cuenta);
    }

    @Operation(summary = "Obtener cuenta por número", description = "Retorna una cuenta por su número único")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cuenta encontrada"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @GetMapping("/numero/{numeroCuenta}")
    public ResponseEntity<CuentaResponseDTO> obtenerCuentaPorNumero(
            @Parameter(description = "Número de cuenta") @PathVariable String numeroCuenta) {
        CuentaResponseDTO cuenta = cuentaService.obtenerCuentaPorNumero(numeroCuenta);
        return ResponseEntity.ok(cuenta);
    }

    @Operation(summary = "Listar todas las cuentas", description = "Retorna la lista completa de cuentas bancarias")
    @ApiResponse(responseCode = "200", description = "Lista de cuentas obtenida exitosamente")
    @GetMapping
    public ResponseEntity<List<CuentaResponseDTO>> obtenerTodasLasCuentas() {
        List<CuentaResponseDTO> cuentas = cuentaService.obtenerTodasLasCuentas();
        return ResponseEntity.ok(cuentas);
    }

    @Operation(summary = "Obtener cuentas por cliente", description = "Retorna todas las cuentas asociadas a un cliente específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de cuentas obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<CuentaResponseDTO>> obtenerCuentasPorCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long clienteId) {
        List<CuentaResponseDTO> cuentas = cuentaService.obtenerCuentasPorCliente(clienteId);
        return ResponseEntity.ok(cuentas);
    }

    @Operation(summary = "Desactivar cuenta", description = "Cambia el estado de la cuenta a inactivo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cuenta desactivada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivarCuenta(
            @Parameter(description = "ID de la cuenta") @PathVariable Long id) {
        cuentaService.desactivarCuenta(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Activar cuenta", description = "Cambia el estado de la cuenta a activo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cuenta activada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activarCuenta(
            @Parameter(description = "ID de la cuenta") @PathVariable Long id) {
        cuentaService.activarCuenta(id);
        return ResponseEntity.noContent().build();
    }
}
