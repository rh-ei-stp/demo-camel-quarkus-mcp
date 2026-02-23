package com.redhat.consulting.integration.camel.quarkus.mcp;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/countEs")
public class CountEsResource {

    @Inject AiLetterCounterService aiLetterCounterService;

    @GET
    @Path("/{word}")
    @Produces(MediaType.TEXT_PLAIN)
    public String ask(@PathParam("word") String word) {
        Log.infof("Counting 'e's in %s", word);
        String result = aiLetterCounterService.countEs(word);
        Log.infof("Result=%s", result);
        return result;
    }
}
