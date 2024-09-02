package edu.goit.urlshortener.repo;

import edu.goit.urlshortener.model.Url;
import edu.goit.urlshortener.security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByShortLink(String slug);

    @Query("SELECT u.shortLink FROM Url u WHERE u.user = :user AND u.expiredTime > CURRENT_TIMESTAMP")
    Optional<List<String>> findAllActiveSlugsByUserId(@Param("user") User user);
}
