package com.bank.api.repository;

import com.bank.api.model.Movimiento;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByCuenta_Cliente_ClienteIdAndFechaBetween(
            Long clienteId, LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
