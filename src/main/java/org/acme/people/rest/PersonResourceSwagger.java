package org.acme.people.rest;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


import org.acme.people.model.Person;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Path("/personswagger")
@ApplicationScoped
public class PersonResourceSwagger {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Person> getAll() {
        return Person.listAll();
    }

    // TODO: adding basic queries
 
    @Operation(summary = "Finds people born before a specific year", description = "Search the people database and return a list of people born before the specified year")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "The list of people born before the specified year", content = @Content(schema = @Schema(implementation = Person.class))),
            @APIResponse(responseCode = "500", description = "Something bad happened") })
    @GET
    @Path("/birth/before/{year}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Person> getBeforeYear(
            @Parameter(description = "Cutoff year for searching for people", required = true, name = "year") @PathParam(value = "year") int year) {

        return Person.getBeforeYear(year);
    }

 
    

}