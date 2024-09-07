package edu.goit.urlshortener.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class LinkRequest {
    @NotBlank(message = "Must not be blank")
    @Size(max = 2048, message = "URL is too long")
    @URL(message = "Invalid URL format")
    private String longUrl;
}
