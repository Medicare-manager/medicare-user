package com.medicare.user.application.Service;

import com.medicare.user.application.Request.UserRequest;
import com.medicare.user.application.Response.RegisterResponse;
import com.medicare.user.application.Response.ResponseDetail;
import com.medicare.user.domain.entity.User;
import com.medicare.user.domain.enums.Role;
import com.medicare.user.infrastructure.persistence.UserRepositoryImpl;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserService {
    private final UserRepositoryImpl userRepositoryImpl;

    public UserService(UserRepositoryImpl userRepositoryImpl) {
        this.userRepositoryImpl = userRepositoryImpl;
    }

    @Transactional
    public ResponseEntity<RegisterResponse> registerUser(UserRequest data) {
        try {
            Role roleConvertido = Role.fromString(data.getRole());
            System.out.println("roleConvertido " + roleConvertido);
            if (this.userRepositoryImpl.findByEmail(data.getEmail()) != null) {
                ResponseDetail errorDetail = new ResponseDetail(
                        "400",
                        "Falha no cadastro do usuário",
                        "Login já existe no sistema."
                );
                RegisterResponse errorResponse = new RegisterResponse(Collections.singletonList(errorDetail), true);
                System.out.println("errorResponse " + errorResponse.getErros());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            String encryptedPassword = new BCryptPasswordEncoder().encode(data.getPassword());

            User newUser = new User(data.getName(), data.getEmail(), encryptedPassword, roleConvertido);

            this.userRepositoryImpl.save(newUser);

            ResponseDetail successDetail = new ResponseDetail(
                    "200",
                    "Usuário cadastrado no sistema",
                    "Registro realizado com sucesso"
            );
            RegisterResponse successResponse = new RegisterResponse(Collections.singletonList(successDetail));
            return ResponseEntity.ok().body(successResponse);

        } catch (Exception e) {
            ResponseDetail errorDetail = new ResponseDetail(
                    "500",
                    "Erro interno no servidor",
                    "Um erro inesperado aconteceu no servidor: " + e.getMessage()
            );
            RegisterResponse errorResponse = new RegisterResponse(Collections.singletonList(errorDetail), true);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
