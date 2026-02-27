package com.banco.banco_api.domain.model.enums;

public enum Estado {
    ACTIVO("Cuenta de Ahorro"),
    INACTIVO("Cuenta Inactiva"),
    SUSPENDIDO("Cuenta Suspendida");

    private final String descripcion;

    Estado(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
