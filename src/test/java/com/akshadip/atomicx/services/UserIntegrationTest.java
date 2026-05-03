package com.akshadip.atomicx.services;

import com.akshadip.atomicx.controllers.AccountController;
import com.akshadip.atomicx.dto.AccountRequestDto;
import com.akshadip.atomicx.dto.AccountResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateAccount() throws Exception{
        String requestBody = """
            {
                "firstName": "test1",
                "userName": "test1",
                "email": "test1@test.com"
            }
        """;
        mockMvc.perform(post("/api/accounts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("test1"))
                .andExpect(jsonPath("$.email").value("test1@test.com"));
    }

    @Test
    void shouldGetAccount() throws Exception{
        String requestBody = """
            {
                "firstName": "test2",
                "userName": "test2",
                "email": "test2@test.com"
            }
        """;
        mockMvc.perform(post("/api/accounts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/accounts/test2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("test2"))
                .andExpect(jsonPath("$.email").value("test2@test.com"));
    }

    @Test
    void shouldReturn404ForNonExistentAccount() throws Exception{
        mockMvc.perform(get("/api/accounts/nonexistent")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
