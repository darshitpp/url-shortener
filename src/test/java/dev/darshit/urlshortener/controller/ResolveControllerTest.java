package dev.darshit.urlshortener.controller;

import dev.darshit.urlshortener.configuration.LettuceTestConfiguration;
import dev.darshit.urlshortener.redis.RedisUrlOperations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
@Import(LettuceTestConfiguration.class)
class ResolveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisUrlOperations redisUrlOperations;

    private static final String testKey = "testKey";
    private static final String testIncorrectKey = "testIncorrectKey";
    private static final String originalUrl = "https://google.com";

    @BeforeEach
    void populateRedis() {
        redisUrlOperations.putIfAbsent(testKey, originalUrl, 1);
    }

    @AfterEach
    void flushRedis() {
        redisUrlOperations.flushAll();
    }

    @Test
    @DisplayName("Resolves URL successfully")
    void should_resolve_url() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/{shortPath}", testKey))
                .andExpect(MockMvcResultMatchers.redirectedUrl(originalUrl))
                .andExpect(MockMvcResultMatchers.status().isFound());
    }

    @Test
    @DisplayName("Returns 404 on invalid resolution")
    void return_404_invalid_resolution() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/{shortPath}", testIncorrectKey))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }


}