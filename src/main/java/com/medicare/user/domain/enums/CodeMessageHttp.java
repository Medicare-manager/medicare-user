package com.medicare.user.domain.enums;

public enum CodeMessageHttp {
    DUZENTOS("200"),
    QUATROCENTOS("400"),
    QUINHENTOS("500");

    private String codeError;

    CodeMessageHttp(String codeError) {
        this.codeError = codeError;
    }

    public String getCodeError() {
        return codeError;
    }
}
