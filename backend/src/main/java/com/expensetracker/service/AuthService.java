package com.expensetracker.service;

import com.expensetracker.dto.AuthRequest;
import com.expensetracker.dto.AuthResponse;
import com.expensetracker.dto.RegisterRequest;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.JwtUtil;
import com.expensetracker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User saved = userRepository.save(user);

        UserPrincipal principal = new UserPrincipal(saved);
        String token = jwtUtil.generateToken(principal);

        return new AuthResponse(token, saved.getId(), saved.getName(), saved.getEmail());
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtUtil.generateToken(principal);

        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail());
    }
}
