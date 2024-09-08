package edu.goit.urlshortener.service;

import edu.goit.urlshortener.model.Url;
import edu.goit.urlshortener.model.dto.ShortLinkResponse;
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
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Testcontainers
public class UrlServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UrlRepository urlRepository;

    @InjectMocks
    private UrlServiceImpl urlService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private User mockUser;
    private Url mockUrl;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");

        mockUrl = Url.builder()
                .id(1L)
                .nativeLink("https://example.com")
                .shortLink("shortLink")
                .clickCount(0L)
                .createdAt(LocalDateTime.now())
                .expiredTime(LocalDateTime.now().plusMonths(1))
                .user(mockUser)
                .build();

        // Mock SecurityContextHolder to return the username
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(mockUser.getUsername());
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testCreateShortLinkSuccess() {
        // Arrange
        when(userRepository.findByUsername(mockUser.getUsername())).thenReturn(Optional.of(mockUser));
        when(urlRepository.save(any(Url.class))).thenReturn(mockUrl);

        // Act
        String shortLink = urlService.createShortLink("https://example.com");

        // Assert
        assertNotNull(shortLink);
        verify(urlRepository, times(1)).save(any(Url.class));
    }

    @Test
    void testCreateShortLinkUserNotFound() {
        // Arrange
        when(userRepository.findByUsername(mockUser.getUsername())).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> urlService.createShortLink("https://example.com"));
        assertEquals("User not found with username " + mockUser.getUsername(), exception.getMessage());
        verify(urlRepository, never()).save(any(Url.class));
    }

    @Test
    void testGetDestinationLinkSuccess() {
        // Arrange
        when(urlRepository.findByShortLink("shortLink")).thenReturn(Optional.of(mockUrl));

        // Act
        String destinationLink = urlService.getDestinationLink("shortLink");

        // Assert
        assertEquals("https://example.com", destinationLink);
        assertEquals(1L, mockUrl.getClickCount()); // click count should increment
        verify(urlRepository, times(1)).findByShortLink("shortLink");
    }

    @Test
    void testGetDestinationLinkNotFound() {
        // Arrange
        when(urlRepository.findByShortLink("nonExistentLink")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> urlService.getDestinationLink("nonExistentLink"));
    }

    @Test
    void testGetShortLinkDtoSuccess() {
        // Arrange
        when(urlRepository.findByShortLink("shortLink")).thenReturn(Optional.of(mockUrl));

        // Act
        ShortLinkResponse response = urlService.getShortLinkDto("shortLink");

        // Assert
        assertNotNull(response);
        assertEquals("shortLink", response.getSlug());
        assertEquals("https://example.com", response.getDestination());
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
        when(userRepository.findByUsername(mockUser.getUsername())).thenReturn(Optional.of(mockUser));
        when(urlRepository.findAllActiveSlugsByUserId(mockUser)).thenReturn(Optional.of(mockSlugsList));

        // Act
        Page<String> result = urlService.findAllActiveUrls(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(urlRepository, times(1)).findAllActiveSlugsByUserId(mockUser);
    }

    @Test
    void testFindAllActiveUrlsUserNotFound() {
        // Arrange
        when(userRepository.findByUsername(mockUser.getUsername())).thenReturn(Optional.empty());

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
