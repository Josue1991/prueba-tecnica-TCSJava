package com.banco.banco_api.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO de respuesta con archivo en base64")
public record ArchivoResponseDTO(
    
    @Schema(description = "Contenido del archivo codificado en base64")
    String contenidoBase64,
    
    @Schema(description = "Nombre del archivo", example = "reporte_movimientos_2026-02-26.pdf")
    String nombreArchivo,
    
    @Schema(description = "Tipo MIME del archivo", example = "application/pdf")
    String tipoMime,
    
    @Schema(description = "Tamaño del archivo en bytes", example = "15240")
    long tamanioBytes
) {}
