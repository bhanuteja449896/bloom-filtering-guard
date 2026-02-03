package com.bloomguard.integration;

import com.bloomguard.BloomGuardApplication;
import com.bloomguard.model.dto.request.AddRequest;
import com.bloomguard.model.dto.request.CheckRequest;
import com.bloomguard.model.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BloomGuardApplication.class)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("integration")
class BloomFilterIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine")
    );

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("redis:7-alpine")
    ).withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void healthCheck_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void addAndCheck_fullWorkflow() throws Exception {
        String filterName = "integration-test-filter";
        String item = "test-item-" + System.currentTimeMillis();

        CheckRequest checkRequest = new CheckRequest(filterName, item);
        mockMvc.perform(post("/api/v1/bloom/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Key", "test-key")
                        .content(objectMapper.writeValueAsString(checkRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mightExist").value(false));

        AddRequest addRequest = new AddRequest(filterName, item);
        mockMvc.perform(post("/api/v1/bloom/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Key", "test-key")
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.added").value(true));

        mockMvc.perform(post("/api/v1/bloom/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-API-Key", "test-key")
                        .content(objectMapper.writeValueAsString(checkRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mightExist").value(true));
    }
}
