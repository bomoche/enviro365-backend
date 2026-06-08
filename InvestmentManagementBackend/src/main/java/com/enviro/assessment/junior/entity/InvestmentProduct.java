package com.enviro.assessment.junior.entity;

import com.enviro.assessment.junior.enums.ProductType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "investment_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productName;

    // Stores enum as string in DB — readable and safe
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType productType;

    // BigDecimal for money — never use double or float for financial values
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    // Many products belong to one investor
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investor_id", nullable = false)
    private Investor investor;

    // One product has many withdrawal notices
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WithdrawalNotice> withdrawalNotices = new ArrayList<>();
}