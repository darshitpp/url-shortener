package dev.darshit.urlshortener.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "CORS_ENABLED=https://allowed.example")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(LettuceTestConfiguration.class)
class SecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rejectsBlankCredentialsAndWildcardOrigins() {
        assertThrows(IllegalStateException.class,
                () -> new SecurityConfiguration("", "secret", new String[]{"https://allowed.example"}));
        assertThrows(IllegalStateException.class,
                () -> new SecurityConfiguration("user", " ", new String[]{"https://allowed.example"}));
        assertThrows(IllegalStateException.class,
                () -> new SecurityConfiguration("user", "secret", new String[]{"*"}));
        assertThrows(IllegalStateException.class,
                () -> new SecurityConfiguration("user", "secret", new String[]{" "}));
    }

    @Test
    void allowsOnlyConfiguredCorsOriginAndContentType() throws Exception {
        mockMvc.perform(options("/shorten")
                        .header(HttpHeaders.ORIGIN, "https://allowed.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "https://allowed.example"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                        containsStringIgnoringCase("Content-Type")));
    }

    @Test
    void rejectsUnconfiguredCorsOrigin() throws Exception {
        mockMvc.perform(options("/shorten")
                        .header(HttpHeaders.ORIGIN, "https://blocked.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden());
    }
}
