package edu.goit.urlshortener.service;

import edu.goit.urlshortener.model.responses.ShortLinkResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UrlService {
    String createShortLink(String longUrl);
    String getDestinationLink(String slug);
    ShortLinkResponse getShortLinkDto(String slug);
    Page<String> findAllActiveUrls(Pageable pageable);
    void deleteShortLink(String slug);

    Page<String> findAllLinks(int offset, int size);

    void extendExpirationDate(String url);
}
