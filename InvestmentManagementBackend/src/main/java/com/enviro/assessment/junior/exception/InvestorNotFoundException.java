package com.enviro.assessment.junior.exception;

public class InvestorNotFoundException extends RuntimeException {
    public InvestorNotFoundException(Long id) {
        super("Investor not found with ID: " + id);
    }
}