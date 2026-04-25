package com.akshadip.atomicx.controllers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.akshadip.atomicx.dto.TransactionRequestDto;
import com.akshadip.atomicx.dto.TransactionResponseDto;
import com.akshadip.atomicx.services.TransactionService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    TransactionService transactionService;
    TransactionController(TransactionService transactionService){
        this.transactionService = transactionService;
    }
    @PostMapping("/transfer")
    TransactionResponseDto transfer(
            @RequestBody TransactionRequestDto transactionRequestDto,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ){
        return transactionService.transfer(transactionRequestDto, idempotencyKey);
    }
    @GetMapping("/balance/{userName}")
    BigDecimal balance(@PathVariable String userName){
        return transactionService.balance(userName);
    }

/**
 * Retrieves a list of transactions for a user within a specified date range.
 * @param userName the username of the user
 * @param start the start date of the range
 * @param end the end date of the range
 * @return a ResponseEntity containing the list of transactions
 */
    @GetMapping("/history/filter")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsByDateRange(
            @RequestParam("UserName") String userName,
            @RequestParam("start") LocalDate start,
            @RequestParam("end") LocalDate end) {
        List<TransactionResponseDto> transactions = transactionService.getAllTransactionBetween(start, end,userName);
        return ResponseEntity.ok(transactions);
    }


}
