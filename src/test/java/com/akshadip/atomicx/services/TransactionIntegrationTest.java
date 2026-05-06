package com.akshadip.atomicx.services;

import com.akshadip.atomicx.controllers.AccountController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private TransactionService transactionService;

    private final List<String> userNames = List.of("test1", "test2", "test3", "test4", "test5", "test6", "test7", "test8", "test9", "test10");
    private final List<String> firstNames = List.of("Alice", "Bob", "Charlie", "David", "Eve", "Frank", "Grace", "Henry", "Ivy", "Jack");
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        for (int idx = 0; idx < userNames.size(); idx++) {
            String userName = userNames.get(idx);
            String firstName = firstNames.get(idx);
            var userMap = Map.of(
                    "firstName", firstName ,
                    "email", "",
                    "userName", userName
            );
            mockMvc.perform(post("/api/accounts/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userMap)))
                    .andExpect(status().isOk());

        }
    }

    /**
     * Verifies the integrity of the system's total balance under heavy concurrency.
     *
     * Simulates multiple random, simultaneous transfers between users.
     * Asserts that the total sum of all balances across the system remains exactly
     * the same before and after the transfers, and that no account drops below zero.
     */
    @Test
    void totalBalanceMustRemainSame() throws Exception {
        int numberOfTransactions = 100;
        BigDecimal totalBalance = BigDecimal.ZERO;
        for (String name : userNames) {
            BigDecimal userBalance = transactionService.balance(name);
            totalBalance = totalBalance.add(userBalance);
        }
        ExecutorService executorService = Executors.newFixedThreadPool(15);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfTransactions);
        Random random = new Random();
        for (int idx = 0; idx < numberOfTransactions; idx++) {
            String sender = userNames.get(random.nextInt(userNames.size()));
            String receiver;
            do {
                receiver = userNames.get(random.nextInt(userNames.size()));
            } while (receiver.equals(sender));
            String idempotencyKey = String.valueOf(UUID.randomUUID());
            var transactionMap = Map.of(
                    "senderName", sender,
                    "receiverName", receiver,
                    "amount", BigDecimal.valueOf(random.nextInt(50) + 1));
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    mockMvc.perform(post("/api/transactions/transfer")
                            .header("Idempotency-Key", idempotencyKey)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(transactionMap))
                    );


                } catch (Exception e) {
                    System.err.println("Transaction Failed :  " + e.getMessage());
                } finally {
                    endLatch.countDown();
                    executorService.shutdown();
                }

            });
        }
        startLatch.countDown();
        boolean completed = endLatch.await(30, java.util.concurrent.TimeUnit.SECONDS);
        assertTrue(completed, "All transactions should complete within timeout");
        for(String userName : userNames){
            assertTrue(transactionService.balance(userName).compareTo(BigDecimal.ZERO) >= 0);
        }
        BigDecimal finalTotalBalance = BigDecimal.ZERO;
        for (String userName : userNames) {
            BigDecimal userBalance = transactionService.balance(userName);
            finalTotalBalance = finalTotalBalance.add(userBalance);
        }
        assertEquals(0, totalBalance.compareTo(finalTotalBalance),
                "Total balance should remain same after all transactions");
    }
}
