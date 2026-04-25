package com.akshadip.atomicx.services;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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

    /**
 * Transfers funds between two accounts with idempotency support.
 * This method performs a financial transaction between a sender and receiver account.
 * It includes idempotency checks to prevent duplicate transactions, validates account
 * existence, and ensures sufficient funds before executing the transfer.
 *
 * @param transactionRequestDto the transaction details including sender, receiver, and amount
 * @param idempotencyKey optional unique key to ensure transaction idempotency
 * @return TransactionResponseDto containing the completed transaction details
 * @throws UserDoesnotExist if sender or receiver account does not exist
 * @throws InsufficientFundsException if sender has insufficient funds
 */
@Transactional(timeout = 10)
public TransactionResponseDto transfer(TransactionRequestDto transactionRequestDto, String idempotencyKey){
    // Idempotency check - return existing transaction if duplicate key provided
    if (idempotencyKey != null && !idempotencyKey.isBlank()) {
        Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return transactionMapper.toResponse(existing.get());
        }
    }

    // Convert request DTO to transaction entity
    Transaction transaction = transactionMapper.toEntity(transactionRequestDto);

    // Set idempotency key if provided
    if (idempotencyKey != null && !idempotencyKey.isBlank()) {
        transaction.setIdempotencyKey(idempotencyKey);
    }

    // Acquire pessimistic locks on both accounts in consistent order to prevent deadlock
    UUID senderId = transaction.getSender();
    UUID receiverId = transaction.getReceiver();
    UUID firstId = senderId.compareTo(receiverId) < 0 ? senderId : receiverId;
    UUID secondId = senderId.compareTo(receiverId) < 0 ? receiverId : senderId;
    Account first = accountRepository.findByAccountIdWithLock(firstId)
            .orElseThrow(()->new UserDoesnotExist("User with id does not exits"));
    Account second = accountRepository.findByAccountIdWithLock(secondId)
            .orElseThrow(()-> new UserDoesnotExist("User with id doesn't exist"));

    // Validate sender has sufficient funds before proceeding
    if(ledgerEntryRepository.getBalance(senderId).compareTo(transaction.getAmount()) < 0){
        throw new InsufficientFundsException("Sender Doesn't have enough funds");
    }

    // Mark transaction as completed and persist
    transaction.setStatus(TransactionStatus.COMPLETED);
    transactionRepository.save(transaction);

    // Execute ledger entries for the transaction
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

    /**
 * Retrieves all transactions for a given user within a specified date range.
 * @param start the start date of the range (inclusive)
 * @param end the end date of the range (inclusive)
 * @param userName the username of the account holder
 * @return a list of TransactionResponseDto representing the transactions in the date range
 * @throws UserDoesnotExist if the user does not exist
 */
public List<TransactionResponseDto> getAllTransactionBetween(LocalDate start, LocalDate end, String userName){
    Instant startTime = start.atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
    Instant endTime = end.atTime(23, 59, 59).toInstant(java.time.ZoneOffset.UTC);
    UUID accountId = accountRepository.getAccountId(userName)
            .orElseThrow(()-> new UserDoesnotExist("User doesn't exist"));
    Optional<List<Transaction>> transactions = transactionRepository.findAllByUserNameAndDateRange(accountId, startTime, endTime);
    return transactions.map(
            list -> list.stream()
                    .map(transactionMapper::toResponse)
                    .toList())
            .orElseGet(Collections::emptyList);

}

}
