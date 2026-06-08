package com.enviro.assessment.junior.repository;

import com.enviro.assessment.junior.entity.WithdrawalNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WithdrawalNoticeRepository extends JpaRepository<WithdrawalNotice, Long> {

    // Fetches all withdrawals for all products belonging to a given investor
    @Query("SELECT w FROM WithdrawalNotice w WHERE w.product.investor.id = :investorId ORDER BY w.createdAt DESC")
    List<WithdrawalNotice> findByInvestorId(@Param("investorId") Long investorId);

    // Used for CSV export
    @Query("SELECT w FROM WithdrawalNotice w WHERE w.product.investor.id = :investorId ORDER BY w.createdAt DESC")
    List<WithdrawalNotice> findByInvestorIdForExport(@Param("investorId") Long investorId);
}