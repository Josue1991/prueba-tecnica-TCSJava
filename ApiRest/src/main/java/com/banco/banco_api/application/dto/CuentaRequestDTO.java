package com.banco.banco_api.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class CuentaRequestDTO {

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Size(min = 10, max = 30, message = "El número de cuenta debe tener entre 10 y 30 caracteres")
    private String numeroCuenta;

    @NotBlank(message = "El tipo de cuenta es obligatorio")
    @Size(max = 50, message = "El tipo de cuenta no debe exceder 50 caracteres")
    private String tipoCuenta;

    @NotNull(message = "El saldo inicial es obligatorio")
    @PositiveOrZero(message = "El saldo inicial debe ser mayor o igual a cero")
    private BigDecimal saldoInicial;

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;

    public CuentaRequestDTO() {
    }

    public CuentaRequestDTO(String numeroCuenta, String tipoCuenta, BigDecimal saldoInicial, Long clienteId) {
        this.numeroCuenta = numeroCuenta;
        this.tipoCuenta = tipoCuenta;
        this.saldoInicial = saldoInicial;
        this.clienteId = clienteId;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public String getTipoCuenta() {
        return tipoCuenta;
    }

    public void setTipoCuenta(String tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
    }

    public BigDecimal getSaldoInicial() {
        return saldoInicial;
    }

    public void setSaldoInicial(BigDecimal saldoInicial) {
        this.saldoInicial = saldoInicial;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }
}
