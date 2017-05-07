package edu.aubg.server;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class MyServer {

	public static void main(String[] args) throws Exception {

		URI baseUri = UriBuilder.fromUri("http://localhost/").port(9093).build();

		ResourceConfig config = new ResourceConfig();
		config.packages("edu.aubg.services");

		JettyHttpContainerFactory.createServer(baseUri, config);
	}
}
