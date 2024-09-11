package edu.goit.urlshortener.service;

import edu.goit.urlshortener.exception.MyEntityNotFoundException;
import edu.goit.urlshortener.model.Url;
import edu.goit.urlshortener.repo.UrlRepository;
import edu.goit.urlshortener.repo.UserRepository;
import edu.goit.urlshortener.security.model.User;
import edu.goit.urlshortener.service.impl.UrlServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UrlServiceImplTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, Url> redisTemplate;

    @InjectMocks
    private UrlServiceImpl urlService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private User authUser;
    private Url mockUrl;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authUser = new User();
        authUser.setId(1L);
        authUser.setUsername("testUser");

        mockUrl = Url.builder()
                .id(1L)
                .nativeLink("https://example.com")
                .shortLink("shortLink")
                .clickCount(0L)
                .createdAt(LocalDateTime.now())
                .expiredTime(LocalDateTime.now().plusMonths(1))
                .user(authUser)
                .build();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(authUser.getUsername());
        SecurityContextHolder.setContext(securityContext);
    }


    @Test
    void testCreateShortLinkSuccess() {
        String longUrl = "https://example.com";
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(authUser));
        when(urlRepository.save(any(Url.class))).thenReturn(mockUrl);

        String shortLink = urlService.createShortLink(longUrl);

        assertNotSame(shortLink, longUrl);
        verify(urlRepository, times(1)).save(any(Url.class));
    }

    @Test
    void testCreateShortLinkUserNotFound() {
        when(userRepository.findByUsername(authUser.getUsername())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            urlService.createShortLink("longUrl");
        }, "User not found with username " + authUser.getUsername());
        verify(urlRepository, never()).save(any(Url.class));
    }


    @Test
    void testGetDestinationLinkFromCache() {
        ValueOperations<String, Url> valueOperations = mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.get("urlCache::abc123")).thenReturn(mockUrl);

        when(valueOperations.increment("urlCache::abc123::clickCount", 1)).thenReturn(1L);

        String destination = urlService.getDestinationLink("abc123");

        assertEquals("https://example.com", destination);

        verify(valueOperations, times(1)).get("urlCache::abc123");
        verify(valueOperations, times(1)).increment("urlCache::abc123::clickCount", 1);
    }


    @Test
    void testGetDestinationLinkNotFound() {
        when(urlRepository.findByShortLink("nonExistentLink")).thenReturn(Optional.empty());

        assertThrows(NullPointerException.class, () -> urlService.getDestinationLink("nonExistentLink"));
    }

    @Test
    void testFindAllActiveUrlsSuccess() {
        List<String> mockSlugsList = List.of("slug1", "slug2");
        Pageable pageable = PageRequest.of(0, 2);
        when(userRepository.findByUsername(authUser.getUsername())).thenReturn(Optional.of(authUser));
        when(urlRepository.findAllActiveSlugsByUserId(authUser)).thenReturn(Optional.of(mockSlugsList));

        Page<String> result = urlService.findAllActiveUrls(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(urlRepository, times(1)).findAllActiveSlugsByUserId(authUser);
    }

    @Test
    void testFindAllActiveUrlsUserNotFound() {
        when(userRepository.findByUsername(authUser.getUsername())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> urlService.findAllActiveUrls(PageRequest.of(0, 2)));
    }

    @Test
    void testDeleteShortLinkSuccess() {
        when(urlRepository.findByShortLink("shortLink")).thenReturn(Optional.of(mockUrl));

        urlService.deleteShortLink("shortLink");

        verify(urlRepository, times(1)).delete(mockUrl);
    }

    @Test
    void testDeleteShortLinkNotFound() {
        when(urlRepository.findByShortLink("nonExistentLink")).thenReturn(Optional.empty());

        assertThrows(MyEntityNotFoundException.class, () -> urlService.deleteShortLink("nonExistentLink"));
        verify(urlRepository, never()).delete(any(Url.class));
    }

}
