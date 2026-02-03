package com.bloomguard.service;

import com.bloomguard.model.entity.FilterConfiguration;
import com.bloomguard.repository.FilterConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuavaBloomFilterServiceTest {

    @Mock
    private FilterConfigRepository filterConfigRepository;

    private GuavaBloomFilterService bloomFilterService;

    @BeforeEach
    void setUp() {
        bloomFilterService = new GuavaBloomFilterService(filterConfigRepository);
    }

    @Test
    void addAndCheck_shouldReturnTrueForAddedItem() {
        FilterConfiguration config = new FilterConfiguration();
        config.setExpectedInsertions(1000L);
        config.setFalsePositiveRate(0.01);
        config.setFilterName("test-filter");
        
        when(filterConfigRepository.findByFilterName(anyString()))
                .thenReturn(Optional.of(config));

        boolean added = bloomFilterService.add("test-filter", "test-item");
        boolean exists = bloomFilterService.mightContain("test-filter", "test-item");

        assertTrue(added);
        assertTrue(exists);
    }

    @Test
    void mightContain_shouldReturnFalseForNonExistentItem() {
        FilterConfiguration config = new FilterConfiguration();
        config.setExpectedInsertions(1000L);
        config.setFalsePositiveRate(0.01);
        config.setFilterName("test-filter");
        
        when(filterConfigRepository.findByFilterName(anyString()))
                .thenReturn(Optional.of(config));

        boolean exists = bloomFilterService.mightContain("test-filter", "non-existent-item");

        assertFalse(exists);
    }

    @Test
    void checkAndAdd_shouldReturnCorrectResult() {
        FilterConfiguration config = new FilterConfiguration();
        config.setExpectedInsertions(1000L);
        config.setFalsePositiveRate(0.01);
        config.setFilterName("test-filter");
        
        when(filterConfigRepository.findByFilterName(anyString()))
                .thenReturn(Optional.of(config));

        BloomFilterService.CheckAndAddResult firstResult = 
                bloomFilterService.checkAndAdd("test-filter", "unique-item");
        
        assertFalse(firstResult.existedBefore());
        assertTrue(firstResult.addedNow());

        BloomFilterService.CheckAndAddResult secondResult = 
                bloomFilterService.checkAndAdd("test-filter", "unique-item");
        
        assertTrue(secondResult.existedBefore());
        assertFalse(secondResult.addedNow());
    }

    @Test
    void clear_shouldRemoveAllItems() {
        FilterConfiguration config = new FilterConfiguration();
        config.setExpectedInsertions(1000L);
        config.setFalsePositiveRate(0.01);
        config.setFilterName("test-filter");
        
        when(filterConfigRepository.findByFilterName(anyString()))
                .thenReturn(Optional.of(config));

        bloomFilterService.add("test-filter", "item-1");
        bloomFilterService.add("test-filter", "item-2");
        
        bloomFilterService.clear("test-filter");

        assertFalse(bloomFilterService.filterExists("test-filter"));
    }

    @Test
    void addBatch_shouldAddMultipleItems() {
        FilterConfiguration config = new FilterConfiguration();
        config.setExpectedInsertions(1000L);
        config.setFalsePositiveRate(0.01);
        config.setFilterName("test-filter");
        
        when(filterConfigRepository.findByFilterName(anyString()))
                .thenReturn(Optional.of(config));

        java.util.List<String> items = java.util.List.of("item-1", "item-2", "item-3");
        
        int added = bloomFilterService.addBatch("test-filter", items);

        assertEquals(3, added);
        
        for (String item : items) {
            assertTrue(bloomFilterService.mightContain("test-filter", item));
        }
    }
}
