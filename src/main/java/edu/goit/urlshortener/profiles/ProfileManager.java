package edu.goit.urlshortener.profiles;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProfileManager {
    @Autowired
    private Environment environment;
    @PostConstruct
    public void getActiveProfiles() {
        for (String profileName : environment.getActiveProfiles()) {
            log.info("***** Currently active profile ***** - " + profileName);
        }
    }
}

