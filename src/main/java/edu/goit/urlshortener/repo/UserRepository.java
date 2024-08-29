package edu.goit.urlshortener.repo;

import edu.goit.urlshortener.security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
