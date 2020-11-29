package dev.darshit.urlshortener;

import dev.darshit.urlshortener.controller.Resolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SmokeTest {

    @Autowired
    private Resolver controller;

    @Test
    public void contextLoads() throws Exception {
        Assertions.assertNotNull(controller);
    }
}