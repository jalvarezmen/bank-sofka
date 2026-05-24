package com.bank.api.service;

import com.bank.api.dto.ClienteDTO;
import java.util.List;

public interface ClienteService {

    List<ClienteDTO> listarTodos();

    ClienteDTO obtenerPorId(Long id);

    ClienteDTO crear(ClienteDTO dto);

    ClienteDTO actualizar(Long id, ClienteDTO dto);

    void eliminar(Long id);
}
