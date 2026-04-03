package com.akshadip.atomicx.mappers;

import com.akshadip.atomicx.dto.TransactionRequestDto;
import com.akshadip.atomicx.dto.TransactionResponseDto;
import com.akshadip.atomicx.exceptions.UserDoesnotExist;
import com.akshadip.atomicx.models.Account;
import com.akshadip.atomicx.models.Transaction;
import com.akshadip.atomicx.models.TransactionStatus;
import com.akshadip.atomicx.repositories.AccountRepository;
import com.akshadip.atomicx.repositories.TransactionRepository;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class TransactionMapper {
    @Value("${app.system.id}")
    private String systemId;
    private final AccountRepository accountRepository;
    private final TimeBasedEpochGenerator idGen;

    TransactionMapper(AccountRepository accountRepository, TimeBasedEpochGenerator idGen) {
        this.accountRepository = accountRepository;
        this.idGen = idGen;
    }


    public Transaction toEntity(TransactionRequestDto transactionRequestDto) {
        Optional<UUID> senderId = accountRepository.getAccountId(transactionRequestDto.getSenderUserName());
        UUID sender = senderId.orElseThrow(() -> new UserDoesnotExist("No sender exist with this UserName"));
        Optional<UUID> receiverId = accountRepository.getAccountId(transactionRequestDto.getReceiverUserName());
        UUID receiver = receiverId.orElseThrow(() -> new UserDoesnotExist("No reciever Exist with this userName"));
        return new Transaction()
                .setSender(sender)
                .setReceiver(receiver)
                .setTransactionId(idGen.generate())
                .setAmount(transactionRequestDto.getAmount())
                .setStatus(TransactionStatus.COMPLETED);
    }

    public TransactionResponseDto toResponse(Transaction transaction) {
        UUID systemUuid = UUID.fromString(systemId);
        String senderUserName;
        if (transaction.getSender().equals(systemUuid)) {
            senderUserName = "SYSTEM_WELCOME_BONUS";
        } else {
            senderUserName = accountRepository.getUserName(transaction.getSender())
                    .orElseThrow(() -> new UserDoesnotExist("No sender exist with this UserName"));
        }
        String receiverUserName = accountRepository.getUserName(transaction.getReceiver())
                .orElseThrow(() -> new UserDoesnotExist("No receiver with this UserName"));
        return new TransactionResponseDto()
                .setSenderUserName(senderUserName)
                .setReceiverUserName(receiverUserName)
                .setStatus(transaction.getStatus())
                .setAmount(transaction.getAmount());
    }
}

