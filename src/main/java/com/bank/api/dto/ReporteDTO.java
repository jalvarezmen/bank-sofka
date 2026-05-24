package com.bank.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReporteDTO {

    private LocalDateTime fecha;
    private String cliente;
    private String numeroCuenta;
    private String tipoCuenta;
    private BigDecimal saldoInicial;
    private boolean estado;
    private BigDecimal movimiento;
    private BigDecimal saldoDisponible;
}
