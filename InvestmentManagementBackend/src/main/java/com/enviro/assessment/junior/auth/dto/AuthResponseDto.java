package com.enviro.assessment.junior.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AuthResponseDto {
    private String token;
    private Long investorId;
    private String firstName;
    private String lastName;
    private String email;
}