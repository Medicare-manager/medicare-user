package com.medicare.user.adapters.in.controller;

import com.medicare.user.adapters.in.dto.AuthenticationDTO;
import com.medicare.user.adapters.in.dto.LoginResponseDTO;
import com.medicare.user.adapters.in.dto.UserDTO;
import com.medicare.user.adapters.out.UserRepository;
import com.medicare.user.domain.enums.Role;
import com.medicare.user.domain.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import com.medicare.user.infrastructure.configuration.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    private UserRepository userRepository;

    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid AuthenticationDTO data) {
        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(data.login(), data.password());
            var auth = this.authenticationManager.authenticate(usernamePassword);
            var token = tokenService.generateToken((User) auth.getPrincipal());
            return ResponseEntity.ok(new LoginResponseDTO(token));
        } catch (BadCredentialsException | InternalAuthenticationServiceException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login e/ou senha inválido.");
        }
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity register(@RequestBody @Valid UserDTO data) {
        Role roleConvertido = Role.fromString(data.getRole());
        if(this.userRepository.findByEmail(data.getEmail()) != null) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"message\":  \"Login ja existe no sistema.\"}");
        }

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.getPassword());

        User newUser = new User(data.getName(), data.getEmail(), encryptedPassword, roleConvertido);

        this.userRepository.saveUser(newUser);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"message\": \"Registro realizado com sucesso\"}");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("logout: "+request);
        // Obtenha o token do cabeçalho da autorização
        String token = extractTokenFromHeader(request);
        // Adicione o token à lista negra para invalidá-lo
        tokenService.addToBlacklist(token);

        return ResponseEntity.ok("Logout successful");
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // O token JWT começa após "Bearer "
        }
        return null;
    }


}
