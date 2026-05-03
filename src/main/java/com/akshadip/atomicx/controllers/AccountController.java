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

    AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Creates a new account based on the provided account details.
     *
     * @param accountRequestDto the request object containing account details such as first name, email, and username
     * @return the response object containing the generated account ID, username, and email of the newly created account
     */
    @PostMapping("/create")
    public AccountResponseDto createAccount(@RequestBody AccountRequestDto accountRequestDto) {
        return accountService.createAccount(accountRequestDto);
    }

    /**
     * Retrieves the account information for a given username.
     *
     * @param userName the username of the account to retrieve
     * @return the account associated with the specified username
     */
    @GetMapping("/{userName}")
    public AccountResponseDto getAccount(@PathVariable String userName) {
        return accountService.getAccount(userName);
    }
}
