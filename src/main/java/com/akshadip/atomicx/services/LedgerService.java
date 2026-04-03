package com.akshadip.atomicx.services;

import com.akshadip.atomicx.dto.AccountResponseDto;
import com.akshadip.atomicx.dto.TransactionResponseDto;
import com.akshadip.atomicx.mappers.TransactionMapper;
import com.akshadip.atomicx.models.LedgerEntry;
import com.akshadip.atomicx.models.Transaction;
import com.akshadip.atomicx.models.TransactionType;
import com.akshadip.atomicx.repositories.LedgerEntryRepository;
import com.akshadip.atomicx.repositories.TransactionRepository;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LedgerService {

    private final TransactionMapper transactionMapper;
    private final TimeBasedEpochGenerator idGen;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final TransactionRepository transactionRepository;
    LedgerService(TransactionMapper transactionMapper,TimeBasedEpochGenerator idGen,LedgerEntryRepository ledgerEntryRepository,
                  TransactionRepository transactionRepository){
        this.transactionMapper = transactionMapper;
        this.idGen = idGen;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransactionResponseDto executeTransaction(Transaction transaction){
        transactionRepository.save(transaction);
        LedgerEntry ledgerEntryCredit = new LedgerEntry()
                .setId(idGen.generate())
                .setAccountId(transaction.getReceiver())
                .setAmount(transaction.getAmount())
                .setTransactionType(TransactionType.CREDIT)
                .setTransaction(transaction);

        LedgerEntry ledgerEntryDebit = new LedgerEntry()
                .setId(idGen.generate())
                .setAccountId(transaction.getSender())
                .setAmount(transaction.getAmount().negate())
                .setTransactionType(TransactionType.DEBIT)
                .setTransaction(transaction);
        ledgerEntryRepository.saveAll(List.of(ledgerEntryDebit,ledgerEntryCredit));
        return transactionMapper.toResponse(transaction);
    }

}
