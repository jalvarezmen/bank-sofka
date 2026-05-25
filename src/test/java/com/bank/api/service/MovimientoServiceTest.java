package com.bank.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bank.api.dto.MovimientoDTO;
import com.bank.api.exception.SaldoInsuficienteException;
import com.bank.api.model.Cuenta;
import com.bank.api.model.Movimiento;
import com.bank.api.repository.CuentaRepository;
import com.bank.api.repository.MovimientoRepository;
import com.bank.api.service.support.RecursoActivoConsulta;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private RecursoActivoConsulta recursoActivoConsulta;

    @Mock
    private CuentaRepository cuentaRepository;

    @InjectMocks
    private MovimientoServiceImpl movimientoService;

    @Test
    void registrarMovimiento_debeActualizarSaldo_cuandoHaySaldo() {
        Cuenta cuenta = new Cuenta();
        cuenta.setId(1L);
        cuenta.setSaldoDisponible(new BigDecimal("2000"));
        cuenta.setEstado(true);

        when(recursoActivoConsulta.cuentaActivaPorId(1L)).thenReturn(cuenta);
        when(cuentaRepository.save(any(Cuenta.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(invocation -> {
            Movimiento movimiento = invocation.getArgument(0);
            movimiento.setId(10L);
            return movimiento;
        });

        MovimientoDTO dto = new MovimientoDTO();
        dto.setCuentaId(1L);
        dto.setValor(new BigDecimal("-575"));

        MovimientoDTO resultado = movimientoService.registrar(dto);

        assertEquals(new BigDecimal("1425"), resultado.getSaldo());
        assertEquals(new BigDecimal("-575"), resultado.getValor());
        assertEquals("Retiro", resultado.getTipoMovimiento());

        ArgumentCaptor<Cuenta> cuentaCaptor = ArgumentCaptor.forClass(Cuenta.class);
        verify(cuentaRepository).save(cuentaCaptor.capture());
        assertEquals(new BigDecimal("1425"), cuentaCaptor.getValue().getSaldoDisponible());
        verify(movimientoRepository).save(any(Movimiento.class));
    }

    @Test
    void registrarMovimiento_debeLanzarExcepcion_cuandoSaldoInsuficiente() {
        Cuenta cuenta = new Cuenta();
        cuenta.setId(1L);
        cuenta.setSaldoDisponible(new BigDecimal("100"));
        cuenta.setEstado(true);

        when(recursoActivoConsulta.cuentaActivaPorId(1L)).thenReturn(cuenta);

        MovimientoDTO dto = new MovimientoDTO();
        dto.setCuentaId(1L);
        dto.setValor(new BigDecimal("-200"));

        assertThrows(SaldoInsuficienteException.class, () -> movimientoService.registrar(dto));
    }
}
