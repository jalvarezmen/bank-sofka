package com.bank.api.service;

import com.bank.api.dto.ReporteDTO;
import com.bank.api.exception.RecursoNoEncontradoException;
import com.bank.api.model.Movimiento;
import com.bank.api.repository.ClienteRepository;
import com.bank.api.repository.MovimientoRepository;
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
    private final ClienteRepository clienteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReporteDTO> generarReporte(Long clienteId, LocalDate fechaInicio, LocalDate fechaFin) {
        clienteRepository
                .findByClienteIdAndEstadoTrue(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cliente no encontrado con id: " + clienteId));

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
