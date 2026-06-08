package com.enviro.assessment.junior.validation;

import com.enviro.assessment.junior.entity.InvestmentProduct;
import com.enviro.assessment.junior.entity.Investor;
import com.enviro.assessment.junior.enums.ProductType;
import com.enviro.assessment.junior.exception.WithdrawalValidationException;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

/**
 * Centralises all withdrawal business rule validation.
 * Keeps WithdrawalService clean — service orchestrates,
 * validator enforces rules.
 */
@Component
public class WithdrawalValidator {

    private static final BigDecimal MAX_WITHDRAWAL_PERCENT = new BigDecimal("0.90");
    private static final int RETIREMENT_MINIMUM_AGE = 65;

    public void validate(Investor investor, InvestmentProduct product, BigDecimal amount) {
        validateRetirementAge(investor, product);
        validateBalance(product, amount);
        validateNinetyPercentRule(product, amount);
    }

    // Business Rule 1: Retirement products require investor to be over 65
    private void validateRetirementAge(Investor investor, InvestmentProduct product) {
        if (ProductType.RETIREMENT.equals(product.getProductType())) {
            int age = Period.between(investor.getDateOfBirth(), LocalDate.now()).getYears();
            if (age <= RETIREMENT_MINIMUM_AGE) {
                throw new WithdrawalValidationException(
                    "Retirement fund withdrawals are only permitted for investors older than "
                    + RETIREMENT_MINIMUM_AGE + " years. Your age: " + age);
            }
        }
    }

    // Business Rule 2: Amount must not exceed current balance
    private void validateBalance(InvestmentProduct product, BigDecimal amount) {
        if (amount.compareTo(product.getBalance()) > 0) {
            throw new WithdrawalValidationException(
                "Withdrawal amount exceeds the available balance of R"
                + product.getBalance());
        }
    }

    // Business Rule 3: Amount must not exceed 90% of balance
    private void validateNinetyPercentRule(InvestmentProduct product, BigDecimal amount) {
        BigDecimal maxAllowed = product.getBalance()
                .multiply(MAX_WITHDRAWAL_PERCENT)
                .setScale(2, RoundingMode.HALF_DOWN);

        if (amount.compareTo(maxAllowed) > 0) {
            throw new WithdrawalValidationException(
                "Withdrawal amount exceeds the maximum allowable amount of R"
                + maxAllowed + " (90% of balance)");
        }
    }
}