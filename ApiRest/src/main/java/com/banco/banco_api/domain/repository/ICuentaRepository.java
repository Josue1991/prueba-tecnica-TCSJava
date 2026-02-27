package com.banco.banco_api.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banco.banco_api.domain.model.entity.Cuenta;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICuentaRepository extends JpaRepository<Cuenta, Long> {

    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);

    List<Cuenta> findByClienteId(Long clienteId);

    List<Cuenta> findByEstadoTrue();

    boolean existsByNumeroCuenta(String numeroCuenta);
}
