package com.luizalebs.comunicacao_api.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacaoEmailRequestDTO {
    private String destinatarioEmail;
    private String assunto;
    private String corpoMensagem;
} 