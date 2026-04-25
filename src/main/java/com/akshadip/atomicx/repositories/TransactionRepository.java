package com.akshadip.atomicx.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.akshadip.atomicx.models.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    @Query("SELECT COUNT(a)>0 FROM Account a WHERE a.accountId = ?1")
    boolean userExists(UUID accountId);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECt t from Transaction t where t.sender = ?1 or t.receiver = ?1 and t.createdAt between ?2 and ?3")
    Optional<List<Transaction>> findAllByUserNameAndDateRange(UUID accountId, Instant start, Instant end);

}
