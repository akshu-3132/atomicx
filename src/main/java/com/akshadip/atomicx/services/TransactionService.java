package com.akshadip.atomicx.services;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.akshadip.atomicx.dto.TransactionRequestDto;
import com.akshadip.atomicx.dto.TransactionResponseDto;
import com.akshadip.atomicx.exceptions.InsufficientFundsException;
import com.akshadip.atomicx.exceptions.UserDoesnotExist;
import com.akshadip.atomicx.mappers.TransactionMapper;
import com.akshadip.atomicx.models.Account;
import com.akshadip.atomicx.models.Transaction;
import com.akshadip.atomicx.models.TransactionStatus;
import com.akshadip.atomicx.repositories.AccountRepository;
import com.akshadip.atomicx.repositories.LedgerEntryRepository;
import com.akshadip.atomicx.repositories.TransactionRepository;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;

@Service
public class TransactionService {

    private final UUID systemUuid;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final AccountRepository accountRepository;
    private final TimeBasedEpochGenerator idGen;
    private final LedgerService ledgerService;
    private final LedgerEntryRepository ledgerEntryRepository;
    TransactionService( @Value("${app.system.id}") String systemId,
            TransactionRepository transactionRepository,
                       TransactionMapper transactionMapper,
                       AccountRepository accountRepository,
                       TimeBasedEpochGenerator idGen,
                       LedgerService ledgerService,
                       LedgerEntryRepository ledgerEntryRepository){
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.accountRepository = accountRepository;
        this.idGen = idGen;
        this.ledgerService = ledgerService;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.systemUuid = UUID.fromString(systemId);
    }

    @Transactional(timeout = 10)
    public TransactionResponseDto transfer(TransactionRequestDto transactionRequestDto, String idempotencyKey){
        // Idempotency check - return existing transaction if duplicate key provided
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return transactionMapper.toResponse(existing.get());
            }
        }

        Transaction transaction = transactionMapper.toEntity(transactionRequestDto);
        
        // Set idempotency key if provided
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            transaction.setIdempotencyKey(idempotencyKey);
        }
        
        UUID senderId = transaction.getSender();
        UUID receiverId = transaction.getReceiver();
        UUID firstId = senderId.compareTo(receiverId) < 0 ? senderId : receiverId;
        UUID secondId = senderId.compareTo(receiverId) < 0 ? receiverId : senderId;
        Account first = accountRepository.findByAccountIdWithLock(firstId)
                .orElseThrow(()->new UserDoesnotExist("User with id does not exits"));
        Account second = accountRepository.findByAccountIdWithLock(secondId)
                .orElseThrow(()-> new UserDoesnotExist("User with id doesn't exist"));

        if(ledgerEntryRepository.getBalance(senderId).compareTo(transaction.getAmount()) < 0){
            throw new InsufficientFundsException("Sender Doesn't have enough funds");
        }
        transaction.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);
       return ledgerService.executeTransaction(transaction);
    }
    @Transactional(timeout = 10)
    public void internalTransfer(UUID accountId){
        Account first = accountRepository.findByAccountIdWithLock(accountId)
                .orElseThrow(()-> new UserDoesnotExist("UserNotFOund"));
        Transaction transaction = new Transaction()
                .setSender(this.systemUuid)
                .setReceiver(accountId)
                .setStatus(TransactionStatus.COMPLETED)
                .setAmount(BigDecimal.valueOf(100))
                .setTransactionId(idGen.generate());
        transactionRepository.save(transaction);
        ledgerService.executeTransaction(transaction);
    }
    public BigDecimal balance(String userName){
        Optional<UUID> userId = accountRepository.getAccountId(userName);
        UUID user = userId.orElseThrow(()-> new UserDoesnotExist("User doesn't exist"));
        return ledgerEntryRepository.getBalance(user);
    }
}
