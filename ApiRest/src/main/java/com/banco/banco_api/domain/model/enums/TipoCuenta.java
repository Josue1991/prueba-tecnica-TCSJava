package com.banco.banco_api.domain.model.enums;


public enum TipoCuenta {
    AHORRO("Cuenta de Ahorro"),
    CORRIENTE("Cuenta Corriente"),
    NOMINA("Cuenta Nómina");

    private final String descripcion;

    TipoCuenta(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
