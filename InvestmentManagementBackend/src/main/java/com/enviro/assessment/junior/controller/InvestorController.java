package com.enviro.assessment.junior.controller;

import com.enviro.assessment.junior.dto.response.PortfolioResponseDto;
import com.enviro.assessment.junior.service.InvestorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/investors")
@RequiredArgsConstructor
public class InvestorController {

    private final InvestorService investorService;

    @GetMapping("/{id}/portfolio")
    public ResponseEntity<PortfolioResponseDto> getPortfolio(@PathVariable Long id) {
        return ResponseEntity.ok(investorService.getPortfolio(id));
    }
}