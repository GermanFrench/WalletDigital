package com.tuusuario.wallet.interfaces.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuusuario.wallet.domain.model.Account;
import com.tuusuario.wallet.domain.model.AuthUser;
import com.tuusuario.wallet.domain.model.User;
import com.tuusuario.wallet.domain.repository.AccountRepository;
import com.tuusuario.wallet.domain.repository.AuthUserRepository;
import com.tuusuario.wallet.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldTransferByEmailAndUpdateBothBalances() throws Exception {
        TestIdentity source = createIdentity("source");
        TestIdentity destination = createIdentity("destination");

        source.account.deposit(new BigDecimal("100.00"));
        accountRepository.save(source.account);

        String token = loginAndGetAccessToken(source.email, source.password);

        mockMvc.perform(post("/api/accounts/{accountId}/transfer-by-email", source.account.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "emailOrAlias": "%s",
                                  "amount": 25.00
                                }
                                """.formatted(destination.email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(source.account.getId().toString()))
                .andExpect(jsonPath("$.balance").value(75.00));

        mockMvc.perform(get("/api/accounts/{accountId}", destination.account.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(destination.account.getId().toString()))
                .andExpect(jsonPath("$.balance").value(25.00));
    }

                @Test
                void shouldReturnNotFoundWhenTransferByEmailRecipientDoesNotExist() throws Exception {
                                TestIdentity source = createIdentity("missing-target");
                                source.account.deposit(new BigDecimal("100.00"));
                                accountRepository.save(source.account);

                                String token = loginAndGetAccessToken(source.email, source.password);

                                mockMvc.perform(post("/api/accounts/{accountId}/transfer-by-email", source.account.getId())
                                                                                                .header("Authorization", "Bearer " + token)
                                                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                                                .content("""
                                                                                                                                {
                                                                                                                                        "emailOrAlias": "nobody.not-found@example.com",
                                                                                                                                        "amount": 10.00
                                                                                                                                }
                                                                                                                                """))
                                                                .andExpect(status().isNotFound())
                                                                .andExpect(jsonPath("$.message").value("No se encontró ninguna cuenta para: nobody.not-found@example.com"))
                                                                .andExpect(jsonPath("$.status").value(404));
                }

                @Test
                void shouldRejectTransferByEmailToSameAccount() throws Exception {
                                TestIdentity source = createIdentity("self-transfer");
                                source.account.deposit(new BigDecimal("50.00"));
                                accountRepository.save(source.account);

                                String token = loginAndGetAccessToken(source.email, source.password);

                                mockMvc.perform(post("/api/accounts/{accountId}/transfer-by-email", source.account.getId())
                                                                                                .header("Authorization", "Bearer " + token)
                                                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                                                .content("""
                                                                                                                                {
                                                                                                                                        "emailOrAlias": "%s",
                                                                                                                                        "amount": 10.00
                                                                                                                                }
                                                                                                                                """.formatted(source.email)))
                                                                .andExpect(status().isBadRequest())
                                                                .andExpect(jsonPath("$.message").value("No puedes transferirte dinero a tu misma cuenta"))
                                                                .andExpect(jsonPath("$.status").value(400));
                }

    @Test
    void shouldReturnPaginatedTransactionsForAccount() throws Exception {
        TestIdentity source = createIdentity("pager");
        String token = loginAndGetAccessToken(source.email, source.password);

        mockMvc.perform(post("/api/accounts/{accountId}/deposit", source.account.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 10.00
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/accounts/{accountId}/deposit", source.account.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 20.00
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/accounts/{accountId}/deposit", source.account.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 30.00
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/accounts/{accountId}/transactions", source.account.getId())
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].accountId").value(source.account.getId().toString()))
                .andExpect(jsonPath("$[1].accountId").value(source.account.getId().toString()))
                .andExpect(jsonPath("$[0].type").value("CREDIT"));
    }

    private String loginAndGetAccessToken(String email, String password) throws Exception {
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(loginResponse);
        String accessToken = jsonNode.get("accessToken").asText();
        assertNotNull(accessToken);
        return accessToken;
    }

    private TestIdentity createIdentity(String prefix) {
        UUID userId = UUID.randomUUID();
        String email = prefix + "." + UUID.randomUUID() + "@example.com";
        String password = "ChangeMe123!";

        userRepository.save(User.create(userId, prefix + " user", email));
        authUserRepository.save(AuthUser.create(
                userId,
                email,
                passwordEncoder.encode(password),
                "USER",
                true
        ));

        String accountNumber = "8" + String.format("%021d", Math.abs(System.nanoTime() % 1_000_000_000_000_000_000L));
        String alias = prefix + "." + Math.abs(System.nanoTime());
        Account account = accountRepository.save(Account.open(
                UUID.randomUUID(),
                userId,
                "USD",
                accountNumber,
                alias
        ));

        return new TestIdentity(email, password, account);
    }

    private record TestIdentity(String email, String password, Account account) {
    }
}
