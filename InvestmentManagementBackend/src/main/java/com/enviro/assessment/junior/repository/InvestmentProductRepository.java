package com.enviro.assessment.junior.repository;

import com.enviro.assessment.junior.entity.InvestmentProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InvestmentProductRepository extends JpaRepository<InvestmentProduct, Long> {

    // Finds a product by its ID and verifies it belongs to the correct investor
    // Prevents an investor from withdrawing from another investor's product
    Optional<InvestmentProduct> findByIdAndInvestorId(Long productId, Long investorId);
}