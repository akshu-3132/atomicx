package com.akshadip.atomicx.services;

import com.akshadip.atomicx.repositories.TransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;

    TransactionService(TransactionRepository transactionRepository){
        this.transactionRepository = transactionRepository;
    }

}
