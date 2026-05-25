package com.bank.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClienteDTO {

    private Long clienteId;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String genero;

    private Integer edad;

    @NotBlank(message = "La identificacion es obligatoria")
    private String identificacion;

    private String direccion;

    private String telefono;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 4, message = "La contraseña debe tener al menos 4 caracteres")
    private String contrasena;

    private boolean estado;
}
