package com.banco.banco_api.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banco.banco_api.domain.model.entity.Cliente;

import java.util.List;
import java.util.Optional;

@Repository
public interface IClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByIdentificacion(String identificacion);

    List<Cliente> findByEstadoTrue();

    boolean existsByIdentificacion(String identificacion);
}
