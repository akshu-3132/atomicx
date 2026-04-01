package com.akshadip.atomicx.mappers;

import com.akshadip.atomicx.dto.AccountRequestDto;
import com.akshadip.atomicx.dto.AccountResponseDto;
import com.akshadip.atomicx.models.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {
    public AccountResponseDto toResponse(Account account){
        return new AccountResponseDto()
                .setAccountId(account.getAccountId())
                .setEmail(account.getEmail())
                .setUserName(account.getUserName());
    }

    public Account toEntity(AccountRequestDto accountRequestDto){
        return new Account()
                .setEmail(accountRequestDto.getEmail())
                .setUserName(accountRequestDto.getUserName())
                .setFirstName(accountRequestDto.getFirstName());
    }

}
