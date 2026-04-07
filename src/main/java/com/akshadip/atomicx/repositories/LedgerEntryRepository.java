package com.akshadip.atomicx.repositories;

import com.akshadip.atomicx.models.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    @Query("SELECT SUM(l.amount) FROM LedgerEntry l WHERE l.accountId = :accountId")
    BigDecimal getBalance(UUID accountId);
}
