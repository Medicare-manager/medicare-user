package com.medicare.user.application.Service;

import com.medicare.user.application.Response.*;
import com.medicare.user.application.Request.AuthenticationRequest;
import com.medicare.user.application.Request.UserRequest;
import com.medicare.user.domain.entity.User;
import com.medicare.user.domain.enums.ErrorHttp;
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

            rabbitMQProducer.sendToEmailQueue(
                    data.login(),
                    "Login realizado com sucesso!",
                    "Olá, você acabou de fazer login no sistema."
            );

            // Retorna o token
            return ResponseEntity.ok(new LoginResponse(token));

        } catch (BadCredentialsException | InternalAuthenticationServiceException e) {
            LoginResponses errorResponse = new LoginResponses(
                    ErrorHttp.QUATROCENTOS.getCodeError(),
                    "Login e/ou senha inválido.",
                    "Corrija o login"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

        } catch (Exception e) {
            LoginResponses errorResponse = new LoginResponses(
                    ErrorHttp.QUINHENTOS.getCodeError(),
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
            
            if (this.userRepositoryImpl.findByEmail(data.getEmail()) != null) {
                RegisterResponse registerResponse = new RegisterResponse(
                    ErrorHttp.QUATROCENTOS.getCodeError(), 
                    "Falha no cadastro do usuário", 
                    "Login já existe no sistema.");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(registerResponse);
            }

            String encryptedPassword = new BCryptPasswordEncoder().encode(data.getPassword());

            User newUser = new User(data.getName(), data.getEmail(), encryptedPassword, roleConvertido);

            this.userRepositoryImpl.save(newUser);
            
            RegisterResponse registerResponse = new RegisterResponse(
                    ErrorHttp.DUZENTOS.getCodeError(),
                    "Usuário cadastrado no sistema",
                    "Registro realizado com sucesso");
            return ResponseEntity.ok().body(registerResponse);
        } catch (Exception e) {
            RegisterResponse registerResponse = new RegisterResponse(
                    ErrorHttp.QUINHENTOS.getCodeError(),
                    "Erro interno no servidor",
                    "Um erro inesperado aconteceu no servidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(registerResponse);
        }
    }
}
