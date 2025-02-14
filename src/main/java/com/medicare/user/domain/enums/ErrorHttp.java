package com.medicare.user.domain.enums;

public enum ErrorHttp {
    DUZENTOS("200"),
    QUATROCENTOS("400"),
    QUINHENTOS("500");

    private String codeError;

    ErrorHttp(String codeError) {
        this.codeError = codeError;
    }

    public String getCodeError() {
        return codeError;
    }
}
