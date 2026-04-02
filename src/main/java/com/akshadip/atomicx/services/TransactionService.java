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
    TransactionService(TransactionRepository transactionRepository,
                       TransactionMapper transactionMapper,
                       AccountRepository accountRepository, TimeBasedEpochGenerator idGen){
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.accountRepository = accountRepository;
        this.idGen = idGen;
    }

    public Transaction getObject(TransactionRequestDto transactionRequestDto,boolean flag){
        Transaction transaction = transactionMapper.toEntity(transactionRequestDto);
        if(flag){
            transaction.setCreditAmount(transactionRequestDto.getAmount());
            transaction.setDebitAmount(BigDecimal.ZERO);
        }
        else{
            transaction.setCreditAmount(BigDecimal.ZERO);
            transaction.setDebitAmount(transactionRequestDto.getAmount());
        }
        return transaction;
    }

    @Transactional
    public TransactionResponseDto transfer(TransactionRequestDto transactionRequestDto){
        Optional<UUID> senderUserName = accountRepository.getAccountId(transactionRequestDto.getSenderUserName());
        UUID sender = senderUserName.orElseThrow(()->new UserDoesnotExist("Sender Doesn't exist"));
        BigDecimal senderBalance = transactionRepository.getBalance(sender);
        if(senderBalance.compareTo(transactionRequestDto.getAmount()) < 0){
            throw new InsufficientFundsException("Insufficeint funds for sender");
        }
        Transaction senderDetails = getObject(transactionRequestDto,true);
        Transaction receiverDetails = getObject(transactionRequestDto,false);
        senderDetails.setStatus(TransactionStatus.COMPLETED);
        receiverDetails.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(senderDetails);
        transactionRepository.save(receiverDetails);
        return transactionMapper.toResponse(senderDetails);
    }
    @Transactional
    public void internalDeposit(UUID accountId){
        UUID systemUuid = UUID.fromString(systemId);
        Transaction transaction = new Transaction();
        transaction.setTransactionId(idGen.generate())
                .setSender(systemUuid)
                .setReceiver(accountId)
                .setCreditAmount(BigDecimal.valueOf(100))
                .setStatus(TransactionStatus.COMPLETED)
                .setCreatedAt(Instant.now())
                .setDebitAmount(BigDecimal.ZERO);
        transactionRepository.save(transaction);
    }
    public BigDecimal balance(String userName){
        Optional<UUID> userId = accountRepository.getAccountId(userName);
        UUID user = userId.orElseThrow(()-> new UserDoesnotExist("User doesn't exist"));
        return transactionRepository.getBalance(user);
    }
}
