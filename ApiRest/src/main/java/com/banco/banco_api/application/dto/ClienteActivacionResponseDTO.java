package com.banco.banco_api.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "DTO de respuesta para validación de activación de cliente")
public record ClienteActivacionResponseDTO(
    
    @Schema(description = "Estado actual del cliente", example = "false")
    boolean clienteActivo,
    
    @Schema(description = "Lista de cuentas del cliente disponibles para activar")
    List<CuentaInfo> cuentas,
    
    @Schema(description = "Mensaje informativo")
    String mensaje
) {
    @Schema(description = "Información resumida de cuenta")
    public record CuentaInfo(
        @Schema(description = "ID de la cuenta", example = "1")
        Long id,
        
        @Schema(description = "Número de cuenta", example = "5520366226")
        String numeroCuenta,
        
        @Schema(description = "Tipo de cuenta", example = "Ahorro")
        String tipoCuenta,
        
        @Schema(description = "Estado actual de la cuenta", example = "false")
        boolean estado,
        
        @Schema(description = "Está eliminada", example = "false")
        boolean deleted
    ) {}
}
