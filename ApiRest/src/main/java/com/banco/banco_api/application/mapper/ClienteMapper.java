package com.banco.banco_api.application.mapper;

import com.banco.banco_api.application.dto.ClienteRequestDTO;
import com.banco.banco_api.application.dto.ClienteResponseDTO;
import com.banco.banco_api.domain.model.entity.Cliente;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ClienteMapper {

    public Cliente toEntity(ClienteRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Cliente cliente = new Cliente();
        cliente.setNombre(dto.getNombre());
        cliente.setGenero(dto.getGenero());
        cliente.setEdad(dto.getEdad());
        cliente.setIdentificacion(dto.getIdentificacion());
        cliente.setDireccion(dto.getDireccion());
        cliente.setTelefono(dto.getTelefono());
        cliente.setPassword(dto.getPassword());
        
        return cliente;
    }

    public ClienteResponseDTO toResponseDTO(Cliente entity) {
        if (entity == null) {
            return null;
        }
        
        return new ClienteResponseDTO(
            entity.getId(),
            entity.getNombre(),
            entity.getGenero(),
            entity.getEdad(),
            entity.getIdentificacion(),
            entity.getDireccion(),
            entity.getTelefono(),
            entity.getEstado(),
            entity.getCreatedAt()
        );
    }

    public List<ClienteResponseDTO> toResponseDTOList(List<Cliente> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public void updateEntityFromDTO(ClienteRequestDTO dto, Cliente entity) {
        if (dto == null || entity == null) {
            return;
        }
        
        if (dto.getNombre() != null) {
            entity.setNombre(dto.getNombre());
        }
        if (dto.getGenero() != null) {
            entity.setGenero(dto.getGenero());
        }
        if (dto.getEdad() != null) {
            entity.setEdad(dto.getEdad());
        }
        if (dto.getIdentificacion() != null) {
            entity.setIdentificacion(dto.getIdentificacion());
        }
        if (dto.getDireccion() != null) {
            entity.setDireccion(dto.getDireccion());
        }
        if (dto.getTelefono() != null) {
            entity.setTelefono(dto.getTelefono());
        }
        if (dto.getPassword() != null) {
            entity.setPassword(dto.getPassword());
        }
    }
}
