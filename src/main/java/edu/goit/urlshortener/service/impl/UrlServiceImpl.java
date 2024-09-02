package edu.goit.urlshortener.service.impl;

import edu.goit.urlshortener.model.Url;
import edu.goit.urlshortener.model.dto.ShortLinkResponse;
import edu.goit.urlshortener.repo.UrlRepository;
import edu.goit.urlshortener.repo.UserRepository;
import edu.goit.urlshortener.security.model.User;
import edu.goit.urlshortener.service.UrlService;
import edu.goit.urlshortener.util.Base62Encoder;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {
    private final UserRepository userRepository;
    private final UrlRepository urlRepository;

    @Transactional
    public String createShortLink(String longUrl) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User authUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username " + username));

        Url url = Url.builder()
                .nativeLink(longUrl)
                .shortLink("") // Temporary value
                .clickCount(0L)
                .createdAt(LocalDateTime.now())
                .expiredTime(LocalDateTime.now().plusMonths(1))
                .user(authUser)
                .build();

        Url saved = urlRepository.save(url);
        String slugStr = Base62Encoder.encode(saved.getId());
        saved.setShortLink(slugStr);
        return "https://localhost:8080/" + slugStr;
    }

    @Transactional
    public String getDestinationLink(String slug) {
        Url url = urlRepository.findByShortLink(slug).orElseThrow(EntityNotFoundException::new);
        url.setClickCount(url.getClickCount() + 1L);
        return url.getNativeLink();
    }

    public ShortLinkResponse getShortLinkDto(String slug) {
        Url url = urlRepository.findByShortLink(slug).orElseThrow(EntityNotFoundException::new);
        return ShortLinkResponse.builder()
                .clickCount(url.getClickCount())
                .slug(url.getShortLink())
                .destination(url.getNativeLink())
                .createdAt(url.getCreatedAt())
                .expiredTime(url.getExpiredTime())
                .build();
    }

    public Page<String> findAllActiveUrls(Pageable pageable) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User authUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username " + username));

        List<String> slugsList = urlRepository.findAllActiveSlugsByUserId(authUser)
                .orElseThrow(EntityNotFoundException::new);
        return new PageImpl<>(slugsList, pageable, slugsList.size());
    }

    public void deleteShortLink(String slug) {
        Url url = urlRepository.findByShortLink(slug)
                .orElseThrow(() -> new EntityNotFoundException("Short link not found"));
        urlRepository.delete(url);
    }
}
