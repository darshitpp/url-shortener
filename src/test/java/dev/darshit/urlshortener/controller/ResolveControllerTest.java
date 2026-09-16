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
    void rendersDestinationWarningWithoutRedirecting() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/{shortPath}", testKey))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().doesNotExist("Location"))
                .andExpect(MockMvcResultMatchers.content().string(
                        org.hamcrest.Matchers.containsString("You are leaving just.darshit.dev")))
                .andExpect(MockMvcResultMatchers.content().string(
                        org.hamcrest.Matchers.containsString(originalUrl)));
    }

    @Test
    void continuesToTheStoredDestination() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/resolve/{shortPath}", testKey))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(MockMvcResultMatchers.redirectedUrl(originalUrl));
    }

    @Test
    void rendersSchemeAndAsciiHostForUnicodeDestination() throws Exception {
        redisUrlOperations.putIfAbsent(
                "unicode-destination", "https://bücher.example/path", 1);
        mockMvc.perform(MockMvcRequestBuilders.get("/{shortPath}", "unicode-destination"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(
                        org.hamcrest.Matchers.containsString("Destination scheme")))
                .andExpect(MockMvcResultMatchers.content().string(
                        org.hamcrest.Matchers.containsString("xn--bcher-kva.example")));
    }

    @Test
    void preservesDestinationPortPathQueryAndFragment() throws Exception {
        redisUrlOperations.putIfAbsent(
                "complete-destination",
                "https://bücher.example:8443/path?x=1#fragment",
                1);
        mockMvc.perform(MockMvcRequestBuilders.get(
                        "/{shortPath}", "complete-destination"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute(
                        "destination",
                        "https://xn--bcher-kva.example:8443/path?x=1#fragment"));
    }

    @Test
    void preservesIpv6Destination() throws Exception {
        redisUrlOperations.putIfAbsent(
                "ipv6-destination", "http://[::1]:8080/path?x=1#fragment", 1);
        mockMvc.perform(MockMvcRequestBuilders.get("/{shortPath}", "ipv6-destination"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.model().attribute(
                        "destination", "http://[::1]:8080/path?x=1#fragment"));
    }

    @Test
    void rejectsDestinationUserInfo() throws Exception {
        redisUrlOperations.putIfAbsent(
                "userinfo-destination",
                "https://user:secret@example.com/path",
                1);
        mockMvc.perform(MockMvcRequestBuilders.get("/{shortPath}", "userinfo-destination"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void continuationIgnoresCallerSuppliedDestination() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/resolve/{shortPath}", testKey)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://attacker.example\"}"))
                .andExpect(MockMvcResultMatchers.status().isFound())
                .andExpect(MockMvcResultMatchers.redirectedUrl(originalUrl));
    }

    @Test
    @DisplayName("Returns 404 on invalid resolution")
    void return_404_invalid_resolution() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/{shortPath}", testIncorrectKey))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }


}
