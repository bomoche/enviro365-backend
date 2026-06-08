package com.enviro.assessment.junior.service;

import com.enviro.assessment.junior.dto.request.WithdrawalRequestDto;
import com.enviro.assessment.junior.dto.response.WithdrawalResponseDto;
import com.enviro.assessment.junior.entity.InvestmentProduct;
import com.enviro.assessment.junior.entity.Investor;
import com.enviro.assessment.junior.entity.WithdrawalNotice;
import com.enviro.assessment.junior.enums.ProductType;
import com.enviro.assessment.junior.exception.InvestorNotFoundException;
import com.enviro.assessment.junior.exception.ProductNotFoundException;
import com.enviro.assessment.junior.exception.WithdrawalValidationException;
import com.enviro.assessment.junior.repository.InvestmentProductRepository;
import com.enviro.assessment.junior.repository.InvestorRepository;
import com.enviro.assessment.junior.repository.WithdrawalNoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WithdrawalService {

    private static final BigDecimal MAX_WITHDRAWAL_PERCENT = new BigDecimal("0.90");
    private static final int RETIREMENT_MINIMUM_AGE = 65;

    private final InvestorRepository investorRepository;
    private final InvestmentProductRepository productRepository;
    private final WithdrawalNoticeRepository withdrawalRepository;

    /**
     * Creates a withdrawal notice after applying all business rules.
     * @Transactional ensures balance update and notice creation
     * succeed or fail together — no partial updates.
     */
    @Transactional
    public WithdrawalResponseDto createWithdrawal(WithdrawalRequestDto dto) {

        // Step 1: Verify investor exists
        Investor investor = investorRepository.findById(dto.getInvestorId())
                .orElseThrow(() -> new InvestorNotFoundException(dto.getInvestorId()));

        // Step 2: Verify product exists AND belongs to this investor
        InvestmentProduct product = productRepository
                .findByIdAndInvestorId(dto.getProductId(), dto.getInvestorId())
                .orElseThrow(() -> new ProductNotFoundException(dto.getProductId()));

        // Step 3: Calculate investor age for retirement rule
        int age = Period.between(investor.getDateOfBirth(), LocalDate.now()).getYears();

        // Business Rule 1: Retirement products — investor must be older than 65
        if (ProductType.RETIREMENT.equals(product.getProductType()) && age <= RETIREMENT_MINIMUM_AGE) {
            throw new WithdrawalValidationException(
                "Retirement fund withdrawals are only permitted for investors older than "
                + RETIREMENT_MINIMUM_AGE + " years. Your age: " + age);
        }

        BigDecimal currentBalance = product.getBalance();
        BigDecimal maxAllowed = currentBalance
                .multiply(MAX_WITHDRAWAL_PERCENT)
                .setScale(2, RoundingMode.HALF_DOWN);

        // Business Rule 2: Amount must not exceed balance
        if (dto.getAmount().compareTo(currentBalance) > 0) {
            throw new WithdrawalValidationException(
                "Withdrawal amount exceeds the available balance of R" + currentBalance);
        }

        // Business Rule 3: Amount must not exceed 90% of balance
        if (dto.getAmount().compareTo(maxAllowed) > 0) {
            throw new WithdrawalValidationException(
                "Withdrawal amount exceeds the maximum allowable amount of R"
                + maxAllowed + " (90% of balance)");
        }

        // Step 4: Deduct balance and save updated product
        BigDecimal newBalance = currentBalance
                .subtract(dto.getAmount())
                .setScale(2, RoundingMode.HALF_DOWN);
        product.setBalance(newBalance);
        productRepository.save(product);

        // Step 5: Save withdrawal notice as an immutable audit record
        WithdrawalNotice notice = WithdrawalNotice.builder()
                .amount(dto.getAmount())
                .balanceAfterWithdrawal(newBalance)
                .product(product)
                .build();
        WithdrawalNotice saved = withdrawalRepository.save(notice);

        return WithdrawalResponseDto.builder()
                .id(saved.getId())
                .investorId(investor.getId())
                .productId(product.getId())
                .productName(product.getProductName())
                .productType(product.getProductType())
                .amount(saved.getAmount())
                .balanceAfterWithdrawal(saved.getBalanceAfterWithdrawal())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    public List<WithdrawalResponseDto> getWithdrawalHistory(Long investorId) {

        // Verify investor exists before fetching history
        if (!investorRepository.existsById(investorId)) {
            throw new InvestorNotFoundException(investorId);
        }

        return withdrawalRepository.findByInvestorId(investorId)
                .stream()
                .map(w -> WithdrawalResponseDto.builder()
                        .id(w.getId())
                        .investorId(investorId)
                        .productId(w.getProduct().getId())
                        .productName(w.getProduct().getProductName())
                        .productType(w.getProduct().getProductType())
                        .amount(w.getAmount())
                        .balanceAfterWithdrawal(w.getBalanceAfterWithdrawal())
                        .createdAt(w.getCreatedAt())
                        .build())
                .toList();
    }

    public String exportToCsv(Long investorId) {

        if (!investorRepository.existsById(investorId)) {
            throw new InvestorNotFoundException(investorId);
        }

        List<WithdrawalNotice> notices = withdrawalRepository
                .findByInvestorIdForExport(investorId);

        StringBuilder csv = new StringBuilder();
        csv.append("Date,Product Name,Product Type,Amount (R),Balance After (R)\n");

        for (WithdrawalNotice w : notices) {
            csv.append(w.getCreatedAt()).append(",")
               .append(w.getProduct().getProductName()).append(",")
               .append(w.getProduct().getProductType()).append(",")
               .append(w.getAmount()).append(",")
               .append(w.getBalanceAfterWithdrawal()).append("\n");
        }

        return csv.toString();
    }
}