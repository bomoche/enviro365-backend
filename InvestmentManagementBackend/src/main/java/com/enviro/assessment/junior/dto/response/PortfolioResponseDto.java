package com.enviro.assessment.junior.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioResponseDto {

    private Long investorId;
    private String firstName;
    private String lastName;
    private String email;

    // Calculated at response time — not stored in the database
    private int age;

    private List<ProductResponseDto> products;
}