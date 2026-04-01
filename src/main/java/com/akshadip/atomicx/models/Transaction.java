package com.akshadip.atomicx.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    @Id
    private UUID transactionId;

    @Column(nullable = false, updatable = false)
    private UUID sender;

    @Column(nullable = false, updatable = false)
    private UUID receiver;

    @Column(nullable = false, updatable = false, precision = 19, scale = 4)
    private BigDecimal debitAmount; // Money leaving sender account

    @Column(nullable = false, updatable = false, precision = 19, scale = 4)
    private BigDecimal creditAmount; // Money entering receiver account

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @CreationTimestamp
    @Column(nullable = false,updatable = false)
    private Instant createdAt;
}
