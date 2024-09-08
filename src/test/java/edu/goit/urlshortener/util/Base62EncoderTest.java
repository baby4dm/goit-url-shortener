package edu.goit.urlshortener.util;

import org.junit.jupiter.api.Test;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

public class Base62EncoderTest {
    private static final int SHORT_URL_LENGTH = 7;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));

    @Test
    void testEncodeShortLength() {
        assertNotNull(Base62Encoder.encode(1L));
        assertEquals(SHORT_URL_LENGTH, Base62Encoder.encode(SHORT_URL_LENGTH).length());
    }

    @Test
    void testEncodeWithLargerId() {
        // Test encoding with a larger number
        long id = 123456789L;
        String encoded = Base62Encoder.encode(id);
        assertNotNull(encoded);
        assertEquals(SHORT_URL_LENGTH, encoded.length());
    }

    @Test
    void testEncodeWithPadding() {
        // Test with a smaller id and ensure padding is added
        long id = 1L;
        String encoded = Base62Encoder.encode(id);
        assertEquals(SHORT_URL_LENGTH, encoded.length());
    }

    @Test
    void testEncodeWithZero() {
        // Test the boundary condition where id = 0
        String encoded = Base62Encoder.encode(0L);
        assertEquals(SHORT_URL_LENGTH, encoded.length());
    }

    @Test
    void testEncodeUniqueForDifferentIds() {
        // Ensure encoding different IDs results in different URLs
        String encoded1 = Base62Encoder.encode(123L);
        String encoded2 = Base62Encoder.encode(456L);
        assertNotEquals(encoded1, encoded2);
    }
}
