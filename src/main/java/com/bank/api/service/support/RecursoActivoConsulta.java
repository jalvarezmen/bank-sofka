package com.bank.api.service.support;

import com.bank.api.model.Cliente;
import com.bank.api.model.Cuenta;

/** Consultas compartidas de entidades activas (evita duplicar orElseThrow en varios servicios). */
public interface RecursoActivoConsulta {

    Cliente clienteActivoPorId(Long clienteId);

    Cuenta cuentaActivaPorId(Long cuentaId);
}
