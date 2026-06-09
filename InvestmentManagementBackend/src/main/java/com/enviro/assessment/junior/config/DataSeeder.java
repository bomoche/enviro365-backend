package com.enviro.assessment.junior.config;

import com.enviro.assessment.junior.entity.InvestmentProduct;
import com.enviro.assessment.junior.entity.Investor;
import com.enviro.assessment.junior.enums.ProductType;
import com.enviro.assessment.junior.repository.InvestmentProductRepository;
import com.enviro.assessment.junior.repository.InvestorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Seeds the database with demo data on startup.
 * Checks if data already exists to avoid duplicates on restart
 * since we are using a file-based persistent H2 database.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final InvestorRepository investorRepository;
    private final InvestmentProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // Only seed if no data exists — prevents duplicates on restart
        if (investorRepository.count() > 0) {
            System.out.println("Data already exists — skipping seeder.");
            return;
        }

        // Create investor — born 1955, age 70, eligible for retirement withdrawal
        Investor investor = Investor.builder()
                .firstName("Thabo")
                .lastName("Nkosi")
                .email("thabo.nkosi@enviro365.co.za")
                .password(passwordEncoder.encode("password123"))
                .dateOfBirth(LocalDate.of(1955, 3, 15))
                .build();

        investorRepository.save(investor);

        // Retirement product — eligible because investor is over 65
        InvestmentProduct retirement = InvestmentProduct.builder()
                .productName("Enviro Retirement Annuity")
                .productType(ProductType.RETIREMENT)
                .balance(new BigDecimal("250000.00"))
                .investor(investor)
                .build();

        // Savings product 1
        InvestmentProduct savings1 = InvestmentProduct.builder()
                .productName("Growth Equity Fund")
                .productType(ProductType.SAVINGS)
                .balance(new BigDecimal("50000.00"))
                .investor(investor)
                .build();

        // Savings product 2
        InvestmentProduct savings2 = InvestmentProduct.builder()
                .productName("Stable Income Bond")
                .productType(ProductType.SAVINGS)
                .balance(new BigDecimal("12350.00"))
                .investor(investor)
                .build();

        productRepository.save(retirement);
        productRepository.save(savings1);
        productRepository.save(savings2);

        System.out.println("Demo data seeded successfully.");
        System.out.println("Investor ID: " + investor.getId());
        System.out.println("Use investor ID " + investor.getId() + " for all API calls.");
    }
}