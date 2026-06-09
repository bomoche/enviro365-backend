package com.enviro.assessment.junior.service;

import com.enviro.assessment.junior.dto.response.PortfolioResponseDto;
import com.enviro.assessment.junior.dto.response.ProductResponseDto;
import com.enviro.assessment.junior.entity.Investor;
import com.enviro.assessment.junior.exception.InvestorNotFoundException;
import com.enviro.assessment.junior.repository.InvestorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvestorService {

    private final InvestorRepository investorRepository;

    @Transactional(readOnly = true)
    public PortfolioResponseDto getPortfolio(Long investorId) {
        // Fetch investor or throw 404
        Investor investor = investorRepository.findById(investorId)
                .orElseThrow(() -> new InvestorNotFoundException(investorId));

        // Calculate age dynamically
        int age = Period.between(investor.getDateOfBirth(), LocalDate.now()).getYears();

        // Map products to DTOs — entities never leave this layer
        List<ProductResponseDto> productDtos = investor.getProducts().stream()
                .map(p -> ProductResponseDto.builder()
                        .productId(p.getId())
                        .productName(p.getProductName())
                        .productType(p.getProductType())
                        .balance(p.getBalance())
                        .build())
                .toList();

        return PortfolioResponseDto.builder()
                .investorId(investor.getId())
                .firstName(investor.getFirstName())
                .lastName(investor.getLastName())
                .email(investor.getEmail())
                .age(age)
                .products(productDtos)
                .build();
    }
}