package com.example.gids.client;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.rest.client.inject.RestClient;



import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/client")
@ApplicationScoped
public class ClientController {

    @Inject
    @RestClient
    private Service service;

    @Inject Tracer tracer;



    @Inject @ConfigProperty(name="conference") String conf;

    @GET
    @Path("/test/{parameter}")
    @Timeout(100)
    @Asynchronous
    @Retry
    @Fallback(
        fallbackMethod = "myfallback"
    )
    @APIResponses(
        value = {
            @APIResponse(
                responseCode = "404",
                description = "Backend failure",
                content = @Content(mediaType = "text/plain")),
            @APIResponse(
                responseCode = "200",
                description = "Got response from backend",
                content = @Content(mediaType = "text/plain")) })
    @Operation(summary= "Calling doSomething()",
                description="Calling the backend and post back the response")

    public CompletionStage<String> onClientSide(@PathParam("parameter") String parameter) { 
        
        Span span = tracer.spanBuilder("my-span").startSpan();
        try (Scope scope = span.makeCurrent()) {
            CompletionStage<String> result =  CompletableFuture.completedStage("Hello " + conf + "! " + service.doSomething(parameter));
            span.addEvent("monday-event");
            span.end();
            return result;
        } 
     
     }

    public CompletionStage<String> myfallback(@PathParam("parameter") String parameter) {   
        return CompletableFuture.completedStage("Hello " + conf + "! This is my fallback!");
    }
    
}
