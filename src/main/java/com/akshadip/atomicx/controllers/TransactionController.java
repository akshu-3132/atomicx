package com.akshadip.atomicx.controllers;

import com.akshadip.atomicx.dto.TransactionRequestDto;
import com.akshadip.atomicx.dto.TransactionResponseDto;
import com.akshadip.atomicx.repositories.TransactionRepository;
import com.akshadip.atomicx.services.TransactionService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    TransactionService transactionService;
    TransactionController(TransactionService transactionService){
        this.transactionService = transactionService;
    }
    @PostMapping("/transfer")
    TransactionResponseDto transfer(@RequestBody TransactionRequestDto transactionRequestDto){
        return transactionService.transfer(transactionRequestDto);
    }
    @GetMapping("/balance/{userName}")
    BigDecimal balance(@PathVariable String userName){
        return transactionService.balance(userName);
    }
}
