package com.banco.banco_api.infrastructure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.banco.banco_api.application.service.IMovimientoService;
import com.banco.banco_api.application.dto.MovimientosRequestDTO;
import com.banco.banco_api.application.dto.MovimientosResponseDTO;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
@Tag(name = "Movimientos", description = "API para la gestión de movimientos bancarios (retiros, depósitos, activación/desactivación)")
public class MovimientoController {

    private final IMovimientoService movimientoService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Registrar un nuevo movimiento", 
        description = "Registra un nuevo movimiento bancario (RETIRO, DEPOSITO, ACTIVAR o DESACTIVAR) en una cuenta específica. " +
                     "El movimiento actualiza el saldo o estado de la cuenta automáticamente."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Movimiento registrado exitosamente",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = MovimientosResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Error de validación: cuenta inactiva, saldo insuficiente, cuenta eliminada, valor inválido"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Cuenta no encontrada con el ID proporcionado"
        )
    })
    public ResponseEntity<MovimientosResponseDTO> registrarMovimiento(
            @Parameter(description = "Datos del movimiento a registrar", required = true)
            @Valid @RequestBody MovimientosRequestDTO request) {

        MovimientosResponseDTO response = movimientoService.registrarMovimiento(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(value = "/cuenta/{cuentaId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Obtener movimientos por cuenta", 
        description = "Obtiene la lista completa de todos los movimientos (débitos y créditos) " +
                     "realizados en una cuenta bancaria específica, ordenados por fecha"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lista de movimientos obtenida exitosamente",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = MovimientosResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Cuenta no encontrada"
        )
    })
    public ResponseEntity<List<MovimientosResponseDTO>> obtenerMovimientosPorCuenta(
            @Parameter(description = "ID de la cuenta bancaria", example = "1", required = true) 
            @PathVariable Long cuentaId) {

        List<MovimientosResponseDTO> movimientos = movimientoService.obtenerMovimientosPorCuenta(cuentaId);
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Obtener todos los movimientos", 
        description = "Obtiene la lista completa de todos los movimientos registrados en el sistema, ordenados por fecha"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lista de movimientos obtenida exitosamente",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = MovimientosResponseDTO.class)
            )
        )
    })
    public ResponseEntity<List<MovimientosResponseDTO>> obtenerTodosLosMovimientos() {
        List<MovimientosResponseDTO> movimientos = movimientoService.obtenerTodosLosMovimientos();
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping(value = "/cliente/{clienteId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Obtener movimientos por cliente", 
        description = "Obtiene la lista de todos los movimientos de todas las cuentas pertenecientes a un cliente específico"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lista de movimientos obtenida exitosamente",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = MovimientosResponseDTO.class)
            )
        )
    })
    public ResponseEntity<List<MovimientosResponseDTO>> obtenerMovimientosPorCliente(
            @Parameter(description = "ID del cliente", example = "1", required = true) 
            @PathVariable Long clienteId) {

        List<MovimientosResponseDTO> movimientos = movimientoService.obtenerMovimientosPorCliente(clienteId);
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping(value = "/cuenta/{cuentaId}/rango", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Obtener movimientos por cuenta y rango de fechas", 
        description = "Obtiene los movimientos de una cuenta específica filtrados por rango de fechas"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lista de movimientos obtenida exitosamente",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = MovimientosResponseDTO.class)
            )
        )
    })
    public ResponseEntity<List<MovimientosResponseDTO>> obtenerMovimientosPorCuentaYFechas(
            @Parameter(description = "ID de la cuenta bancaria", example = "1", required = true) 
            @PathVariable Long cuentaId,
            @Parameter(description = "Fecha de inicio (formato: YYYY-MM-DD)", example = "2026-01-01", required = true)
            @RequestParam String fechaInicio,
            @Parameter(description = "Fecha de fin (formato: YYYY-MM-DD)", example = "2026-12-31", required = true)
            @RequestParam String fechaFin) {

        LocalDate inicio = LocalDate.parse(fechaInicio);
        LocalDate fin = LocalDate.parse(fechaFin);
        List<MovimientosResponseDTO> movimientos = movimientoService.obtenerMovimientosPorCuentaYFechas(cuentaId, inicio, fin);
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping(value = "/cliente/{clienteId}/rango", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Obtener movimientos por cliente y rango de fechas", 
        description = "Obtiene los movimientos de todas las cuentas de un cliente filtrados por rango de fechas"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Lista de movimientos obtenida exitosamente",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = MovimientosResponseDTO.class)
            )
        )
    })
    public ResponseEntity<List<MovimientosResponseDTO>> obtenerMovimientosPorClienteYFechas(
            @Parameter(description = "ID del cliente", example = "1", required = true) 
            @PathVariable Long clienteId,
            @Parameter(description = "Fecha de inicio (formato: YYYY-MM-DD)", example = "2026-01-01", required = true)
            @RequestParam String fechaInicio,
            @Parameter(description = "Fecha de fin (formato: YYYY-MM-DD)", example = "2026-12-31", required = true)
            @RequestParam String fechaFin) {

        LocalDate inicio = LocalDate.parse(fechaInicio);
        LocalDate fin = LocalDate.parse(fechaFin);
        List<MovimientosResponseDTO> movimientos = movimientoService.obtenerMovimientosPorClienteYFechas(clienteId, inicio, fin);
        return ResponseEntity.ok(movimientos);
    }
}
