package com.banco.banco_api.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DTO para solicitar envío de reporte por correo electrónico")
public record EnviarReporteEmailRequestDTO(
    
    @Schema(description = "Correo electrónico del destinatario", example = "usuario@ejemplo.com")
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "Debe ser un correo electrónico válido")
    String emailDestinatario,
    
    @Schema(description = "Formato del archivo adjunto", example = "PDF", allowableValues = {"PDF", "EXCEL"})
    @NotBlank(message = "El formato es obligatorio")
    String formato,
    
    @Schema(description = "Asunto personalizado del correo (opcional)", example = "Reporte de Movimientos - Febrero 2026")
    String asunto,
    
    @Schema(description = "Mensaje adicional en el cuerpo del correo (opcional)")
    String mensajeAdicional
) {}
