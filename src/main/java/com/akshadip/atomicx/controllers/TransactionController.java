package com.akshadip.atomicx.controllers;

import java.math.BigDecimal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
