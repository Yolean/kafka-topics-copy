package se.yolean.kafka.topicscopy.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import se.yolean.kafka.topicscopy.Readiness;

@Path("/healthz")
public class Healthz {

  @Inject
  Readiness readiness;

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public Response get() {
    if (readiness.isApplicationReady()) {
      return Response.ok("ready\n").build();
    } else {
      return Response.status(500).entity("unready\n").build();
    }
  }

}
