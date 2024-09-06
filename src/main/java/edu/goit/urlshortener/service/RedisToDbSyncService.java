package edu.goit.urlshortener.service;

import edu.goit.urlshortener.model.Url;
import edu.goit.urlshortener.repo.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisToDbSyncService {
    private final RedisTemplate<String, Url> redisTemplate;
    private final UrlRepository urlRepository;

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void synchronize() {
        Set<String> cacheKeys = redisTemplate.keys("urlCache::*");

        if (cacheKeys != null && !cacheKeys.isEmpty()) {
            List<String> urlKeys = cacheKeys.stream()
                    .filter(key -> !key.endsWith("::clickCount"))
                    .toList();

            List<String> shortLinks = urlKeys.stream()
                    .map(key -> key.replace("urlCache::", ""))
                    .toList();

            List<Url> urlsToUpdate = urlRepository.findByShortLinkIn(shortLinks);

            for (Url dbUrl : urlsToUpdate) {
                String cacheKey = "urlCache::" + dbUrl.getShortLink();
                Url cachedUrl = redisTemplate.opsForValue().get(cacheKey);

                if (cachedUrl != null && !cachedUrl.getClickCount().equals(dbUrl.getClickCount())) {
                    dbUrl.setClickCount(cachedUrl.getClickCount());
                }
            }

            if (!urlsToUpdate.isEmpty()) {
                urlRepository.saveAll(urlsToUpdate);
            }
        }
    }
}
