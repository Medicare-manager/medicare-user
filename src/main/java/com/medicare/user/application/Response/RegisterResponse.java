package com.medicare.user.application.Response;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    private List<ResponseDetail> sucess;
    private List<ResponseDetail> erros;

    public RegisterResponse(List<ResponseDetail> sucess) {
        this.sucess = sucess;
    }

    public RegisterResponse(List<ResponseDetail> erros, boolean isError) {
        this.erros = erros;
    }
}
