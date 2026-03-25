package ru.bicev.submonitor.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.bicev.submonitor.dto.auth.JwtResponse;
import ru.bicev.submonitor.dto.auth.LoginRequest;
import ru.bicev.submonitor.dto.auth.SingupRequest;
import ru.bicev.submonitor.entity.Subscriber;
import ru.bicev.submonitor.exception.DuplicateSubscriberException;
import ru.bicev.submonitor.repository.SubscriberRepository;
import ru.bicev.submonitor.security.JwtUtil;
import ru.bicev.submonitor.security.UserDetailsImpl;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SubscriberRepository subscriberRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public JwtResponse registerSubscriber(SingupRequest request) {
        if (subscriberRepository.existsByUsername(request.username())
                || subscriberRepository.existsByEmail(request.email())) {
            throw new DuplicateSubscriberException("Username or email already in use");
        }

        var subscriber = Subscriber.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .build();

        var createdSub = subscriberRepository.save(subscriber);

        Authentication auth = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        SecurityContextHolder.getContext().setAuthentication(auth);
        String jwt = jwtUtil.generateToken(auth);

        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

        return new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername());

    }

    public JwtResponse login(LoginRequest request) {

        Authentication auth = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        SecurityContextHolder.getContext().setAuthentication(auth);
        String jwt = jwtUtil.generateToken(auth);

        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();

        return new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername());
    }

}
