package org.acme.people.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import javax.inject.Inject;
import org.acme.people.service.GreetingService;
import javax.ws.rs.PathParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/helloerror")
public class GreetingResourceError {

    public static final Logger log = LoggerFactory.getLogger(GreetingResourceError.class);

    @Inject
    GreetingService service;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/greeting/{name}")
    public String greeting(@PathParam("name") String name) {
        return service.greeting(name);
    }


    @GET
    @Path("/lastletter/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String lastLetter(@PathParam("name") String name) {
        int len = name.length();
        //String lastLetter = name.substring(len);
        String lastLetter = name.substring(len - 1);
        log.info("Got last letter: " + lastLetter);
        return lastLetter;
    }
}