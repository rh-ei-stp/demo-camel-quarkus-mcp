package com.redhat.consulting.integration.camel.quarkus.mcp;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/ai")
public class OurAiResource {

    @Inject OurAiService llmService;

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String ask(String prompt) {
        Log.infof("prompt=%s", prompt);
        String aiResponse = llmService.ask(prompt);
        Log.infof("aiReponse=%s", aiResponse);
        return aiResponse;
    }

}
