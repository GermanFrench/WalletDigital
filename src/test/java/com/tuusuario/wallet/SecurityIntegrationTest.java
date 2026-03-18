package com.tuusuario.wallet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuusuario.wallet.domain.model.Account;
import com.tuusuario.wallet.domain.model.User;
import com.tuusuario.wallet.domain.repository.AccountRepository;
import com.tuusuario.wallet.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    private UUID protectedAccountId;

    @BeforeEach
    void setUp() {
        String uniqueEmail = "protected.user." + UUID.randomUUID() + "@example.com";
        User user = userRepository.save(User.create(UUID.randomUUID(), "Protected User", uniqueEmail));
        Account account = accountRepository.save(Account.open(UUID.randomUUID(), user.getId(), "USD"));
        protectedAccountId = account.getId();
    }

    @Test
    void shouldRejectProtectedEndpointWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/accounts/{accountId}", protectedAccountId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldLoginAndAccessProtectedEndpointWithJwt() throws Exception {
        String responseBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@wallet.local",
                                  "password": "ChangeMe123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(responseBody);
        String token = jsonNode.get("accessToken").asText();

        mockMvc.perform(get("/api/accounts/{accountId}", protectedAccountId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(protectedAccountId.toString()));
    }
}

