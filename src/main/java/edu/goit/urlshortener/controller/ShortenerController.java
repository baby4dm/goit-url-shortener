package edu.goit.urlshortener.controller;

import edu.goit.urlshortener.model.requests.LinkRequest;
import edu.goit.urlshortener.model.responses.ShortLinkResponse;
import edu.goit.urlshortener.security.model.UrlRequest;
import edu.goit.urlshortener.service.UrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/urls")
public class ShortenerController {

    private final UrlService urlService;

    @PostMapping("/create")
    public ResponseEntity<String> createShortLink(@RequestBody @Valid LinkRequest linkRequest) {
        String shortLink = urlService.createShortLink(linkRequest.getLongUrl());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shortLink);
    }

    @PostMapping
    public ResponseEntity<String> redirect(@RequestBody UrlRequest request) {
        String destinationLink = urlService.getDestinationLink(request.url());
        return new ResponseEntity<>(destinationLink, HttpStatus.OK);
    }

    @PostMapping("/info")
    public ResponseEntity<ShortLinkResponse> getShortLinkInfo(@RequestBody UrlRequest request) {
        ShortLinkResponse response = urlService.getShortLinkDto(request.url());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/findAllActive")
    public ResponseEntity<Page<String>> getAllActiveUrls(@RequestParam int offset, @RequestParam int size) {
        Pageable pageable = PageRequest.of(offset, size);
        Page<String> activeUrls = urlService.findAllActiveUrls(pageable);
        return ResponseEntity.ok(activeUrls);
    }

    @GetMapping("/findAll")
    public ResponseEntity<Page<String>> getAllUrls(@RequestParam int offset, @RequestParam int size) {
        Page<String> allUrls = urlService.findAllLinks(offset, size);
        return ResponseEntity.status(HttpStatus.OK)
                .body(allUrls);
    }

    @PutMapping()
    public ResponseEntity<Void> extendExpirationDate(@RequestBody UrlRequest request) {
        urlService.extendExpirationDate(request.url());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping()
    public ResponseEntity<Void> deleteShortLink(@RequestBody UrlRequest request) {
        urlService.deleteShortLink(request.url());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}