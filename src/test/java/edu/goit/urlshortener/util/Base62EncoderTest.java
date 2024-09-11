package edu.goit.urlshortener.util;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;


class Base62EncoderTest {
    private static final int SHORT_URL_LENGTH = 7;

    @Test
    void testEncodeShortLength() {
        assertNotNull(Base62Encoder.encode(1L));
        assertEquals(SHORT_URL_LENGTH, Base62Encoder.encode(SHORT_URL_LENGTH).length());
    }

    @Test
    void testEncodeWithLargerId() {
        long id = 123456789L;
        String encoded = Base62Encoder.encode(id);
        assertNotNull(encoded);
        assertEquals(SHORT_URL_LENGTH, encoded.length());
    }

    @Test
    void testEncodeWithPadding() {
        long id = 1L;
        String encoded = Base62Encoder.encode(id);
        assertEquals(SHORT_URL_LENGTH, encoded.length());
    }

    @Test
    void testEncodeWithZero() {
        String encoded = Base62Encoder.encode(0L);
        assertEquals(SHORT_URL_LENGTH, encoded.length());
    }

    @Test
    void testEncodeUniqueForDifferentIds() {
        String encoded1 = Base62Encoder.encode(123L);
        String encoded2 = Base62Encoder.encode(456L);
        assertNotEquals(encoded1, encoded2);
    }
}
