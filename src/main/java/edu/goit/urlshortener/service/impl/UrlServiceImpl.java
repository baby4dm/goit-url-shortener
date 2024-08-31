package edu.goit.urlshortener.service.impl;

import edu.goit.urlshortener.model.Url;
import edu.goit.urlshortener.repo.UrlRepository;
import edu.goit.urlshortener.repo.UserRepository;
import edu.goit.urlshortener.security.model.User;
import edu.goit.urlshortener.service.UrlService;
import edu.goit.urlshortener.util.Base62Encoder;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
                .destination(longUrl)
                .slug("") // Temporary value
                .clickCount(0L)
                .createdAt(LocalDateTime.now())
                .expiredTime(LocalDateTime.now().plusMonths(1))
                .user(authUser)
                .build();

        Url saved = urlRepository.save(url);
        String slugStr = Base62Encoder.encode(saved.getId());
        saved.setSlug(slugStr);
        return "https://localhost:9999/" + slugStr;
    }

    @Transactional
    public String getDestinationLink(String slug) {
        Url url = urlRepository.findBySlug(slug).orElseThrow(EntityNotFoundException::new);
        url.setClickCount(url.getClickCount() + 1L);
        return url.getDestination();
    }
}
