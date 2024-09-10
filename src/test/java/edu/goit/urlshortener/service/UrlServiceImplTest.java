package edu.goit.urlshortener.service;

import edu.goit.urlshortener.model.Url;
import edu.goit.urlshortener.repo.UrlRepository;
import edu.goit.urlshortener.repo.UserRepository;
import edu.goit.urlshortener.security.model.User;
import edu.goit.urlshortener.service.impl.UrlServiceImpl;
import edu.goit.urlshortener.util.Base62Encoder;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

//@ExtendWith(MockitoExtension.class)
public class UrlServiceImplTest {

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
        // Arrange
        String longUrl = "https://example.com";
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(authUser));
        when(urlRepository.save(any(Url.class))).thenReturn(mockUrl);

        // Act
        String shortLink = urlService.createShortLink(longUrl);

        // Assert
        assertNotSame(shortLink, longUrl);
        verify(urlRepository, times(1)).save(any(Url.class));
    }

    @Test
    void testCreateShortLinkUserNotFound() {
        // Arrange
        when(userRepository.findByUsername(authUser.getUsername())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {urlService.createShortLink("longUrl");},"User not found with username " + authUser.getUsername());
        verify(urlRepository, never()).save(any(Url.class));
    }


    @Test
    void testGetDestinationLinkFromCache() {
        // Arrange
        // Create a mock for ValueOperations<String, Url>
        ValueOperations<String, Url> valueOperations = mock(ValueOperations.class);

        // Mock redisTemplate.opsForValue() to return the mocked valueOperations
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Mock the behavior of valueOperations to return the URL when get() is called
        when(valueOperations.get("urlCache::abc123")).thenReturn(mockUrl);

        // Mock the behavior of valueOperations to increment the click count
        when(valueOperations.increment("urlCache::abc123::clickCount", 1)).thenReturn(1L);

        // Act
        String destination = urlService.getDestinationLink("abc123");

        // Assert
        assertEquals("https://example.com", destination);

        // Verify the correct interactions with the mocks
        verify(valueOperations, times(1)).get("urlCache::abc123");
        verify(valueOperations, times(1)).increment("urlCache::abc123::clickCount", 1);
    }



    @Test
    void testGetDestinationLinkNotFound() {
        // Arrange
        when(urlRepository.findByShortLink("nonExistentLink")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NullPointerException.class, () -> urlService.getDestinationLink("nonExistentLink"));
    }


    @Test
    void testGetShortLinkDtoNotFound() {
        // Arrange
        when(urlRepository.findByShortLink("nonExistentLink")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> urlService.getShortLinkDto("nonExistentLink"));
    }

    @Test
    void testFindAllActiveUrlsSuccess() {
        // Arrange
        List<String> mockSlugsList = List.of("slug1", "slug2");
        Pageable pageable = PageRequest.of(0, 2);
        when(userRepository.findByUsername(authUser.getUsername())).thenReturn(Optional.of(authUser));
        when(urlRepository.findAllActiveSlugsByUserId(authUser)).thenReturn(Optional.of(mockSlugsList));

        // Act
        Page<String> result = urlService.findAllActiveUrls(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(urlRepository, times(1)).findAllActiveSlugsByUserId(authUser);
    }

    @Test
    void testFindAllActiveUrlsUserNotFound() {
        // Arrange
        when(userRepository.findByUsername(authUser.getUsername())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> urlService.findAllActiveUrls(PageRequest.of(0, 2)));
    }

    @Test
    void testDeleteShortLinkSuccess() {
        // Arrange
        when(urlRepository.findByShortLink("shortLink")).thenReturn(Optional.of(mockUrl));

        // Act
        urlService.deleteShortLink("shortLink");

        // Assert
        verify(urlRepository, times(1)).delete(mockUrl);
    }

    @Test
    void testDeleteShortLinkNotFound() {
        // Arrange
        when(urlRepository.findByShortLink("nonExistentLink")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> urlService.deleteShortLink("nonExistentLink"));
        verify(urlRepository, never()).delete(any(Url.class));
    }

}
