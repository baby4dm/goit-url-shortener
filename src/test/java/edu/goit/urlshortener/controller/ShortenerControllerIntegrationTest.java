package edu.goit.urlshortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.goit.urlshortener.model.requests.LinkRequest;
import edu.goit.urlshortener.model.responses.ShortLinkResponse;
import edu.goit.urlshortener.service.impl.UrlServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ShortenerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UrlServiceImpl urlService;

    @Autowired
    private ObjectMapper objectMapper;

    private LinkRequest linkRequest;
    private ShortLinkResponse shortLinkResponse;
    private Page<String> activeUrls;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));


    @BeforeEach
    void setUp() {
        linkRequest = new LinkRequest("https://example.com");

        shortLinkResponse = ShortLinkResponse.builder()
                .clickCount(5L)
                .slug("shortSlug")
                .destination("https://example.com")
                .createdAt(LocalDateTime.now())
                .expiredTime(LocalDateTime.now().plusMonths(1))
                .build();

        activeUrls = new PageImpl<>(List.of("slug1", "slug2"));
    }

    @Test
    void testCreateShortLinkSuccess() throws Exception {
        // Arrange
        when(urlService.createShortLink(any(String.class))).thenReturn("https://localhost:8080/shortSlug");

        // Act & Assert
        mockMvc.perform(post("/api/v1/urls/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(linkRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("https://localhost:8080/shortSlug"));
    }

    @Test
    void testRedirectSuccess() throws Exception {
        // Arrange
        when(urlService.getDestinationLink("shortSlug")).thenReturn("https://example.com");

        // Act & Assert
        mockMvc.perform(get("/api/v1/urls/shortSlug"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com"));
    }

    @Test
    void testGetShortLinkInfoSuccess() throws Exception {
        // Arrange
        when(urlService.getShortLinkDto("shortSlug")).thenReturn(shortLinkResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/urls/info/shortSlug"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clickCount", is(5)))
                .andExpect(jsonPath("$.slug", is("shortSlug")))
                .andExpect(jsonPath("$.destination", is("https://example.com")));
    }

    @Test
    void testGetAllActiveUrlsSuccess() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2);
        when(urlService.findAllActiveUrls(pageable)).thenReturn(activeUrls);

        // Act & Assert
        mockMvc.perform(get("/api/v1/urls/findAllActive")
                        .param("offset", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0]", is("slug1")))
                .andExpect(jsonPath("$.content[1]", is("slug2")));
    }

    @Test
    void testDeleteShortLinkSuccess() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/urls/shortSlug"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testExtendExpirationDateSuccess() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v1/urls/extend/shortSlug"))
                .andExpect(status().isNoContent());
    }
}
