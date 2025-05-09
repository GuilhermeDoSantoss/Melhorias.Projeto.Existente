package com.luizalebs.comunicacao_api.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luizalebs.comunicacao_api.api.dto.ComunicacaoInDTO;
import com.luizalebs.comunicacao_api.api.dto.ComunicacaoOutDTO;
import com.luizalebs.comunicacao_api.api.exception.ComunicacaoNaoEncontradaException;
import com.luizalebs.comunicacao_api.business.service.ComunicacaoService;
import com.luizalebs.comunicacao_api.infraestructure.enums.StatusEnvioEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ComunicacaoController.class)
class ComunicacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ComunicacaoService service;

    @Autowired
    private ObjectMapper objectMapper; // For converting DTOs to JSON

    private ComunicacaoInDTO comunicacaoInDTO;
    private ComunicacaoOutDTO comunicacaoOutDTO;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2024, 5, 21, 10, 0, 0);

        comunicacaoInDTO = new ComunicacaoInDTO();
        comunicacaoInDTO.setDataHoraEnvio(testDateTime);
        comunicacaoInDTO.setDestinatario("destinatario@example.com");
        comunicacaoInDTO.setMensagem("Olá Teste");

        comunicacaoOutDTO = new ComunicacaoOutDTO();
        comunicacaoOutDTO.setId(1L);
        comunicacaoOutDTO.setDataHoraEnvio(testDateTime);
        comunicacaoOutDTO.setEmailDestinatario("destinatario@example.com");
        comunicacaoOutDTO.setMensagem("Olá Teste");
        comunicacaoOutDTO.setStatusEnvio(StatusEnvioEnum.PENDENTE);
    }

    @Test
    void agendar_deveRetornarOkEComunicacaoOutDTO_quandoSucesso() throws Exception {
        when(service.agendarComunicacao(any(ComunicacaoInDTO.class))).thenReturn(comunicacaoOutDTO);

        mockMvc.perform(post("/comunicacao/agendar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(comunicacaoInDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.emailDestinatario").value("destinatario@example.com"))
                .andExpect(jsonPath("$.statusEnvio").value("PENDENTE"));
                // Add more specific date/time assertion if exact format is critical
                // .andExpect(jsonPath("$.dataHoraEnvio").value(testDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
    }
    
    @Test
    void agendar_deveRetornarBadRequest_quandoInputInvalido() throws Exception {
        // Simulate service throwing IllegalArgumentException due to invalid DTO (e.g. null)
        when(service.agendarComunicacao(any(ComunicacaoInDTO.class)))
            .thenThrow(new IllegalArgumentException("DTO de entrada (ComunicacaoInDTO) não pode ser nulo."));

        mockMvc.perform(post("/comunicacao/agendar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ComunicacaoInDTO()))) // Send potentially invalid DTO
                .andExpect(status().isBadRequest())
                .andExpect(content().string("DTO de entrada (ComunicacaoInDTO) não pode ser nulo."));
    }

    @Test
    void buscarStatus_deveRetornarOkEComunicacaoOutDTO_quandoEncontrado() throws Exception {
        when(service.buscarStatusComunicacao(anyString())).thenReturn(comunicacaoOutDTO);

        mockMvc.perform(get("/comunicacao")
                .param("emailDestinatario", "destinatario@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.statusEnvio").value("PENDENTE"));
    }

    @Test
    void buscarStatus_deveRetornarNotFound_quandoNaoEncontrado() throws Exception {
        String email = "naoexiste@example.com";
        when(service.buscarStatusComunicacao(email))
            .thenThrow(new ComunicacaoNaoEncontradaException(email));

        mockMvc.perform(get("/comunicacao")
                .param("emailDestinatario", email))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Comunicação não encontrada para o e-mail: " + email));
    }
    
    @Test
    void buscarStatus_deveRetornarBadRequest_quandoEmailInvalido() throws Exception {
        String email = ""; // Invalid email
        when(service.buscarStatusComunicacao(email))
            .thenThrow(new IllegalArgumentException("E-mail do destinatário não pode ser nulo ou vazio para busca."));

        mockMvc.perform(get("/comunicacao")
                .param("emailDestinatario", email))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("E-mail do destinatário não pode ser nulo ou vazio para busca."));
    }

    @Test
    void cancelarStatus_deveRetornarOkEComunicacaoOutDTO_quandoSucesso() throws Exception {
        ComunicacaoOutDTO canceladoDTO = new ComunicacaoOutDTO();
        canceladoDTO.setId(1L);
        canceladoDTO.setStatusEnvio(StatusEnvioEnum.CANCELADO);
        canceladoDTO.setDataHoraEnvio(testDateTime);
        canceladoDTO.setEmailDestinatario("destinatario@example.com");

        when(service.alterarStatusComunicacao(anyString())).thenReturn(canceladoDTO);

        mockMvc.perform(patch("/comunicacao/cancelar")
                .param("emailDestinatario", "destinatario@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.statusEnvio").value("CANCELADO"));
    }

    @Test
    void cancelarStatus_deveRetornarNotFound_quandoNaoEncontrado() throws Exception {
        String email = "naoexiste@example.com";
        when(service.alterarStatusComunicacao(email))
            .thenThrow(new ComunicacaoNaoEncontradaException(email));

        mockMvc.perform(patch("/comunicacao/cancelar")
                .param("emailDestinatario", email))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Comunicação não encontrada para o e-mail: " + email));
    }
} 