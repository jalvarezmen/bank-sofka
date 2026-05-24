package com.bank.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CuentaDTO {

    private Long id;

    @NotBlank(message = "El numero de cuenta es obligatorio")
    private String numeroCuenta;

    @NotBlank(message = "El tipo de cuenta es obligatorio")
    private String tipoCuenta;

    @NotNull(message = "El saldo inicial es obligatorio")
    private BigDecimal saldoInicial;

    private BigDecimal saldoDisponible;

    private boolean estado;

    @NotNull(message = "El cliente es obligatorio")
    private Long clienteId;
}
