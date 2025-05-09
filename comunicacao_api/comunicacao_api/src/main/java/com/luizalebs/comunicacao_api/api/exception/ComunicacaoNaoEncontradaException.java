package com.luizalebs.comunicacao_api.api.exception;

public class ComunicacaoNaoEncontradaException extends EntityNotFoundException {

    private static final String DEFAULT_MESSAGE_PREFIX = "Comunicação não encontrada";

    public ComunicacaoNaoEncontradaException() {
        super(DEFAULT_MESSAGE_PREFIX + ".");
    }

    public ComunicacaoNaoEncontradaException(String emailDestinatario) {
        super(String.format("%s para o e-mail: %s", DEFAULT_MESSAGE_PREFIX, emailDestinatario));
    }
} 