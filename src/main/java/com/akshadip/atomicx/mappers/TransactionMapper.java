package com.akshadip.atomicx.mappers;

import com.akshadip.atomicx.dto.TransactionRequestDto;
import com.akshadip.atomicx.dto.TransactionResponseDto;
import com.akshadip.atomicx.models.Account;
import com.akshadip.atomicx.models.Transaction;
import com.akshadip.atomicx.repositories.AccountRepository;
import com.akshadip.atomicx.repositories.TransactionRepository;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class TransactionMapper {

    private final AccountRepository accountRepository;
    private final TimeBasedEpochGenerator idGen;

    TransactionMapper(AccountRepository accountRepository, TimeBasedEpochGenerator idGen) {
        this.accountRepository = accountRepository;
        this.idGen = idGen;
    }


    public Transaction toEntity(TransactionRequestDto transactionRequestDto) {
        Optional<UUID> senderId = accountRepository.getAccountId(transactionRequestDto.getSenderUserName());
        UUID sender = senderId.orElseThrow(() -> new RuntimeException("No sender from this username"));
        Optional<UUID> receiverId = accountRepository.getAccountId(transactionRequestDto.getReceiverUserName());
        UUID receiver = receiverId.orElseThrow(() -> new RuntimeException("No receiver from this userName"));
        return new Transaction()
                .setSender(sender)
                .setReceiver(receiver)
                .setCreatedAt(Instant.now())
                .setTransactionId(idGen.generate());
    }

    public TransactionResponseDto toResponse(Transaction transaction) {
        Optional<String> sender = accountRepository.getUserName(transaction.getSender());
        Optional<String> receiver = accountRepository.getUserName(transaction.getReceiver());
        String senderUserName = sender.orElseThrow(() -> new RuntimeException("Sender UserName doesn't exist"));
        String receiverUserName = receiver.orElseThrow(() -> new RuntimeException("Receiver UserName doesn't exist"));

        return new TransactionResponseDto()
                .setSenderUserName(senderUserName)
                .setReceiverUserName(receiverUserName)
                .setStatus(transaction.getStatus())
                .setAmount(transaction.getCreditAmount());

    }
}

