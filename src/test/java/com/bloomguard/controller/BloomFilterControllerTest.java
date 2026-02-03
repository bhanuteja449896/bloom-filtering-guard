package com.bloomguard.controller;

import com.bloomguard.model.dto.request.CheckRequest;
import com.bloomguard.model.dto.response.ApiResponse;
import com.bloomguard.model.dto.response.CheckResponse;
import com.bloomguard.security.TenantContext;
import com.bloomguard.service.AuditService;
import com.bloomguard.service.BackupRecoveryService;
import com.bloomguard.service.BloomFilterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BloomFilterController.class)
class BloomFilterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BloomFilterService bloomFilterService;

    @MockBean
    private AuditService auditService;

    @MockBean
    private BackupRecoveryService backupRecoveryService;

    @Test
    @WithMockUser
    void check_shouldReturnCorrectResponse() throws Exception {
        when(bloomFilterService.mightContain(anyString(), anyString())).thenReturn(true);

        CheckRequest request = new CheckRequest("test-filter", "test-item");

        mockMvc.perform(post("/api/v1/bloom/check")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.filterName").value("test-filter"))
                .andExpect(jsonPath("$.data.mightExist").value(true));
    }

    @Test
    @WithMockUser
    void check_shouldValidateRequest() throws Exception {
        CheckRequest request = new CheckRequest("", "test-item");

        mockMvc.perform(post("/api/v1/bloom/check")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void check_shouldRequireAuthentication() throws Exception {
        CheckRequest request = new CheckRequest("test-filter", "test-item");

        mockMvc.perform(post("/api/v1/bloom/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
