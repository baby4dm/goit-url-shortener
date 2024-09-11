package edu.goit.urlshortener.controller;

import edu.goit.urlshortener.model.requests.LinkRequest;
import edu.goit.urlshortener.model.responses.ShortLinkResponse;
import edu.goit.urlshortener.security.model.UrlRequest;
import edu.goit.urlshortener.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/urls")
public class ShortenerController {

    private final UrlService urlService;

    @Operation(summary = "Create a short link", description = "Generates a shortened URL for a given long URL.")
    @ApiResponse(responseCode = "201", description = "Short link created successfully")
    @PostMapping("/create")
    public ResponseEntity<String> createShortLink(
            @Parameter(description = "Request object containing the long URL") @RequestBody @Valid LinkRequest linkRequest) {
        String shortLink = urlService.createShortLink(linkRequest.getLongUrl());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shortLink);
    }

    @Operation(summary = "Redirect to original URL", description = "Redirects to the original URL for the given short link.")
    @ApiResponse(responseCode = "200", description = "Redirect successful")
    @GetMapping
    public ResponseEntity<String> redirect(
            @Parameter(description = "The short URL")String request) {
        String destinationLink = urlService.getDestinationLink(request);
        return new ResponseEntity<>(destinationLink, HttpStatus.OK);
    }

    @Operation(summary = "Get short link info", description = "Returns information about the given short link, such as creation date and expiration date.")
    @ApiResponse(responseCode = "200", description = "Short link information retrieved successfully")
    @PostMapping("/info")
    public ResponseEntity<ShortLinkResponse> getShortLinkInfo(
            @Parameter(description = "The short URL") String request) {
        ShortLinkResponse response = urlService.getShortLinkDto(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all active URLs", description = "Fetches a paginated list of all active short links.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of active URLs retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    @GetMapping("/findAllActive")
    public ResponseEntity<Page<String>> getAllActiveUrls(
            @Parameter(description = "Pagination offset") @RequestParam int offset,
            @Parameter(description = "Pagination size") @RequestParam int size) {
        Pageable pageable = PageRequest.of(offset, size);
        Page<String> activeUrls = urlService.findAllActiveUrls(pageable);
        return ResponseEntity.ok(activeUrls);
    }

    @Operation(summary = "Get all URLs", description = "Fetches a paginated list of all short links.")
    @ApiResponse(responseCode = "200", description = "List of all URLs retrieved successfully")
    @GetMapping("/findAll")
    public ResponseEntity<Page<String>> getAllUrls(
            @Parameter(description = "Pagination offset") @RequestParam int offset,
            @Parameter(description = "Pagination size") @RequestParam int size) {
        Page<String> allUrls = urlService.findAllLinks(offset, size);
        return ResponseEntity.status(HttpStatus.OK).body(allUrls);
    }

    @Operation(summary = "Extend expiration date", description = "Extends the expiration date of the given short link.")
    @ApiResponse(responseCode = "200", description = "Expiration date extended successfully")
    @PutMapping
    public ResponseEntity<Void> extendExpirationDate(
            @Parameter(description = "The short URL")String request) {
        urlService.extendExpirationDate(request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "Delete short link", description = "Deletes the given short link.")
    @ApiResponse(responseCode = "204", description = "Short link deleted successfully")
    @DeleteMapping
    public ResponseEntity<Void> deleteShortLink(
            @Parameter(description = "The short URL")String request) {
        urlService.deleteShortLink(request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
