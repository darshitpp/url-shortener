package dev.darshit.urlshortener;

import dev.darshit.urlshortener.controller.ResolveController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SmokeTest {

    @Autowired
    private ResolveController controller;

    @Test
    public void contextLoads() throws Exception {
        Assertions.assertNotNull(controller);
    }
}