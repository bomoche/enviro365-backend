package com.enviro.assessment.junior.dto.response;

import com.enviro.assessment.junior.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalResponseDto {

    private Long id;
    private Long investorId;
    private Long productId;
    private String productName;
    private ProductType productType;
    private BigDecimal amount;
    private BigDecimal balanceAfterWithdrawal;
    private LocalDateTime createdAt;
}