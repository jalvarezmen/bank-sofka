package com.bank.api.service;

import com.bank.api.dto.CuentaDTO;
import java.util.List;

public interface CuentaService {

    List<CuentaDTO> listarTodos();

    CuentaDTO obtenerPorId(Long id);

    CuentaDTO crear(CuentaDTO dto);

    CuentaDTO actualizar(Long id, CuentaDTO dto);

    void eliminar(Long id);
}
