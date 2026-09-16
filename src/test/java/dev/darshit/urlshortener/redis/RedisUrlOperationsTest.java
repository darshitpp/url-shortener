package dev.darshit.urlshortener.redis;

import dev.darshit.urlshortener.configuration.LettuceTestConfiguration;
import dev.darshit.urlshortener.configuration.RedisSerializationBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(LettuceTestConfiguration.class)
class RedisUrlOperationsTest {

    @Autowired
    private RedisUrlOperations operations;

    @Autowired
    private LettuceConnectionFactory connectionFactory;

    private RedisTemplate<String, String> raw;

    @BeforeEach
    void setUp() {
        raw = RedisSerializationBuilder.getRedisTemplate(connectionFactory, String.class);
        raw.afterPropertiesSet();
        raw.getConnectionFactory().getConnection().flushAll();
    }

    @AfterEach
    void tearDown() {
        raw.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    void writesOnlyNamespacedUrlKeys() {
        Assertions.assertEquals(Boolean.TRUE,
                operations.putIfAbsent("alias", "https://example.com", 1).orElse(null));
        Assertions.assertEquals("https://example.com", raw.opsForValue().get("url:alias"));
        Assertions.assertFalse(raw.hasKey("alias"));
    }

    @Test
    void migratesLegacyUrlWithoutOverwritingNamespacedValue() {
        raw.opsForValue().set("legacy", "https://legacy.example");
        raw.opsForValue().set("url:legacy", "https://new.example");
        Assertions.assertEquals("https://new.example", operations.get("legacy").orElse(null));
        Assertions.assertEquals("https://new.example", raw.opsForValue().get("url:legacy"));
    }

    @Test
    void neverResolvesLegacyMetadataAsShortLinks() {
        raw.opsForValue().set("DEFAULT_DOMAIN", "https://example.com");
        raw.opsForValue().set("SHORT_LINK_COUNTER", "41");
        Assertions.assertTrue(operations.get("DEFAULT_DOMAIN").isEmpty());
        Assertions.assertTrue(operations.get("SHORT_LINK_COUNTER").isEmpty());
    }

    @Test
    void continuesTheLegacyCounter() {
        raw.opsForValue().set("SHORT_LINK_COUNTER", "41");
        Assertions.assertEquals(42L, operations.incrementCounterForShortUrl());
        Assertions.assertEquals(43L, operations.incrementCounterForShortUrl());
        Assertions.assertEquals("43", raw.opsForValue().get("counter:short-link"));
    }

    @Test
    void migratesLegacyDefaultDomainAndDeletesBothForms() {
        raw.opsForValue().set("DEFAULT_DOMAIN", "https://legacy.example");
        Assertions.assertEquals("https://legacy.example",
                operations.getDefaultDomain().orElse(null));
        Assertions.assertEquals("https://legacy.example",
                raw.opsForValue().get("config:default-domain"));

        operations.deleteDefaultDomain();
        Assertions.assertFalse(raw.hasKey("DEFAULT_DOMAIN"));
        Assertions.assertFalse(raw.hasKey("config:default-domain"));
    }

    @Test
    void revokesBothLegacyAndNamespacedAliases() {
        raw.opsForValue().set("alias", "https://legacy.example");
        raw.opsForValue().set("url:alias", "https://new.example");
        operations.delete("alias");
        Assertions.assertFalse(raw.hasKey("alias"));
        Assertions.assertFalse(raw.hasKey("url:alias"));
    }
}
