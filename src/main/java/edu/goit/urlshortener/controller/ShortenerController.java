package edu.goit.urlshortener.controller;

import edu.goit.urlshortener.model.requests.LinkRequest;
import edu.goit.urlshortener.model.responses.ShortLinkResponse;
import edu.goit.urlshortener.service.UrlService;
import edu.goit.urlshortener.service.impl.UrlServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/{url}")
    public ResponseEntity<Void> redirect(@PathVariable String url) {
        String destinationLink = urlService.getDestinationLink(url);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", destinationLink)
                .build();
    }

    @GetMapping("/info/{url}")
    public ResponseEntity<ShortLinkResponse> getShortLinkInfo(@PathVariable String url) {
        ShortLinkResponse response = urlService.getShortLinkDto(url);
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

    @PutMapping("/extend/{url}")
    public ResponseEntity<Void> extendExpirationDate(@PathVariable String url) {
        urlService.extendExpirationDate(url);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{url}")
    public ResponseEntity<Void> deleteShortLink(@PathVariable String url) {
        urlService.deleteShortLink(url);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}