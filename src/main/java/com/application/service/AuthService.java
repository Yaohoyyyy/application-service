package com.application.service;

import com.application.entity.Employee;
import com.application.entity.Token;
import com.application.model.AuthRequest;
import com.application.model.AuthResponse;
import com.application.repository.EmployeeRepository;
import com.application.repository.TokenRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class AuthService {

    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
            "MySuperSecretKeyForJWT2026MustBeAtLeast256BitsLong!!!".getBytes(StandardCharsets.UTF_8)
    );

    private final EmployeeRepository employeeRepository;
    private final TokenRepository tokenRepository;

    public AuthService(EmployeeRepository employeeRepository, TokenRepository tokenRepository) {
        this.employeeRepository = employeeRepository;
        this.tokenRepository = tokenRepository;
    }

    public AuthResponse login(AuthRequest request) {
        Employee employee = employeeRepository
                .findByLoginAndPassword(request.login(), request.password())
                .orElseThrow(() -> new RuntimeException("Invalid login or password"));

        String jwt = Jwts.builder()
                .subject(employee.getId().toString())
                .issuedAt(new Date())
                .signWith(SECRET_KEY)
                .compact();

        tokenRepository.save(new Token(jwt, employee.getId()));

        return new AuthResponse(jwt);
    }

    public boolean validateToken(String jwt) {
        return tokenRepository.findByToken(jwt).isPresent();
    }
}
