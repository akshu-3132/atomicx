package com.akshadip.atomicx.services;

import com.akshadip.atomicx.dto.TransactionRequestDto;
import com.akshadip.atomicx.dto.TransactionResponseDto;
import com.akshadip.atomicx.exceptions.InsufficientFundsException;
import com.akshadip.atomicx.exceptions.UserDoesnotExist;
import com.akshadip.atomicx.mappers.TransactionMapper;
import com.akshadip.atomicx.models.Transaction;
import com.akshadip.atomicx.models.TransactionStatus;
import com.akshadip.atomicx.repositories.AccountRepository;
import com.akshadip.atomicx.repositories.TransactionRepository;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import jakarta.transaction.Transactional;
import org.hibernate.validator.cfg.defs.UUIDDef;
import org.springframework.data.jpa.repository.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.sql.Time;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {
    @Value("${app.system.id}")
    private String systemId;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final AccountRepository accountRepository;
    private final TimeBasedEpochGenerator idGen;
    private final LedgerService ledgerService;
    TransactionService(TransactionRepository transactionRepository,
                       TransactionMapper transactionMapper,
                       AccountRepository accountRepository,
                       TimeBasedEpochGenerator idGen,
                       LedgerService ledgerService){
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.accountRepository = accountRepository;
        this.idGen = idGen;
        this.ledgerService = ledgerService;
    }

    @Transactional
    public TransactionResponseDto transfer(TransactionRequestDto transactionRequestDto){
        Optional<UUID> senderUserName = accountRepository.getAccountId(transactionRequestDto.getSenderUserName());
        UUID senderId = senderUserName.orElseThrow(() -> new UserDoesnotExist("User Doesn't exist"));
        BigDecimal senderBalance = transactionRepository.getBalance(senderId);
        if(senderBalance.compareTo(transactionRequestDto.getAmount()) < 0){
            throw new InsufficientFundsException("Insufficient funds for sender");
        }
        Transaction transaction = transactionMapper.toEntity(transactionRequestDto);
        return ledgerService.executeTransaction(transaction);

    }
    @Transactional
    public void internalTransfer(UUID accountId){
        UUID systemUuid = UUID.fromString(systemId);
        Transaction transaction = new Transaction()
                .setSender(systemUuid)
                .setReceiver(accountId)
                .setStatus(TransactionStatus.COMPLETED)
                .setAmount(BigDecimal.valueOf(100))
                .setTransactionId(idGen.generate());
        ledgerService.executeTransaction(transaction);
    }
    public BigDecimal balance(String userName){
        Optional<UUID> userId = accountRepository.getAccountId(userName);
        UUID user = userId.orElseThrow(()-> new UserDoesnotExist("User doesn't exist"));
        return transactionRepository.getBalance(user);
    }
}
