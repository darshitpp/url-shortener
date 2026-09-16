package dev.darshit.urlshortener;

import dev.darshit.urlshortener.configuration.LettuceTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(LettuceTestConfiguration.class)
class UiAssetsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void uiPageIsPublicAndContainsShorteningForm() throws Exception {
        mockMvc.perform(get("/ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("id=\"shorten-form\"")))
                .andExpect(content().string(containsString("id=\"original-url\"")))
                .andExpect(content().string(containsString("id=\"custom-path\"")))
                .andExpect(content().string(containsString("maxlength=\"2048\"")))
                .andExpect(content().string(containsString("maxlength=\"64\"")));
    }

    @Test
    void stylesheetIsPublic() throws Exception {
        mockMvc.perform(get("/ui/styles.css"))
                .andExpect(status().isOk());
    }

    @Test
    void appJsIsPublicAndContainsCanonicalBehavior() throws Exception {
        mockMvc.perform(get("/ui/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("fetch(\"/shorten\", {")))

                .andExpect(content().string(containsString("payload.strategy = \"custom\"")))
                .andExpect(content().string(containsString("response.status === 400")))
                .andExpect(content().string(containsString("response.status === 413")))
                .andExpect(content().string(containsString("response.status === 429")))
                .andExpect(content().string(containsString(
                        "response.headers.get(\"Retry-After\")")))
                .andExpect(content().string(containsString(
                        "status.textContent = message")))
                .andExpect(content().string(containsString(
                        "shortUrlLink.textContent = shortUrl")))
                .andExpect(content().string(not(containsString("innerHTML"))))
                .andExpect(content().string(containsString("new AbortController()")))
                .andExpect(content().string(containsString(
                        "signal: controller.signal")));
    }
}
