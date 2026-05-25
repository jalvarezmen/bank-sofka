package com.bank.api.service;

import com.bank.api.dto.MovimientoDTO;
import com.bank.api.exception.RecursoNoEncontradoException;
import com.bank.api.exception.SaldoInsuficienteException;
import com.bank.api.service.support.RecursoActivoConsulta;
import com.bank.api.model.Cuenta;
import com.bank.api.model.Movimiento;
import com.bank.api.repository.CuentaRepository;
import com.bank.api.repository.MovimientoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MovimientoServiceImpl implements MovimientoService {

    private static final String TIPO_DEPOSITO = "Deposito";
    private static final String TIPO_RETIRO = "Retiro";

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;
    private final RecursoActivoConsulta recursoActivoConsulta;

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoDTO> listarTodos() {
        return movimientoRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MovimientoDTO obtenerPorId(Long id) {
        return movimientoRepository
                .findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Movimiento no encontrado con id: " + id));
    }

    @Override
    @Transactional
    public MovimientoDTO registrar(MovimientoDTO dto) {
        Cuenta cuenta = recursoActivoConsulta.cuentaActivaPorId(dto.getCuentaId());
        BigDecimal nuevoSaldo = calcularSaldoTrasMovimiento(cuenta, dto.getValor());

        cuenta.setSaldoDisponible(nuevoSaldo);
        cuentaRepository.save(cuenta);

        Movimiento movimiento = new Movimiento();
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setValor(dto.getValor());
        movimiento.setSaldo(nuevoSaldo);
        movimiento.setTipoMovimiento(resolverTipoMovimiento(dto.getValor()));
        movimiento.setCuenta(cuenta);

        return toDto(movimientoRepository.save(movimiento));
    }

    private BigDecimal calcularSaldoTrasMovimiento(Cuenta cuenta, BigDecimal valor) {
        BigDecimal nuevoSaldo = cuenta.getSaldoDisponible().add(valor);
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new SaldoInsuficienteException();
        }
        return nuevoSaldo;
    }

    private String resolverTipoMovimiento(BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) >= 0) {
            return TIPO_DEPOSITO;
        }
        return TIPO_RETIRO;
    }

    private MovimientoDTO toDto(Movimiento movimiento) {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setId(movimiento.getId());
        dto.setFecha(movimiento.getFecha());
        dto.setTipoMovimiento(movimiento.getTipoMovimiento());
        dto.setValor(movimiento.getValor());
        dto.setSaldo(movimiento.getSaldo());
        dto.setCuentaId(movimiento.getCuenta().getId());
        return dto;
    }
}
