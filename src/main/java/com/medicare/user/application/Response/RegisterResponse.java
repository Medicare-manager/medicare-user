package com.medicare.user.application.Response;

import java.util.List;

public class RegisterResponse {
    private List<ResponseDetail> sucess;
    private List<ResponseDetail> erros;

    public RegisterResponse(List<ResponseDetail> sucess) {
        this.sucess = sucess;
    }

    public RegisterResponse(List<ResponseDetail> erros, boolean isError) {
        this.erros = erros;
    }

    public List<ResponseDetail> getSucess() {
        return sucess;
    }

    public void setSucess(List<ResponseDetail> sucess) {
        this.sucess = sucess;
    }

    public List<ResponseDetail> getErros() {
        return erros;
    }

    public void setErros(List<ResponseDetail> erros) {
        this.erros = erros;
    }
}
