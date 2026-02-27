package com.banco.banco_api.application.service;

import com.banco.banco_api.application.dto.ClienteRequestDTO;
import com.banco.banco_api.application.dto.ClienteResponseDTO;
import com.banco.banco_api.application.dto.ClienteActivacionRequestDTO;
import com.banco.banco_api.application.dto.ClienteActivacionResponseDTO;

import java.util.List;

public interface IClienteService {

    ClienteResponseDTO crearCliente(ClienteRequestDTO clienteDTO);

    ClienteResponseDTO obtenerClientePorId(Long id);

    ClienteResponseDTO obtenerClientePorDocumento(String documentoIdentidad);

    List<ClienteResponseDTO> obtenerTodosLosClientes();

    List<ClienteResponseDTO> obtenerClientesActivos();

    ClienteResponseDTO actualizarCliente(Long id, ClienteRequestDTO clienteDTO);

    void desactivarCliente(Long id);

    void activarCliente(Long id);
    
    ClienteActivacionResponseDTO validarActivacionCliente(Long id);
    
    void activarClienteConCuentas(Long id, ClienteActivacionRequestDTO request);
}
