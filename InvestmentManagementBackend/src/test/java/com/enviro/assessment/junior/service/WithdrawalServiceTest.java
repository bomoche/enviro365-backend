package com.enviro.assessment.junior.service;

import com.enviro.assessment.junior.dto.request.WithdrawalRequestDto;
import com.enviro.assessment.junior.dto.response.WithdrawalResponseDto;
import com.enviro.assessment.junior.entity.InvestmentProduct;
import com.enviro.assessment.junior.entity.Investor;
import com.enviro.assessment.junior.entity.WithdrawalNotice;
import com.enviro.assessment.junior.enums.ProductType;
import com.enviro.assessment.junior.exception.InvestorNotFoundException;
import com.enviro.assessment.junior.exception.ProductNotFoundException;
import com.enviro.assessment.junior.repository.InvestmentProductRepository;
import com.enviro.assessment.junior.repository.InvestorRepository;
import com.enviro.assessment.junior.repository.WithdrawalNoticeRepository;
import com.enviro.assessment.junior.validation.WithdrawalValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WithdrawalServiceTest {

    @Mock
    private InvestorRepository investorRepository;

    @Mock
    private InvestmentProductRepository productRepository;

    @Mock
    private WithdrawalNoticeRepository withdrawalRepository;

    @Mock
    private WithdrawalValidator withdrawalValidator;

    @InjectMocks
    private WithdrawalService withdrawalService;

    private Investor investor;
    private InvestmentProduct product;
    private WithdrawalRequestDto requestDto;

    @BeforeEach
    void setUp() {
        investor = Investor.builder()
                .id(1L)
                .firstName("Thabo")
                .lastName("Nkosi")
                .email("thabo@enviro365.co.za")
                .dateOfBirth(LocalDate.of(1955, 1, 1))
                .build();

        product = InvestmentProduct.builder()
                .id(1L)
                .productName("Growth Equity Fund")
                .productType(ProductType.SAVINGS)
                .balance(new BigDecimal("50000.00"))
                .investor(investor)
                .build();

        requestDto = new WithdrawalRequestDto();
        requestDto.setInvestorId(1L);
        requestDto.setProductId(1L);
        requestDto.setAmount(new BigDecimal("10000.00"));
    }

    @Test
    @DisplayName("Valid withdrawal should return response DTO with updated balance")
    void shouldCreateWithdrawalSuccessfully() {

        WithdrawalNotice savedNotice = WithdrawalNotice.builder()
                .id(1L)
                .amount(new BigDecimal("10000.00"))
                .balanceAfterWithdrawal(new BigDecimal("40000.00"))
                .product(product)
                .build();

        // Use reflection to set createdAt since @PrePersist doesn't fire in unit tests
        savedNotice = WithdrawalNotice.builder()
                .id(1L)
                .amount(new BigDecimal("10000.00"))
                .balanceAfterWithdrawal(new BigDecimal("40000.00"))
                .product(product)
                .build();

        when(investorRepository.findById(1L)).thenReturn(Optional.of(investor));
        when(productRepository.findByIdAndInvestorId(1L, 1L)).thenReturn(Optional.of(product));
        doNothing().when(withdrawalValidator).validate(any(), any(), any());
        when(productRepository.save(any())).thenReturn(product);
        when(withdrawalRepository.save(any())).thenReturn(savedNotice);

        WithdrawalResponseDto response = withdrawalService.createWithdrawal(requestDto);

        assertNotNull(response);
        assertEquals(new BigDecimal("10000.00"), response.getAmount());
        assertEquals(new BigDecimal("40000.00"), response.getBalanceAfterWithdrawal());

        // Verify all steps were called
        verify(investorRepository).findById(1L);
        verify(productRepository).findByIdAndInvestorId(1L, 1L);
        verify(withdrawalValidator).validate(any(), any(), any());
        verify(productRepository).save(any());
        verify(withdrawalRepository).save(any());
    }

    @Test
    @DisplayName("Non-existent investor should throw InvestorNotFoundException")
    void shouldThrowWhenInvestorNotFound() {
        when(investorRepository.findById(99L)).thenReturn(Optional.empty());

        requestDto.setInvestorId(99L);

        assertThrows(
            InvestorNotFoundException.class,
            () -> withdrawalService.createWithdrawal(requestDto)
        );

        // Verify service stopped — product was never fetched
        verify(productRepository, never()).findByIdAndInvestorId(any(), any());
    }

    @Test
    @DisplayName("Non-existent product should throw ProductNotFoundException")
    void shouldThrowWhenProductNotFound() {
        when(investorRepository.findById(1L)).thenReturn(Optional.of(investor));
        when(productRepository.findByIdAndInvestorId(99L, 1L)).thenReturn(Optional.empty());

        requestDto.setProductId(99L);

        assertThrows(
            ProductNotFoundException.class,
            () -> withdrawalService.createWithdrawal(requestDto)
        );

        // Verify validator was never reached
        verify(withdrawalValidator, never()).validate(any(), any(), any());
    }

    @Test
    @DisplayName("getWithdrawalHistory should throw when investor not found")
    void shouldThrowWhenFetchingHistoryForNonExistentInvestor() {
        when(investorRepository.existsById(99L)).thenReturn(false);

        assertThrows(
            InvestorNotFoundException.class,
            () -> withdrawalService.getWithdrawalHistory(99L)
        );
    }

    @Test
    @DisplayName("getWithdrawalHistory should return list of withdrawal DTOs")
    void shouldReturnWithdrawalHistory() {
        WithdrawalNotice notice = WithdrawalNotice.builder()
                .id(1L)
                .amount(new BigDecimal("5000.00"))
                .balanceAfterWithdrawal(new BigDecimal("45000.00"))
                .product(product)
                .build();

        when(investorRepository.existsById(1L)).thenReturn(true);
        when(withdrawalRepository.findByInvestorId(1L)).thenReturn(List.of(notice));

        List<WithdrawalResponseDto> result = withdrawalService.getWithdrawalHistory(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(new BigDecimal("5000.00"), result.get(0).getAmount());
    }

    @Test
    @DisplayName("exportToCsv should throw when investor not found")
    void shouldThrowWhenExportingCsvForNonExistentInvestor() {
        when(investorRepository.existsById(99L)).thenReturn(false);

        assertThrows(
            InvestorNotFoundException.class,
            () -> withdrawalService.exportToCsv(99L)
        );
    }

    @Test
    @DisplayName("exportToCsv should return CSV string with headers")
    void shouldReturnCsvWithHeaders() {
        WithdrawalNotice notice = WithdrawalNotice.builder()
                .id(1L)
                .amount(new BigDecimal("5000.00"))
                .balanceAfterWithdrawal(new BigDecimal("45000.00"))
                .product(product)
                .build();

        when(investorRepository.existsById(1L)).thenReturn(true);
        when(withdrawalRepository.findByInvestorIdForExport(1L)).thenReturn(List.of(notice));

        String csv = withdrawalService.exportToCsv(1L);

        assertNotNull(csv);
        assertTrue(csv.startsWith("Date,Product Name,Product Type,Amount (R),Balance After (R)"));
        assertTrue(csv.contains("Growth Equity Fund"));
        assertTrue(csv.contains("5000.00"));
    }
}