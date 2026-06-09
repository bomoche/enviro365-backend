package com.enviro.assessment.junior.auth;

import com.enviro.assessment.junior.auth.dto.AuthResponseDto;
import com.enviro.assessment.junior.auth.dto.LoginRequestDto;
import com.enviro.assessment.junior.auth.dto.RegisterRequestDto;
import com.enviro.assessment.junior.entity.Investor;
import com.enviro.assessment.junior.exception.WithdrawalValidationException;
import com.enviro.assessment.junior.repository.InvestorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final InvestorRepository investorRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponseDto register(RegisterRequestDto dto) {

        // Check if email already exists
        if (investorRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new WithdrawalValidationException("Email already registered.");
        }

        // Create investor with hashed password
        Investor investor = Investor.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .dateOfBirth(dto.getDateOfBirth())
                .build();

        investorRepository.save(investor);

        String token = jwtService.generateToken(investor.getEmail());

        return AuthResponseDto.builder()
                .token(token)
                .investorId(investor.getId())
                .firstName(investor.getFirstName())
                .lastName(investor.getLastName())
                .email(investor.getEmail())
                .build();
    }

    public AuthResponseDto login(LoginRequestDto dto) {

        Investor investor = investorRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new WithdrawalValidationException("Invalid email or password."));

        if (!passwordEncoder.matches(dto.getPassword(), investor.getPassword())) {
            throw new WithdrawalValidationException("Invalid email or password.");
        }

        String token = jwtService.generateToken(investor.getEmail());

        return AuthResponseDto.builder()
                .token(token)
                .investorId(investor.getId())
                .firstName(investor.getFirstName())
                .lastName(investor.getLastName())
                .email(investor.getEmail())
                .build();
    }
}