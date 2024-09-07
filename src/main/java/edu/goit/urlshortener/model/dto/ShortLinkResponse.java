package edu.goit.urlshortener.model.dto;



import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@ToString
@Builder
public class ShortLinkResponse {
    private String slug;
    private Long clickCount;
    private String destination;
    private LocalDateTime createdAt;
    private LocalDateTime expiredTime;
}
