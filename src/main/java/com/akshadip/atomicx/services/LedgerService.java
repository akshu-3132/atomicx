package com.akshadip.atomicx.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.akshadip.atomicx.dto.TransactionResponseDto;
import com.akshadip.atomicx.mappers.TransactionMapper;
import com.akshadip.atomicx.models.LedgerEntry;
import com.akshadip.atomicx.models.Transaction;
import com.akshadip.atomicx.models.TransactionType;
import com.akshadip.atomicx.repositories.LedgerEntryRepository;
import com.akshadip.atomicx.repositories.TransactionRepository;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;

@Service
public class LedgerService {

    private final TransactionMapper transactionMapper;
    private final TimeBasedEpochGenerator idGen;
    private final LedgerEntryRepository ledgerEntryRepository;
    LedgerService(TransactionMapper transactionMapper,TimeBasedEpochGenerator idGen,LedgerEntryRepository ledgerEntryRepository,
                  TransactionRepository transactionRepository){
        this.transactionMapper = transactionMapper;
        this.idGen = idGen;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional(timeout = 7)
    public TransactionResponseDto executeTransaction(Transaction transaction){
        BigDecimal creditAmount = transaction.getAmount();
        BigDecimal debitAmount = transaction.getAmount().negate();

        // Double-entry validation: credits must equal debits (net = 0)
        BigDecimal netAmount = creditAmount.add(debitAmount);
        if (netAmount.compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException(
                "Double-entry accounting violation: debits (" + debitAmount + 
                ") + credits (" + creditAmount + ") = " + netAmount
            );
        }

        LedgerEntry ledgerEntryCredit = new LedgerEntry()
                .setId(idGen.generate())
                .setAccountId(transaction.getReceiver())
                .setAmount(creditAmount)
                .setTransactionType(TransactionType.CREDIT)
                .setTransaction(transaction);

        LedgerEntry ledgerEntryDebit = new LedgerEntry()
                .setId(idGen.generate())
                .setAccountId(transaction.getSender())
                .setAmount(debitAmount)
                .setTransactionType(TransactionType.DEBIT)
                .setTransaction(transaction);
        ledgerEntryRepository.saveAll(List.of(ledgerEntryDebit,ledgerEntryCredit));
        return transactionMapper.toResponse(transaction);
    }

}
