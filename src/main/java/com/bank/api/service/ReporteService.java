package com.bank.api.service;

import com.bank.api.dto.ReporteDTO;
import java.time.LocalDate;
import java.util.List;

public interface ReporteService {

    List<ReporteDTO> generarReporte(Long clienteId, LocalDate fechaInicio, LocalDate fechaFin);
}
