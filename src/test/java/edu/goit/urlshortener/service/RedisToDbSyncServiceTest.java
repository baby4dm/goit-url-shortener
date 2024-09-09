package edu.goit.urlshortener.service;

import edu.goit.urlshortener.model.Url;
import edu.goit.urlshortener.repo.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RedisToDbSyncServiceTest {

    @Mock
    private RedisTemplate<String, Url> redisTemplate;

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private ValueOperations<String, Url> valueOperations;

    @InjectMocks
    private RedisToDbSyncService redisToDbSyncService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initializes mocks
        when(redisTemplate.opsForValue()).thenReturn(valueOperations); // Mock ValueOperations behavior
    }

    @Test
    void testSynchronizeWithNonEmptyCache() {
        // Arrange
        // Mock Redis keys
        Set<String> cacheKeys = new HashSet<>(Arrays.asList(
                "urlCache::abc123",
                "urlCache::xyz456::clickCount"
        ));
        when(redisTemplate.keys("urlCache::*")).thenReturn(cacheKeys);

        // Mock database response
        List<Url> dbUrls = new ArrayList<>();
        Url dbUrl = new Url();
        dbUrl.setShortLink("abc123");
        dbUrl.setClickCount(10L);
        dbUrls.add(dbUrl);

        when(urlRepository.findByShortLinkIn(anyList())).thenReturn(dbUrls);

        // Mock Redis cached URL data
        Url cachedUrl = new Url();
        cachedUrl.setShortLink("abc123");
        cachedUrl.setClickCount(15L);  // Simulate an updated click count
        when(valueOperations.get("urlCache::abc123")).thenReturn(cachedUrl);

        // Act
        redisToDbSyncService.synchronize();

        // Assert
        verify(redisTemplate, times(1)).keys("urlCache::*");
        verify(urlRepository, times(1)).findByShortLinkIn(anyList());
        verify(urlRepository, times(1)).saveAll(dbUrls);
        assertEquals(15L, dbUrl.getClickCount());  // Verify the click count has been updated
    }

    @Test
    void testSynchronizeWithEmptyCache() {
        // Arrange
        when(redisTemplate.keys("urlCache::*")).thenReturn(Collections.emptySet());

        // Act
        redisToDbSyncService.synchronize();

        // Assert
        verify(redisTemplate, times(1)).keys("urlCache::*");
        verify(urlRepository, times(0)).findByShortLinkIn(anyList());  // Should not query the database
        verify(urlRepository, times(0)).saveAll(anyList());  // Should not save anything
    }

    @Test
    void testSynchronizeWithNoKeysToUpdate() {
        // Arrange
        Set<String> cacheKeys = new HashSet<>(Arrays.asList("urlCache::abc123", "urlCache::xyz456"));
        when(redisTemplate.keys("urlCache::*")).thenReturn(cacheKeys);

        // Mock database response with no matching URLs
        when(urlRepository.findByShortLinkIn(anyList())).thenReturn(Collections.emptyList());

        // Act
        redisToDbSyncService.synchronize();

        // Assert
        verify(redisTemplate, times(1)).keys("urlCache::*");
        verify(urlRepository, times(1)).findByShortLinkIn(anyList());
        verify(urlRepository, times(0)).saveAll(anyList());  // No save operation should be performed
    }

    @Test
    void testSynchronizeWithMatchingClickCounts() {
        // Arrange
        Set<String> cacheKeys = new HashSet<>(Collections.singletonList("urlCache::abc123"));
        when(redisTemplate.keys("urlCache::*")).thenReturn(cacheKeys);

        // Mock database and Redis responses with the same click count
        Url dbUrl = new Url();
        dbUrl.setShortLink("abc123");
        dbUrl.setClickCount(10L);

        when(urlRepository.findByShortLinkIn(anyList())).thenReturn(Collections.singletonList(dbUrl));

        Url cachedUrl = new Url();
        cachedUrl.setShortLink("abc123");
        cachedUrl.setClickCount(10L);  // Same click count as dbUrl

        when(valueOperations.get("urlCache::abc123")).thenReturn(cachedUrl);

        // Act
        redisToDbSyncService.synchronize();

        // Assert
        verify(redisTemplate, times(1)).keys("urlCache::*");
        verify(urlRepository, times(1)).findByShortLinkIn(anyList());
        verify(urlRepository, times(0)).saveAll(anyList());  // No save operation since click counts are the same
    }
}
