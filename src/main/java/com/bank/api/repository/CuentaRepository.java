package com.bank.api.repository;

import com.bank.api.model.Cuenta;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);

    List<Cuenta> findByEstadoTrue();

    Optional<Cuenta> findByIdAndEstadoTrue(Long id);
}
