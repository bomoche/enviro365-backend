package com.enviro.assessment.junior.controller;

import com.enviro.assessment.junior.dto.request.WithdrawalRequestDto;
import com.enviro.assessment.junior.dto.response.WithdrawalResponseDto;
import com.enviro.assessment.junior.service.WithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/withdrawals")
@RequiredArgsConstructor
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    // POST /api/withdrawals
    @PostMapping
    public ResponseEntity<WithdrawalResponseDto> createWithdrawal(
            @Valid @RequestBody WithdrawalRequestDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(withdrawalService.createWithdrawal(dto));
    }

    // GET /api/withdrawals?investorId=1
    @GetMapping
    public ResponseEntity<List<WithdrawalResponseDto>> getHistory(
            @RequestParam Long investorId) {
        return ResponseEntity.ok(withdrawalService.getWithdrawalHistory(investorId));
    }

    // GET /api/withdrawals/export?investorId=1
    @GetMapping("/export")
    public ResponseEntity<String> exportCsv(@RequestParam Long investorId) {
        String csv = withdrawalService.exportToCsv(investorId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "enviro365-statement.csv");

        return ResponseEntity.ok().headers(headers).body(csv);
    }
}