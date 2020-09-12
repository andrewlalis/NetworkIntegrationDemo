package nl.andrewlalis.network_integration_demo.network.client;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Helper class that can generate lots of clients.
 */
public class ClientFactory {
	private final String ip;
	private final int port;

	public ClientFactory(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public Consumer buildConsumer() throws IOException {
		return new Consumer(this.ip, this.port, ThreadLocalRandom.current().nextInt(1, 20));
	}

	public Producer buildProducer() throws IOException {
		return new Producer(this.ip, this.port);
	}
}
