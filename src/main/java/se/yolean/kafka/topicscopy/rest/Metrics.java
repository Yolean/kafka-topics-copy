package se.yolean.kafka.topicscopy.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

@Path("/metrics")
public class Metrics {

	private CollectorRegistry registry;
	private Set<String> names;

	public Metrics() {
		this.registry = CollectorRegistry.defaultRegistry;
		this.names = new HashSet<String>(0);
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response get() {
		return Response.ok(new StreamingOutput() {
			@Override
			public void write(OutputStream out) throws IOException, WebApplicationException {
				OutputStreamWriter osw = new OutputStreamWriter(out, Charset.defaultCharset());
				TextFormat.write004(osw, registry.filteredMetricFamilySamples(names));
				osw.append('\n');
				osw.flush();
				out.close();
			}
		}).build();
	}

}
