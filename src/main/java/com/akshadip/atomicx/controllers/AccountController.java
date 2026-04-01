package com.akshadip.atomicx.controllers;

import com.akshadip.atomicx.dto.AccountRequestDto;
import com.akshadip.atomicx.dto.AccountResponseDto;
import com.akshadip.atomicx.models.Account;
import com.akshadip.atomicx.services.AccountService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;

    AccountController(AccountService accountService){
        this.accountService = accountService;
    }

    @PostMapping("/create")
    AccountResponseDto createAccount(@RequestBody AccountRequestDto accountRequestDto){
        return accountService.createAccount(accountRequestDto);
    }
    @GetMapping("/{userName}")
    Account getAccount(@PathVariable String userName){
        return accountService.getAccount(userName);
    }
}
