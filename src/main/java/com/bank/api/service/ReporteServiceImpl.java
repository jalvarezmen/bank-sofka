package com.bank.api.service;

import com.bank.api.dto.ReporteDTO;
import com.bank.api.model.Movimiento;
import com.bank.api.repository.MovimientoRepository;
import com.bank.api.service.support.RecursoActivoConsulta;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private final MovimientoRepository movimientoRepository;
    private final RecursoActivoConsulta recursoActivoConsulta;

    @Override
    @Transactional(readOnly = true)
    public List<ReporteDTO> generarReporte(Long clienteId, LocalDate fechaInicio, LocalDate fechaFin) {
        recursoActivoConsulta.clienteActivoPorId(clienteId);

        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);

        return movimientoRepository
                .findByCuenta_Cliente_ClienteIdAndFechaBetween(clienteId, inicio, fin)
                .stream()
                .map(this::toReporteDto)
                .toList();
    }

    private ReporteDTO toReporteDto(Movimiento movimiento) {
        ReporteDTO dto = new ReporteDTO();
        dto.setFecha(movimiento.getFecha());
        dto.setCliente(movimiento.getCuenta().getCliente().getNombre());
        dto.setNumeroCuenta(movimiento.getCuenta().getNumeroCuenta());
        dto.setTipoCuenta(movimiento.getCuenta().getTipoCuenta());
        dto.setSaldoInicial(movimiento.getCuenta().getSaldoInicial());
        dto.setEstado(movimiento.getCuenta().isEstado());
        dto.setMovimiento(movimiento.getValor());
        dto.setSaldoDisponible(movimiento.getSaldo());
        return dto;
    }
}
