package com.banco.banco_api.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.banco.banco_api.domain.model.entity.Movimiento;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IMovimientoRepository extends JpaRepository<Movimiento, Long> {
    
    List<Movimiento> findByCuentaId(Long cuentaId);
    
    @Query("SELECT m FROM Movimiento m WHERE m.cuenta.cliente.id = :clienteId " +
           "ORDER BY m.fechaMovimiento DESC")
    List<Movimiento> findByCuentaClienteId(@Param("clienteId") Long clienteId);
    
    @Query("SELECT m FROM Movimiento m ORDER BY m.fechaMovimiento DESC")
    List<Movimiento> findAllOrdered();
    
    @Query("SELECT m FROM Movimiento m WHERE m.cuenta.id = :cuentaId " +
           "AND m.fechaMovimiento >= :fechaInicio AND m.fechaMovimiento <= :fechaFin " +
           "ORDER BY m.fechaMovimiento ASC")
    List<Movimiento> findByCuentaIdAndFechaMovimientoBetween(
        @Param("cuentaId") Long cuentaId, 
        @Param("fechaInicio") LocalDateTime fechaInicio, 
        @Param("fechaFin") LocalDateTime fechaFin
    );
    
    @Query("SELECT m FROM Movimiento m WHERE m.cuenta.cliente.id = :clienteId " +
           "AND m.fechaMovimiento >= :fechaInicio AND m.fechaMovimiento <= :fechaFin " +
           "ORDER BY m.cuenta.id, m.fechaMovimiento ASC")
    List<Movimiento> findByClienteIdAndFechaMovimientoBetween(
        @Param("clienteId") Long clienteId, 
        @Param("fechaInicio") LocalDateTime fechaInicio, 
        @Param("fechaFin") LocalDateTime fechaFin
    );
    
    @Query("SELECT m FROM Movimiento m WHERE m.fechaMovimiento >= :fechaInicio " +
           "AND m.fechaMovimiento <= :fechaFin ORDER BY m.cuenta.id, m.fechaMovimiento ASC")
    List<Movimiento> findByFechaMovimientoBetween(
        @Param("fechaInicio") LocalDateTime fechaInicio, 
        @Param("fechaFin") LocalDateTime fechaFin
    );
}
