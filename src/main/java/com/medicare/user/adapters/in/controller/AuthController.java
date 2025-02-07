package com.medicare.user.adapters.in.controller;

import com.medicare.user.adapters.in.dto.AuthenticationDTO;
import com.medicare.user.adapters.in.dto.LoginResponseDTO;
import com.medicare.user.application.Request.UserRequest;
import com.medicare.user.application.Response.RegisterResponse;
import com.medicare.user.application.Service.RabbitMQProducer;
import com.medicare.user.application.Service.UserService;
import com.medicare.user.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import com.medicare.user.infrastructure.configuration.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    private final UserService userService;

    @Autowired
    private RabbitMQProducer rabbitMQProducer;

    public AuthController(TokenService tokenService, UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @Operation(summary = "Autenticar usuário",
        responses = {
            @ApiResponse(responseCode = "200", description = "Retorna token."),
            @ApiResponse(responseCode = "401", description = "Falha no login."),
            @ApiResponse(responseCode = "500", description = "Falha interna no servidor"),
    })
    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid AuthenticationDTO data) {
        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
            var auth = this.authenticationManager.authenticate(usernamePassword);
            var token = tokenService.generateToken((User) auth.getPrincipal());
            System.out.println("email de login: " + data.login());
            rabbitMQProducer.sendToEmailQueue(
                    data.login(),
                    "Login realizado com sucesso!",
                    "Olá, você acabou de fazer login no sistema."
            );

            return ResponseEntity.ok(new LoginResponseDTO(token));
        } catch (BadCredentialsException | InternalAuthenticationServiceException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login e/ou senha inválido.");
        }
    }

    @Operation(summary = "Registrar novo usuário",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Usuário cadastrado no sistema"),
                    @ApiResponse(responseCode = "400", description = "Falha no cadastro do usuário"),
                    @ApiResponse(responseCode = "500", description = "Falha interna no servidor")
            })
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid UserRequest data) {
        return userService.registerUser(data);
    }

    @Operation(summary = "Faz logout do sistema.",
            responses = {
        @ApiResponse(responseCode = "200", description = "Usuário saiu do sistema"),
        @ApiResponse(responseCode = "401", description = "Falha ao sair do sistema"),
        @ApiResponse(responseCode = "500", description = "Falha interna no servidor"),
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("logout: "+request);
        // Obtenha o token do cabeçalho da autorização
        String token = extractTokenFromHeader(request);
        // Adicione o token à lista negra para invalidá-lo
        tokenService.addToBlacklist(token);

        return ResponseEntity.ok("Logout successful");
    }

    @Operation(summary = "Rota pública de teste")
    @PostMapping("/login2")
    public String login2() {
        return "funciona";
    }


    private String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // O token JWT começa após "Bearer "
        }
        return null;
    }


}
