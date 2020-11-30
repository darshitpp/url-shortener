package dev.darshit.urlshortener.controller;

import dev.darshit.urlshortener.domain.ShortenResponse;
import dev.darshit.urlshortener.redis.RedisUrlOperations;
import dev.darshit.urlshortener.strategy.StrategyFactory;
import dev.darshit.urlshortener.utils.JsonUtils;
import dev.darshit.urlshortener.utils.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
class ShortenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisUrlOperations redisUrlOperations;

    @Autowired
    private StrategyFactory strategyFactory;

    @Test
    @DisplayName("Shorten URL with Default Input")
    void shorten_url_with_default_input() throws Exception {

        String json = "{\n" +
                "    \"url\": \"http://google.com\"\n" +
                "}";

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.request(HttpMethod.POST, "/shorten")
                .accept(MediaType.APPLICATION_JSON)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ShortenResponse response = JsonUtils.value(mvcResult.getResponse().getContentAsString(), ShortenResponse.class);
        Assertions.assertFalse(StringUtils.isEmpty(response.getShortUrl()));
        Assertions.assertEquals(7, response.getTtlInDays());

    }

    @Test
    @DisplayName("Shorten URL with Hash Strategy")
    void shorten_url_with_hash_strategy() throws Exception {

        String json = "{\n" +
                "    \"url\": \"https://google.com\",\n" +
                "    \"strategy\": \"hash\"\n" +
                "}";

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.request(HttpMethod.POST, "/shorten")
                .accept(MediaType.APPLICATION_JSON)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ShortenResponse response = JsonUtils.value(mvcResult.getResponse().getContentAsString(), ShortenResponse.class);
        Assertions.assertEquals(7, response.getTtlInDays());
        Assertions.assertEquals(8, response.getShortUrl().length());
    }

    @Test
    @DisplayName("Shorten Invalid URL")
    void shorten_invalid_url() throws Exception {

        String json = "{\n" +
                "    \"url\": \"test Invalid URL\"\n" +
                "}";

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.request(HttpMethod.POST, "/shorten")
                .accept(MediaType.APPLICATION_JSON)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        ShortenResponse response = JsonUtils.value(mvcResult.getResponse().getContentAsString(), ShortenResponse.class);
        Assertions.assertEquals("Please pass a valid URL", response.getError());
        Assertions.assertNull(response.getTtlInDays());
        Assertions.assertNull(response.getShortUrl());
    }

    @Test
    @DisplayName("Invalid TTL greater than 30")
    void invalid_url_ttl_greater_than_30() throws Exception {

        String json = "{\n" +
                "    \"url\": \"https://google.com\",\n" +
                "    \"strategy\": \"hash\",\n" +
                "    \"options\": {\n" +
                "        \"ttl\": 33\n" +
                "    }\n" +
                "}";

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.request(HttpMethod.POST, "/shorten")
                .accept(MediaType.APPLICATION_JSON)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        ShortenResponse response = JsonUtils.value(mvcResult.getResponse().getContentAsString(), ShortenResponse.class);
        Assertions.assertEquals("TTL must be between 1 and 30 days", response.getError());
        Assertions.assertNull(response.getTtlInDays());
        Assertions.assertNull(response.getShortUrl());
    }

    @Test
    @DisplayName("Invalid TTL less than 1")
    void invalid_url_ttl_less_than_1() throws Exception {

        String json = "{\n" +
                "    \"url\": \"https://google.com\",\n" +
                "    \"strategy\": \"hash\",\n" +
                "    \"options\": {\n" +
                "        \"ttl\": 0\n" +
                "    }\n" +
                "}";

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.request(HttpMethod.POST, "/shorten")
                .accept(MediaType.APPLICATION_JSON)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        ShortenResponse response = JsonUtils.value(mvcResult.getResponse().getContentAsString(), ShortenResponse.class);
        Assertions.assertEquals("TTL must be between 1 and 30 days", response.getError());
        Assertions.assertNull(response.getTtlInDays());
        Assertions.assertNull(response.getShortUrl());
    }

    @Test
    @DisplayName("Invalid URL Path Size greater than 18")
    void invalid_url_path_size_greater_than_18() throws Exception {

        String json = "{\n" +
                "    \"url\": \"https://google.com\",\n" +
                "    \"strategy\": \"hash\",\n" +
                "    \"options\": {\n" +
                "        \"urlSize\": 20\n" +
                "    }\n" +
                "}";

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.request(HttpMethod.POST, "/shorten")
                .accept(MediaType.APPLICATION_JSON)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        ShortenResponse response = JsonUtils.value(mvcResult.getResponse().getContentAsString(), ShortenResponse.class);
        Assertions.assertEquals("Size must be between 5 and 18 characters", response.getError());
        Assertions.assertNull(response.getTtlInDays());
        Assertions.assertNull(response.getShortUrl());
    }

    @Test
    @DisplayName("Invalid URL Path Size less than 5")
    void invalid_url_path_size_less_than_5() throws Exception {

        String json = "{\n" +
                "    \"url\": \"https://google.com\",\n" +
                "    \"strategy\": \"hash\",\n" +
                "    \"options\": {\n" +
                "        \"urlSize\": 4\n" +
                "    }\n" +
                "}";

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.request(HttpMethod.POST, "/shorten")
                .accept(MediaType.APPLICATION_JSON)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        ShortenResponse response = JsonUtils.value(mvcResult.getResponse().getContentAsString(), ShortenResponse.class);
        Assertions.assertEquals("Size must be between 5 and 18 characters", response.getError());
        Assertions.assertNull(response.getTtlInDays());
        Assertions.assertNull(response.getShortUrl());
    }

    @Test
    @DisplayName("Empty Custom Path")
    void empty_custom_path() throws Exception {

        String json = "{\n" +
                "    \"url\": \"https://google.com\",\n" +
                "    \"options\": {\n" +
                "        \"customPath\": \"\" \n" +
                "    }\n" +
                "}";

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.request(HttpMethod.POST, "/shorten")
                .accept(MediaType.APPLICATION_JSON)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        ShortenResponse response = JsonUtils.value(mvcResult.getResponse().getContentAsString(), ShortenResponse.class);
        Assertions.assertEquals("CustomPath cannot be empty", response.getError());
        Assertions.assertNull(response.getTtlInDays());
        Assertions.assertNull(response.getShortUrl());
    }

    @Test
    @DisplayName("Invalid Custom Path")
    void invalid_custom_path() throws Exception {

        String json = "{\n" +
                "    \"url\": \"https://google.com\",\n" +
                "    \"options\": {\n" +
                "        \"customPath\": \"asdfasdf*%\" \n" +
                "    }\n" +
                "}";

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.request(HttpMethod.POST, "/shorten")
                .accept(MediaType.APPLICATION_JSON)
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        ShortenResponse response = JsonUtils.value(mvcResult.getResponse().getContentAsString(), ShortenResponse.class);
        Assertions.assertEquals("CustomPath cannot contain special characters except - and _", response.getError());
        Assertions.assertNull(response.getTtlInDays());
        Assertions.assertNull(response.getShortUrl());
    }

    @AfterEach
    void flushRedis() {
        redisUrlOperations.flushAll();
    }

}