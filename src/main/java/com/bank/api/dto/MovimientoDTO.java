package com.bank.api.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MovimientoDTO {

    private Long id;

    private LocalDateTime fecha;

    private String tipoMovimiento;

    @NotNull(message = "El valor es obligatorio")
    private BigDecimal valor;

    private BigDecimal saldo;

    @NotNull(message = "La cuenta es obligatoria")
    private Long cuentaId;
}
