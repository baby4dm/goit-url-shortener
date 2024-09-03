package edu.goit.urlshortener.controller;

import edu.goit.urlshortener.model.dto.LinkRequest;
import edu.goit.urlshortener.model.dto.ShortLinkResponse;
import edu.goit.urlshortener.service.impl.UrlServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/urls")
@RequiredArgsConstructor
public class ShortenerController {

    private final UrlServiceImpl urlService;

    @PostMapping
    public ResponseEntity<String> createShortLink(@RequestBody @Valid LinkRequest linkRequest) {
        String shortLink = urlService.createShortLink(linkRequest.getLongUrl());
        return ResponseEntity.ok(shortLink);
    }

    @GetMapping("/{url}")
    public ResponseEntity<String> redirect(@PathVariable String url) {
        String destinationLink = urlService.getDestinationLink(url);
        return ResponseEntity.status(302).header("Location", destinationLink).build();
    }

    @GetMapping("/info/{url}")
    public ResponseEntity<ShortLinkResponse> getShortLinkInfo(@PathVariable String url) {
        ShortLinkResponse response = urlService.getShortLinkDto(url);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<String>> getAllActiveUrls(Pageable pageable) {
        Page<String> activeUrls = urlService.findAllActiveUrls(pageable);
        return ResponseEntity.ok(activeUrls);
    }

    @DeleteMapping("/{url}")
    public ResponseEntity<Void> deleteShortLink(@PathVariable String url) {
        urlService.deleteShortLink(url);
        return ResponseEntity.noContent().build();
    }
}
