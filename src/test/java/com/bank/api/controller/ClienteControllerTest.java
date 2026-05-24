package com.bank.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bank.api.dto.ClienteDTO;
import com.bank.api.service.ClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteService clienteService;

    @Test
    void listarClientes_debeRetornar200_conListaVacia() throws Exception {
        when(clienteService.listarTodos()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void crearCliente_debeRetornar201_cuandoDatosValidos() throws Exception {
        ClienteDTO request = new ClienteDTO();
        request.setNombre("Jose Lema");
        request.setIdentificacion("1234567890");
        request.setContrasena("1234");
        request.setGenero("M");
        request.setEdad(35);

        ClienteDTO response = new ClienteDTO();
        response.setClienteId(1L);
        response.setNombre("Jose Lema");
        response.setIdentificacion("1234567890");
        response.setContrasena("1234");

        when(clienteService.crear(any(ClienteDTO.class))).thenReturn(response);

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").value(1))
                .andExpect(jsonPath("$.nombre").value("Jose Lema"));
    }
}
