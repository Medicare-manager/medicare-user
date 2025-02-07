package com.medicare.user.domain.records;

public record ErrorDetail(
        String codigo,
        String mensagem,
        String detalhes
) {}
