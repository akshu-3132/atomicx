package com.akshadip.atomicx.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.val;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Account {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID accountId;

    @NotNull
    private String firstName;

    @NotBlank
    @Column(unique = true)
    private String userName;

    @Email
    private String email;
}
