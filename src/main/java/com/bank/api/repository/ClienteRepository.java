package com.bank.api.repository;

import com.bank.api.model.Cliente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByIdentificacion(String identificacion);

    List<Cliente> findByEstadoTrue();

    Optional<Cliente> findByClienteIdAndEstadoTrue(Long clienteId);
}
