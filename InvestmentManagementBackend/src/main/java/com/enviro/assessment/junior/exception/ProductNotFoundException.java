package com.enviro.assessment.junior.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long id) {
        super("Investment product not found or does not belong to this investor. ID: " + id);
    }
}