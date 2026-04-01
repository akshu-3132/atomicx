package com.akshadip.atomicx.services;

import com.akshadip.atomicx.config.IdGeneratorConfig;
import com.akshadip.atomicx.dto.AccountRequestDto;
import com.akshadip.atomicx.dto.AccountResponseDto;
import com.akshadip.atomicx.mappers.AccountMapper;
import com.akshadip.atomicx.models.Account;
import com.akshadip.atomicx.repositories.AccountRepository;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final TimeBasedEpochGenerator idGen;

    AccountService(AccountRepository accountRepository, AccountMapper accountMapper,
                              TimeBasedEpochGenerator idGen) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.idGen = idGen;
    }

    public AccountResponseDto createAccount(AccountRequestDto accountRequestDto) {
        Account account = accountMapper.toEntity(accountRequestDto);
        account.setAccountId( idGen.generate());
        accountRepository.save(account);
        return new AccountResponseDto()
                .setAccountId(account.getAccountId())
                .setUserName(account.getUserName())
                .setEmail(account.getEmail());
    }
    public Account getAccount(String userName){
        return accountRepository.findByUserName(userName).orElseThrow(()->new RuntimeException("User Not found"));
    }

}
