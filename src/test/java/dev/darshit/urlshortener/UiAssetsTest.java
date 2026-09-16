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
}
