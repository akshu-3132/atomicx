package com.akshadip.atomicx.repositories;

import com.akshadip.atomicx.models.Account;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Accessors(chain = true)
@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByUserName(String userName);

    @Query("Select a.accountId from Account a where a.userName = ?1")
    Optional<UUID> getAccountId(String userName);

    @Query("Select a.userName from Account a where a.accountId = ?1")
    Optional<String> getUserName(UUID accountId);
}
