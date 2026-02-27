package com.banco.banco_api.application.mapper;

import com.banco.banco_api.application.dto.CuentaRequestDTO;
import com.banco.banco_api.application.dto.CuentaResponseDTO;
import com.banco.banco_api.domain.model.entity.Cliente;
import com.banco.banco_api.domain.model.entity.Cuenta;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CuentaMapper {

    public Cuenta toEntity(CuentaRequestDTO dto, Cliente cliente) {
        if (dto == null) {
            return null;
        }
        
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(dto.getNumeroCuenta());
        cuenta.setTipoCuenta(dto.getTipoCuenta());
        cuenta.setSaldoInicial(dto.getSaldoInicial());
        cuenta.setSaldoActual(dto.getSaldoInicial());
        cuenta.setCliente(cliente);
        
        return cuenta;
    }

    public CuentaResponseDTO toResponseDTO(Cuenta entity) {
        if (entity == null) {
            return null;
        }
        
        String nombreCliente = entity.getCliente() != null 
            ? entity.getCliente().getNombre()
            : null;
            
        Long clienteId = entity.getCliente() != null 
            ? entity.getCliente().getId() 
            : null;
        
        return new CuentaResponseDTO(
            entity.getId(),
            entity.getNumeroCuenta(),
            entity.getTipoCuenta(),
            entity.getSaldoInicial(),
            entity.getSaldoActual(),
            clienteId,
            nombreCliente,
            entity.getEstado(),
            entity.getDeleted(),
            entity.getCreatedAt()
        );
    }

    public List<CuentaResponseDTO> toResponseDTOList(List<Cuenta> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
}
