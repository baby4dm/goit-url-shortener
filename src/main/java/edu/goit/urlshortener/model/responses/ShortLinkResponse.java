package edu.goit.urlshortener.model.responses;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
public class ShortLinkResponse {
    private String slug;
    private Long clickCount;
    private String destination;
    private LocalDateTime createdAt;
    private LocalDateTime expiredTime;
}
