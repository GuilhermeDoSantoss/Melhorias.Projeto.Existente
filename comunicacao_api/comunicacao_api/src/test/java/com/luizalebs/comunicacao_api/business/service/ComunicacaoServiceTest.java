package com.luizalebs.comunicacao_api.business.service;

import com.luizalebs.comunicacao_api.api.dto.ComunicacaoInDTO;
import com.luizalebs.comunicacao_api.api.dto.ComunicacaoOutDTO;
import com.luizalebs.comunicacao_api.api.exception.ComunicacaoNaoEncontradaException;
// Make sure you have a custom IllegalArgumentException or use java.lang.IllegalArgumentException
// import com.luizalebs.comunicacao_api.api.exception.IllegalArgumentException; 
import com.luizalebs.comunicacao_api.business.converter.ComunicacaoConverter;
import com.luizalebs.comunicacao_api.infraestructure.entities.ComunicacaoEntity;
import com.luizalebs.comunicacao_api.infraestructure.enums.StatusEnvioEnum;
import com.luizalebs.comunicacao_api.infraestructure.repositories.ComunicacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComunicacaoServiceTest {

    @Mock
    private ComunicacaoRepository repository;

    @Mock
    private ComunicacaoConverter converter;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private ComunicacaoService service;

    private ComunicacaoInDTO comunicacaoInDTO;
    private ComunicacaoEntity comunicacaoEntity;
    private ComunicacaoOutDTO comunicacaoOutDTO;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.now().plusDays(1);

        comunicacaoInDTO = new ComunicacaoInDTO();
        // Assuming ComunicacaoInDTO has these setters - will be reviewed based on actual DTO
        // For now, setting fields directly if setters are the issue in controller test
        comunicacaoInDTO.dataHoraEnvio = testDateTime; 
        comunicacaoInDTO.destinatario = "destinatario@example.com";
        comunicacaoInDTO.mensagem = "Olá Mundo";

        comunicacaoEntity = new ComunicacaoEntity();
        comunicacaoEntity.setId(1L);
        comunicacaoEntity.setEmailDestinatario("destinatario@example.com");
        comunicacaoEntity.setDataHoraEnvio(testDateTime); // Assuming entity uses LocalDateTime
        comunicacaoEntity.setMensagem("Olá Mundo");
        comunicacaoEntity.setStatusEnvio(StatusEnvioEnum.PENDENTE);

        comunicacaoOutDTO = new ComunicacaoOutDTO();
        // Assuming ComunicacaoOutDTO has these setters or public fields
        comunicacaoOutDTO.id = 1L;
        comunicacaoOutDTO.emailDestinatario = "destinatario@example.com";
        comunicacaoOutDTO.dataHoraEnvio = testDateTime; // Assuming DTO uses LocalDateTime
        comunicacaoOutDTO.mensagem = "Olá Mundo";
        comunicacaoOutDTO.statusEnvio = StatusEnvioEnum.PENDENTE;
    }

    @Test
    void agendarComunicacao_deveAgendarERetornarDTO_quandoSucesso() {
        when(converter.paraEntity(any(ComunicacaoInDTO.class))).thenReturn(comunicacaoEntity);
        when(repository.save(any(ComunicacaoEntity.class))).thenReturn(comunicacaoEntity);
        when(converter.paraDTO(any(ComunicacaoEntity.class))).thenReturn(comunicacaoOutDTO);
        when(emailNotificationService.enviarNotificacaoEmail(anyString(), anyString(), anyString())).thenReturn(true);

        ComunicacaoOutDTO resultado = service.agendarComunicacao(comunicacaoInDTO);

        assertNotNull(resultado);
        assertEquals(StatusEnvioEnum.PENDENTE, resultado.statusEnvio);
        assertEquals(comunicacaoOutDTO.emailDestinatario, resultado.emailDestinatario);
        verify(repository, times(1)).save(comunicacaoEntity);
        verify(emailNotificationService, times(1)).enviarNotificacaoEmail(
            comunicacaoEntity.getEmailDestinatario(),
            "Agendamento de Comunicação Confirmado",
            "Sua comunicação foi agendada para: " + comunicacaoEntity.getDataHoraEnvio() + ". Status: PENDENTE."
        );
    }

    @Test
    void agendarComunicacao_deveLancarIllegalArgumentException_quandoDtoNulo() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.agendarComunicacao(null);
        });
        assertEquals("DTO de entrada (ComunicacaoInDTO) não pode ser nulo.", exception.getMessage());
    }
    
    @Test
    void agendarComunicacao_deveAgendarMesmoComFalhaNotificacaoEmail() {
        when(converter.paraEntity(any(ComunicacaoInDTO.class))).thenReturn(comunicacaoEntity);
        when(repository.save(any(ComunicacaoEntity.class))).thenReturn(comunicacaoEntity);
        when(converter.paraDTO(any(ComunicacaoEntity.class))).thenReturn(comunicacaoOutDTO);
        when(emailNotificationService.enviarNotificacaoEmail(anyString(), anyString(), anyString())).thenReturn(false);

        ComunicacaoOutDTO resultado = service.agendarComunicacao(comunicacaoInDTO);

        assertNotNull(resultado);
        assertEquals(StatusEnvioEnum.PENDENTE, resultado.statusEnvio);
        verify(repository, times(1)).save(comunicacaoEntity);
        verify(emailNotificationService, times(1)).enviarNotificacaoEmail(anyString(), anyString(), anyString());
    }


    @Test
    void buscarStatusComunicacao_deveRetornarDTO_quandoEncontrado() {
        when(repository.findByEmailDestinatario(anyString())).thenReturn(comunicacaoEntity);
        when(converter.paraDTO(any(ComunicacaoEntity.class))).thenReturn(comunicacaoOutDTO);

        ComunicacaoOutDTO resultado = service.buscarStatusComunicacao("destinatario@example.com");

        assertNotNull(resultado);
        assertEquals(comunicacaoOutDTO.id, resultado.id);
        verify(repository, times(1)).findByEmailDestinatario("destinatario@example.com");
    }

    @Test
    void buscarStatusComunicacao_deveLancarComunicacaoNaoEncontradaException_quandoNaoEncontrado() {
        when(repository.findByEmailDestinatario(anyString())).thenReturn(null);
        String email = "inexistente@example.com";
        
        Exception exception = assertThrows(ComunicacaoNaoEncontradaException.class, () -> {
            service.buscarStatusComunicacao(email);
        });
        assertEquals("Comunicação não encontrada para o e-mail: " + email, exception.getMessage());
    }

    @Test
    void buscarStatusComunicacao_deveLancarIllegalArgumentException_quandoEmailNuloOuVazio() {
        Exception exceptionNulo = assertThrows(IllegalArgumentException.class, () -> {
            service.buscarStatusComunicacao(null);
        });
        assertEquals("E-mail do destinatário não pode ser nulo ou vazio para busca.", exceptionNulo.getMessage());
        
        Exception exceptionVazio = assertThrows(IllegalArgumentException.class, () -> {
            service.buscarStatusComunicacao("");
        });
        assertEquals("E-mail do destinatário não pode ser nulo ou vazio para busca.", exceptionVazio.getMessage());
    }

    @Test
    void alterarStatusComunicacao_deveCancelarERetornarDTO_quandoSucesso() {
        comunicacaoEntity.setStatusEnvio(StatusEnvioEnum.PENDENTE); 
        ComunicacaoEntity entidadeCancelada = new ComunicacaoEntity();
        entidadeCancelada.setId(comunicacaoEntity.getId());
        entidadeCancelada.setEmailDestinatario(comunicacaoEntity.getEmailDestinatario());
        entidadeCancelada.setDataHoraEnvio(comunicacaoEntity.getDataHoraEnvio());
        entidadeCancelada.setMensagem(comunicacaoEntity.getMensagem());
        entidadeCancelada.setStatusEnvio(StatusEnvioEnum.CANCELADO);

        ComunicacaoOutDTO dtoCancelado = new ComunicacaoOutDTO();
        dtoCancelado.id = comunicacaoOutDTO.id;
        dtoCancelado.statusEnvio = StatusEnvioEnum.CANCELADO;
        dtoCancelado.dataHoraEnvio = comunicacaoOutDTO.dataHoraEnvio;
        dtoCancelado.emailDestinatario = comunicacaoOutDTO.emailDestinatario; // Added this line

        when(repository.findByEmailDestinatario(anyString())).thenReturn(comunicacaoEntity);
        when(repository.save(any(ComunicacaoEntity.class))).thenReturn(entidadeCancelada); 
        when(converter.paraDTO(any(ComunicacaoEntity.class))).thenReturn(dtoCancelado);
        when(emailNotificationService.enviarNotificacaoEmail(anyString(), anyString(), anyString())).thenReturn(true);

        ComunicacaoOutDTO resultado = service.alterarStatusComunicacao("destinatario@example.com");

        assertNotNull(resultado);
        assertEquals(StatusEnvioEnum.CANCELADO, resultado.statusEnvio);
        verify(repository, times(1)).save(any(ComunicacaoEntity.class)); 
        verify(emailNotificationService, times(1)).enviarNotificacaoEmail(
            entidadeCancelada.getEmailDestinatario(),
            "Agendamento de Comunicação Cancelado",
            "Sua comunicação agendada para: " + entidadeCancelada.getDataHoraEnvio() + " foi CANCELADA."
        );
    }

    @Test
    void alterarStatusComunicacao_deveLancarComunicacaoNaoEncontradaException_quandoNaoEncontrado() {
        when(repository.findByEmailDestinatario(anyString())).thenReturn(null);
        String email = "inexistente@example.com";

        Exception exception = assertThrows(ComunicacaoNaoEncontradaException.class, () -> {
            service.alterarStatusComunicacao(email);
        });
         assertEquals("Comunicação não encontrada para o e-mail: " + email, exception.getMessage());
    }
    
    @Test
    void alterarStatusComunicacao_deveLancarIllegalArgumentException_quandoEmailNuloOuVazio() {
        Exception exceptionNulo = assertThrows(IllegalArgumentException.class, () -> {
            service.alterarStatusComunicacao(null);
        });
        assertEquals("E-mail do destinatário não pode ser nulo ou vazio para alteração de status.", exceptionNulo.getMessage());
        
        Exception exceptionVazio = assertThrows(IllegalArgumentException.class, () -> {
            service.alterarStatusComunicacao("");
        });
        assertEquals("E-mail do destinatário não pode ser nulo ou vazio para alteração de status.", exceptionVazio.getMessage());
    }
} 