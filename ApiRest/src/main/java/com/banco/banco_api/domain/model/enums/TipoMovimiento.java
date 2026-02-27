package com.banco.banco_api.domain.model.enums;

public enum TipoMovimiento {
    RETIRO("Retiro"),
    DEPOSITO("Depósito"),
    ACTIVAR("Activar"),
    DESACTIVAR("Desactivar");

    private final String descripcion;

    TipoMovimiento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}