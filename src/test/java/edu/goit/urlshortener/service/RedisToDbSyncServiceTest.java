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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testSynchronizeWithNonEmptyCache() {
        Set<String> cacheKeys = new HashSet<>(Arrays.asList(
                "urlCache::abc123",
                "urlCache::xyz456::clickCount"
        ));
        when(redisTemplate.keys("urlCache::*")).thenReturn(cacheKeys);


        List<Url> dbUrls = new ArrayList<>();
        Url dbUrl = new Url();
        dbUrl.setShortLink("abc123");
        dbUrl.setClickCount(10L);
        dbUrls.add(dbUrl);

        when(urlRepository.findByShortLinkIn(anyList())).thenReturn(dbUrls);


        Url cachedUrl = new Url();
        cachedUrl.setShortLink("abc123");
        cachedUrl.setClickCount(15L);
        when(valueOperations.get("urlCache::abc123")).thenReturn(cachedUrl);

        redisToDbSyncService.synchronize();

        verify(redisTemplate, times(1)).keys("urlCache::*");
        verify(urlRepository, times(1)).findByShortLinkIn(anyList());
        verify(urlRepository, times(1)).saveAll(dbUrls);
        assertEquals(15L, dbUrl.getClickCount());
    }

    @Test
    void testSynchronizeWithEmptyCache() {
        when(redisTemplate.keys("urlCache::*")).thenReturn(Collections.emptySet());

        redisToDbSyncService.synchronize();

        verify(redisTemplate, times(1)).keys("urlCache::*");
        verify(urlRepository, times(0)).findByShortLinkIn(anyList());
        verify(urlRepository, times(0)).saveAll(anyList());
    }

    @Test
    void testSynchronizeWithNoKeysToUpdate() {
        Set<String> cacheKeys = new HashSet<>(Arrays.asList("urlCache::abc123", "urlCache::xyz456"));
        when(redisTemplate.keys("urlCache::*")).thenReturn(cacheKeys);

        when(urlRepository.findByShortLinkIn(anyList())).thenReturn(Collections.emptyList());

        redisToDbSyncService.synchronize();

        verify(redisTemplate, times(1)).keys("urlCache::*");
        verify(urlRepository, times(1)).findByShortLinkIn(anyList());
        verify(urlRepository, times(0)).saveAll(anyList());
    }

    @Test
    void testSynchronizeWithMatchingClickCounts() {
        Set<String> cacheKeys = new HashSet<>(Collections.singletonList("urlCache::abc123"));
        when(redisTemplate.keys("urlCache::*")).thenReturn(cacheKeys);

        Url dbUrl = new Url();
        dbUrl.setShortLink("abc123");
        dbUrl.setClickCount(10L);

        when(urlRepository.findByShortLinkIn(anyList())).thenReturn(Collections.singletonList(dbUrl));

        Url cachedUrl = new Url();
        cachedUrl.setShortLink("abc123");
        cachedUrl.setClickCount(10L);

        when(valueOperations.get("urlCache::abc123")).thenReturn(cachedUrl);

        redisToDbSyncService.synchronize();

        verify(redisTemplate, times(1)).keys("urlCache::*");
        verify(urlRepository, times(1)).findByShortLinkIn(anyList());
    }
}
