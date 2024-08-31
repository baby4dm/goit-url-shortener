package edu.goit.urlshortener.repo;

import edu.goit.urlshortener.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findBySlug(String slug);
}
