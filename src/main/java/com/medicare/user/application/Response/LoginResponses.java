package com.medicare.user.application.Response;

public class LoginResponses {
    private String codigo;
    private String mensagem;
    private String detalhes;

    public LoginResponses(String codigo, String mensagem, String detalhes) {
        this.codigo = codigo;
        this.mensagem = mensagem;
        this.detalhes = detalhes;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public void setDetalhes(String detalhes) {
        this.detalhes = detalhes;
    }
}
