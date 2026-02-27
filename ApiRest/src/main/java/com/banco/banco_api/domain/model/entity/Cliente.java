package com.banco.banco_api.domain.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@PrimaryKeyJoinColumn(name = "id")
public class Cliente extends Persona {

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false)
    private Boolean estado = true;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        createdAt = LocalDateTime.now();
    }

    public Cliente() {
        super();
    }

    public Cliente(String nombre, String genero, Integer edad, String identificacion, 
                   String direccion, String telefono, String password) {
        super(nombre, genero, edad, identificacion, direccion, telefono);
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + getId() +
                ", nombre='" + getNombre() + '\'' +
                ", identificacion='" + getIdentificacion() + '\'' +
                ", estado=" + estado +
                ", deleted=" + deleted +
                '}';
    }
}
