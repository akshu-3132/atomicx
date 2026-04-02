package com.akshadip.atomicx.dto;

import com.akshadip.atomicx.models.TransactionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class TransactionResponseDto {
    private String senderUserName;
    private String receiverUserName;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;


}
