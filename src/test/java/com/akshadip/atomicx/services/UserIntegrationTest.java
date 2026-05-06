package com.akshadip.atomicx.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest
@AutoConfigureMockMvc
public class UserIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateAccount() throws Exception {
        String firstName = "test" + System.currentTimeMillis();
        String userName = "user" + System.currentTimeMillis();
        String email = userName + "@test.com";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("firstName", firstName);
        requestBody.put("userName", userName);
        requestBody.put("email", email);

        mockMvc.perform(post("/api/accounts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value(userName))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void shouldGetAccount() throws Exception {
        String firstName = "test" + System.currentTimeMillis();
        String userName = "user" + System.currentTimeMillis();
        String email = userName + "@test.com";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("firstName", firstName);
        requestBody.put("userName", userName);
        requestBody.put("email", email);

        mockMvc.perform(post("/api/accounts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/accounts/" + userName)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value(userName));
    }

    @Test
    void shouldReturn404ForNonExistentAccount() throws Exception {
        mockMvc.perform(get("/api/accounts/nonexistent")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}