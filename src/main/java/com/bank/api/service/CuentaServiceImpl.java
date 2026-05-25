package com.bank.api.service;

import com.bank.api.dto.CuentaDTO;
import com.bank.api.model.Cliente;
import com.bank.api.model.Cuenta;
import com.bank.api.repository.CuentaRepository;
import com.bank.api.service.support.RecursoActivoConsulta;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;
    private final RecursoActivoConsulta recursoActivoConsulta;

    @Override
    @Transactional(readOnly = true)
    public List<CuentaDTO> listarTodos() {
        return cuentaRepository.findByEstadoTrue().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaDTO obtenerPorId(Long id) {
        return toDto(buscarActiva(id));
    }

    @Override
    @Transactional
    public CuentaDTO crear(CuentaDTO dto) {
        Cliente cliente = recursoActivoConsulta.clienteActivoPorId(dto.getClienteId());

        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(dto.getNumeroCuenta());
        cuenta.setTipoCuenta(dto.getTipoCuenta());
        cuenta.setSaldoInicial(dto.getSaldoInicial());
        cuenta.setSaldoDisponible(dto.getSaldoInicial());
        cuenta.setEstado(true);
        cuenta.setCliente(cliente);
        return toDto(cuentaRepository.save(cuenta));
    }

    @Override
    @Transactional
    public CuentaDTO actualizar(Long id, CuentaDTO dto) {
        Cuenta cuenta = buscarActiva(id);
        cuenta.setNumeroCuenta(dto.getNumeroCuenta());
        cuenta.setTipoCuenta(dto.getTipoCuenta());
        cuenta.setSaldoInicial(dto.getSaldoInicial());
        if (dto.getSaldoDisponible() != null) {
            cuenta.setSaldoDisponible(dto.getSaldoDisponible());
        }
        return toDto(cuentaRepository.save(cuenta));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Cuenta cuenta = buscarActiva(id);
        cuenta.setEstado(false);
        cuentaRepository.save(cuenta);
    }

    private Cuenta buscarActiva(Long id) {
        return recursoActivoConsulta.cuentaActivaPorId(id);
    }

    private CuentaDTO toDto(Cuenta cuenta) {
        CuentaDTO dto = new CuentaDTO();
        dto.setId(cuenta.getId());
        dto.setNumeroCuenta(cuenta.getNumeroCuenta());
        dto.setTipoCuenta(cuenta.getTipoCuenta());
        dto.setSaldoInicial(cuenta.getSaldoInicial());
        dto.setSaldoDisponible(cuenta.getSaldoDisponible());
        dto.setEstado(cuenta.isEstado());
        dto.setClienteId(cuenta.getCliente().getClienteId());
        return dto;
    }
}
