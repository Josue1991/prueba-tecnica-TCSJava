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
}
