package com.enviro.assessment.junior.service;

import com.enviro.assessment.junior.dto.request.WithdrawalRequestDto;
import com.enviro.assessment.junior.dto.response.WithdrawalResponseDto;
import com.enviro.assessment.junior.entity.InvestmentProduct;
import com.enviro.assessment.junior.entity.Investor;
import com.enviro.assessment.junior.entity.WithdrawalNotice;
import com.enviro.assessment.junior.exception.InvestorNotFoundException;
import com.enviro.assessment.junior.exception.ProductNotFoundException;
import com.enviro.assessment.junior.repository.InvestmentProductRepository;
import com.enviro.assessment.junior.repository.InvestorRepository;
import com.enviro.assessment.junior.repository.WithdrawalNoticeRepository;
import com.enviro.assessment.junior.validation.WithdrawalValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WithdrawalService {

    private final InvestorRepository investorRepository;
    private final InvestmentProductRepository productRepository;
    private final WithdrawalNoticeRepository withdrawalRepository;
    private final WithdrawalValidator withdrawalValidator;

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

        // Step 2: Verify product exists and belongs to this investor
        InvestmentProduct product = productRepository
                .findByIdAndInvestorId(dto.getProductId(), dto.getInvestorId())
                .orElseThrow(() -> new ProductNotFoundException(dto.getProductId()));

        // Step 3: Apply all business rules — throws if any rule fails
        withdrawalValidator.validate(investor, product, dto.getAmount());

        // Step 4: Deduct balance and save updated product
        BigDecimal newBalance = product.getBalance()
                .subtract(dto.getAmount())
                .setScale(2, RoundingMode.HALF_DOWN);
        product.setBalance(newBalance);
        productRepository.save(product);

        // Step 5: Save withdrawal notice as immutable audit record
        WithdrawalNotice notice = WithdrawalNotice.builder()
                .amount(dto.getAmount())
                .balanceAfterWithdrawal(newBalance)
                .product(product)
                .build();
        WithdrawalNotice saved = withdrawalRepository.save(notice);

        // Step 6: Return response DTO — entity never leaves service layer
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

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
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