package nl.andrewlalis.network_integration_demo;

import nl.andrewlalis.network_integration_demo.network.client.ClientFactory;
import nl.andrewlalis.network_integration_demo.network.server.DelegatorServer;

import java.io.IOException;

/**
 * Main application entry point.
 */
public class NetworkIntegrationDemo {
	public static final int PORT = 56712;
	public static final String IP = "127.0.0.1";

	public static void main(String[] args) throws IOException {
		DelegatorServer delegator = new DelegatorServer(PORT);
		delegator.start();

		ClientFactory factory = new ClientFactory(IP, PORT);
		for (int i = 0; i < 10; i++) {
			factory.buildConsumer().start();
		}
		for (int i = 0; i < 100; i++) {
			factory.buildProducer().start();
		}
	}
}
