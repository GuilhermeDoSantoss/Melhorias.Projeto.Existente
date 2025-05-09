package com.luizalebs.comunicacao_api.business.service;

import com.luizalebs.comunicacao_api.api.dto.ComunicacaoInDTO;
import com.luizalebs.comunicacao_api.api.dto.ComunicacaoOutDTO;
import com.luizalebs.comunicacao_api.api.exception.ComunicacaoNaoEncontradaException;
import com.luizalebs.comunicacao_api.business.converter.ComunicacaoConverter;
import com.luizalebs.comunicacao_api.infraestructure.entities.ComunicacaoEntity;
import com.luizalebs.comunicacao_api.infraestructure.enums.StatusEnvioEnum;
import com.luizalebs.comunicacao_api.infraestructure.repositories.ComunicacaoRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ComunicacaoService {

    private final ComunicacaoRepository repository;
    private final ComunicacaoConverter converter;
    private final EmailNotificationService emailNotificationService;

    public ComunicacaoService(ComunicacaoRepository repository, ComunicacaoConverter converter, EmailNotificationService emailNotificationService) {
        this.repository = repository;
        this.converter = converter;
        this.emailNotificationService = emailNotificationService;
    }

    public ComunicacaoOutDTO agendarComunicacao(ComunicacaoInDTO dto) {
        if (Objects.isNull(dto)) {
            throw new IllegalArgumentException("DTO de entrada (ComunicacaoInDTO) não pode ser nulo.");
        }
        dto.setStatusEnvio(StatusEnvioEnum.PENDENTE);
        ComunicacaoEntity entity = converter.paraEntity(dto);
        ComunicacaoEntity savedEntity = repository.save(entity);
        
        boolean notificacaoEnviada = emailNotificationService.enviarNotificacaoEmail(
            savedEntity.getEmailDestinatario(), 
            "Agendamento de Comunicação Confirmado", 
            "Sua comunicação foi agendada para: " + savedEntity.getDataHoraEnvio() + ". Status: PENDENTE."
        );

        if (!notificacaoEnviada) {
            System.err.println("Falha ao enviar e-mail de confirmação de agendamento para: " + savedEntity.getEmailDestinatario());
        }

        return converter.paraDTO(savedEntity);
    }

    public ComunicacaoOutDTO buscarStatusComunicacao(String emailDestinatario) {
        if (emailDestinatario == null || emailDestinatario.isBlank()) {
            throw new IllegalArgumentException("E-mail do destinatário não pode ser nulo ou vazio para busca.");
        }
        ComunicacaoEntity entity = repository.findByEmailDestinatario(emailDestinatario);
        if (Objects.isNull(entity)) {
            throw new ComunicacaoNaoEncontradaException(emailDestinatario);
        }
        return converter.paraDTO(entity);
    }

    public ComunicacaoOutDTO alterarStatusComunicacao(String emailDestinatario) {
        if (emailDestinatario == null || emailDestinatario.isBlank()) {
            throw new IllegalArgumentException("E-mail do destinatário não pode ser nulo ou vazio para alteração de status.");
        }
        ComunicacaoEntity entity = repository.findByEmailDestinatario(emailDestinatario);
        if (Objects.isNull(entity)) {
            throw new ComunicacaoNaoEncontradaException(emailDestinatario);
        }
        entity.setStatusEnvio(StatusEnvioEnum.CANCELADO);
        ComunicacaoEntity updatedEntity = repository.save(entity);

        boolean notificacaoEnviada = emailNotificationService.enviarNotificacaoEmail(
            updatedEntity.getEmailDestinatario(),
            "Agendamento de Comunicação Cancelado",
            "Sua comunicação agendada para: " + updatedEntity.getDataHoraEnvio() + " foi CANCELADA."
        );

        if (!notificacaoEnviada) {
            System.err.println("Falha ao enviar e-mail de cancelamento para: " + updatedEntity.getEmailDestinatario());
        }

        return converter.paraDTO(updatedEntity);
    }

}
