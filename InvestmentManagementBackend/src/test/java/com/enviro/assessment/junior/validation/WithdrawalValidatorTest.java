package com.enviro.assessment.junior.validation;

import com.enviro.assessment.junior.entity.InvestmentProduct;
import com.enviro.assessment.junior.entity.Investor;
import com.enviro.assessment.junior.enums.ProductType;
import com.enviro.assessment.junior.exception.WithdrawalValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class WithdrawalValidatorTest {

    private WithdrawalValidator validator;
    private Investor eligibleInvestor;
    private Investor youngInvestor;
    private InvestmentProduct savingsProduct;
    private InvestmentProduct retirementProduct;

    @BeforeEach
    void setUp() {
        validator = new WithdrawalValidator();

        // Investor over 65 — eligible for retirement withdrawal
        eligibleInvestor = Investor.builder()
                .firstName("Thabo")
                .lastName("Nkosi")
                .dateOfBirth(LocalDate.of(1955, 1, 1))
                .email("thabo@enviro365.co.za")
                .build();

        // Investor under 65 — not eligible for retirement withdrawal
        youngInvestor = Investor.builder()
                .firstName("Sipho")
                .lastName("Dlamini")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .email("sipho@enviro365.co.za")
                .build();

        savingsProduct = InvestmentProduct.builder()
                .productName("Growth Equity Fund")
                .productType(ProductType.SAVINGS)
                .balance(new BigDecimal("50000.00"))
                .build();

        retirementProduct = InvestmentProduct.builder()
                .productName("Enviro Retirement Annuity")
                .productType(ProductType.RETIREMENT)
                .balance(new BigDecimal("250000.00"))
                .build();
    }

    @Test
    @DisplayName("Valid savings withdrawal should pass all rules")
    void shouldPassForValidSavingsWithdrawal() {
        assertDoesNotThrow(() ->
            validator.validate(youngInvestor, savingsProduct, new BigDecimal("10000.00"))
        );
    }

    @Test
    @DisplayName("Valid retirement withdrawal for eligible investor should pass")
    void shouldPassForEligibleRetirementWithdrawal() {
        assertDoesNotThrow(() ->
            validator.validate(eligibleInvestor, retirementProduct, new BigDecimal("10000.00"))
        );
    }

    @Test
    @DisplayName("Retirement withdrawal for investor under 65 should fail")
    void shouldFailRetirementWithdrawalForYoungInvestor() {
        WithdrawalValidationException ex = assertThrows(
            WithdrawalValidationException.class,
            () -> validator.validate(youngInvestor, retirementProduct, new BigDecimal("10000.00"))
        );
        assertTrue(ex.getMessage().contains("older than 65"));
    }

    @Test
    @DisplayName("Amount exceeding 90% of balance should fail")
    void shouldFailWhenAmountExceedsNinetyPercent() {
        // 90% of 50000 = 45000, so 46000 should fail
        WithdrawalValidationException ex = assertThrows(
            WithdrawalValidationException.class,
            () -> validator.validate(eligibleInvestor, savingsProduct, new BigDecimal("46000.00"))
        );
        assertTrue(ex.getMessage().contains("90%"));
    }

    @Test
    @DisplayName("Amount equal to exactly 90% of balance should pass")
    void shouldPassWhenAmountEqualsNinetyPercent() {
        // Exactly 90% of 50000 = 45000
        assertDoesNotThrow(() ->
            validator.validate(eligibleInvestor, savingsProduct, new BigDecimal("45000.00"))
        );
    }

    @Test
    @DisplayName("Amount exceeding full balance should fail")
    void shouldFailWhenAmountExceedsBalance() {
        WithdrawalValidationException ex = assertThrows(
            WithdrawalValidationException.class,
            () -> validator.validate(eligibleInvestor, savingsProduct, new BigDecimal("60000.00"))
        );
        assertTrue(ex.getMessage().contains("available balance"));
    }
}