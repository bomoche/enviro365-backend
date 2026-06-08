package com.enviro.assessment.junior.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "withdrawal_notice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawalNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    // Snapshot of balance at the moment of withdrawal
    // Once saved this never changes — it is an audit record
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfterWithdrawal;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Many notices belong to one product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private InvestmentProduct product;

    // Automatically sets the timestamp before saving to DB
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}