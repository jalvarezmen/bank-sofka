package com.bank.api.service;

import com.bank.api.dto.MovimientoDTO;
import java.util.List;

public interface MovimientoService {

    List<MovimientoDTO> listarTodos();

    MovimientoDTO obtenerPorId(Long id);

    MovimientoDTO registrar(MovimientoDTO dto);
}
