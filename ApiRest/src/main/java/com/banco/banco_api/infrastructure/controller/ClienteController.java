package com.banco.banco_api.infrastructure.controller;

import com.banco.banco_api.application.dto.ClienteRequestDTO;
import com.banco.banco_api.application.dto.ClienteResponseDTO;
import com.banco.banco_api.application.dto.ClienteActivacionRequestDTO;
import com.banco.banco_api.application.dto.ClienteActivacionResponseDTO;
import com.banco.banco_api.application.service.IClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Gestión de clientes bancarios")
public class ClienteController {

    private final IClienteService clienteService;

    public ClienteController(IClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Operation(summary = "Crear un nuevo cliente", description = "Registra un nuevo cliente en el sistema bancario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Cliente ya existe")
    })
    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crearCliente(@Valid @RequestBody ClienteRequestDTO clienteDTO) {
        ClienteResponseDTO clienteCreado = clienteService.crearCliente(clienteDTO);
        return new ResponseEntity<>(clienteCreado, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener cliente por ID", description = "Retorna un cliente específico por su identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> obtenerClientePorId(
            @Parameter(description = "ID del cliente") @PathVariable Long id) {
        ClienteResponseDTO cliente = clienteService.obtenerClientePorId(id);
        return ResponseEntity.ok(cliente);
    }

    @Operation(summary = "Obtener cliente por documento", description = "Retorna un cliente por su documento de identidad")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping("/documento/{documentoIdentidad}")
    public ResponseEntity<ClienteResponseDTO> obtenerClientePorDocumento(
            @Parameter(description = "Documento de identidad del cliente") @PathVariable String documentoIdentidad) {
        ClienteResponseDTO cliente = clienteService.obtenerClientePorDocumento(documentoIdentidad);
        return ResponseEntity.ok(cliente);
    }

    @Operation(summary = "Listar todos los clientes", description = "Retorna la lista completa de clientes")
    @ApiResponse(responseCode = "200", description = "Lista de clientes obtenida exitosamente")
    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> obtenerTodosLosClientes() {
        List<ClienteResponseDTO> clientes = clienteService.obtenerTodosLosClientes();
        return ResponseEntity.ok(clientes);
    }

    @Operation(summary = "Listar clientes activos", description = "Retorna solo los clientes con estado activo")
    @ApiResponse(responseCode = "200", description = "Lista de clientes activos obtenida exitosamente")
    @GetMapping("/activos")
    public ResponseEntity<List<ClienteResponseDTO>> obtenerClientesActivos() {
        List<ClienteResponseDTO> clientes = clienteService.obtenerClientesActivos();
        return ResponseEntity.ok(clientes);
    }

    @Operation(summary = "Actualizar cliente", description = "Actualiza la información de un cliente existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> actualizarCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long id,
            @Valid @RequestBody ClienteRequestDTO clienteDTO) {
        ClienteResponseDTO clienteActualizado = clienteService.actualizarCliente(id, clienteDTO);
        return ResponseEntity.ok(clienteActualizado);
    }

    @Operation(summary = "Desactivar cliente", description = "Desactiva el cliente y TODAS sus cuentas activas automáticamente. Genera movimientos de desactivación para cada cuenta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente y todas sus cuentas desactivados exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivarCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long id) {
        clienteService.desactivarCliente(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Activar cliente", description = "Cambia el estado del cliente a activo (sin activar cuentas)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente activado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activarCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long id) {
        clienteService.activarCliente(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Validar activación de cliente", 
               description = "Obtiene la lista de cuentas del cliente para permitir selección de cuáles activar.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validación completada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping("/{id}/validar-activacion")
    public ResponseEntity<ClienteActivacionResponseDTO> validarActivacionCliente(
            @Parameter(description = "ID del cliente") @PathVariable Long id) {
        ClienteActivacionResponseDTO response = clienteService.validarActivacionCliente(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Activar cliente con cuentas seleccionadas", 
               description = "Activa el cliente y las cuentas seleccionadas. Genera movimientos de activación para cada cuenta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente y cuentas activados exitosamente"),
            @ApiResponse(responseCode = "400", description = "Error de validación: cuenta no pertenece al cliente, cuenta eliminada, etc."),
            @ApiResponse(responseCode = "404", description = "Cliente o cuenta no encontrada")
    })
    @PatchMapping("/{id}/activar-con-cuentas")
    public ResponseEntity<Void> activarClienteConCuentas(
            @Parameter(description = "ID del cliente") @PathVariable Long id,
            @Valid @RequestBody ClienteActivacionRequestDTO request) {
        clienteService.activarClienteConCuentas(id, request);
        return ResponseEntity.noContent().build();
    }
}
