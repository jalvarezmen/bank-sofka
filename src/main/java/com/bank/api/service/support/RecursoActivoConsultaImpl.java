package com.bank.api.service.support;

import com.bank.api.exception.RecursoNoEncontradoException;
import com.bank.api.model.Cliente;
import com.bank.api.model.Cuenta;
import com.bank.api.repository.ClienteRepository;
import com.bank.api.repository.CuentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecursoActivoConsultaImpl implements RecursoActivoConsulta {

    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;

    @Override
    public Cliente clienteActivoPorId(Long clienteId) {
        return clienteRepository
                .findByClienteIdAndEstadoTrue(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cliente no encontrado con id: " + clienteId));
    }

    @Override
    public Cuenta cuentaActivaPorId(Long cuentaId) {
        return cuentaRepository
                .findByIdAndEstadoTrue(cuentaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cuenta no encontrada con id: " + cuentaId));
    }
}
