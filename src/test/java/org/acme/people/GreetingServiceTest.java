package org.acme.people;

import javax.inject.Inject;

import org.acme.people.service.GreetingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class GreetingServiceTest {


    @Inject 
    GreetingService service;

    @Test
    public void testGreetingService() {
        Assertions.assertTrue(service.greeting("Quarkus").startsWith("hello Quarkus"));
    }
}