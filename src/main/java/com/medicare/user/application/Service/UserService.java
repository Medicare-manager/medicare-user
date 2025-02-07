package com.medicare.user.application.Service;

import com.medicare.user.application.Response.LoginResponse;
import com.medicare.user.application.Request.AuthenticationRequest;
import com.medicare.user.application.Request.UserRequest;
import com.medicare.user.application.Response.LoginResponses;
import com.medicare.user.application.Response.RegisterResponse;
import com.medicare.user.application.Response.ResponseDetail;
import com.medicare.user.domain.entity.User;
import com.medicare.user.domain.enums.Role;
import com.medicare.user.infrastructure.configuration.security.TokenService;
import com.medicare.user.infrastructure.persistence.UserRepositoryImpl;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;

import java.util.Collections;

@Service
public class UserService {
    private final UserRepositoryImpl userRepositoryImpl;

    private final AuthenticationManager authenticationManager;

    private final TokenService tokenService;

    private final RabbitMQProducer rabbitMQProducer;

    public UserService(UserRepositoryImpl userRepositoryImpl, AuthenticationManager authenticationManager,
                       TokenService tokenService, RabbitMQProducer rabbitMQProducer) {
        this.userRepositoryImpl = userRepositoryImpl;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.rabbitMQProducer = rabbitMQProducer;
    }

    public ResponseEntity<?> login(AuthenticationRequest data) {
        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
            var auth = this.authenticationManager.authenticate(usernamePassword);

            var token = tokenService.generateToken((User) auth.getPrincipal());

            // Envia mensagem para a fila RabbitMQ
            rabbitMQProducer.sendToEmailQueue(
                    data.login(),
                    "Login realizado com sucesso!",
                    "Olá, você acabou de fazer login no sistema."
            );

            // Retorna o token
            return ResponseEntity.ok(new LoginResponse(token));

        } catch (BadCredentialsException | InternalAuthenticationServiceException e) {
            LoginResponses errorResponse = new LoginResponses(
                    "400",
                    "Login e/ou senha inválido.",
                    "Corrija o login"
            );
            // Retorna erro 401 em caso de falha na autenticação
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

        } catch (Exception e) {
            // Captura exceções genéricas e retorna erro 500
            LoginResponses errorResponse = new LoginResponses(
                    "500",
                    "Erro interno no servidor",
                    "Um erro inesperado aconteceu no servidor: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
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
