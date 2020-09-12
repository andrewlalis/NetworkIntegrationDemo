package nl.andrewlalis.network_integration_demo.network.client;

import lombok.extern.slf4j.Slf4j;
import nl.andrewlalis.network_integration_demo.network.Message;
import nl.andrewlalis.network_integration_demo.network.MessageType;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Producer, which will occasionally send a random natural number to the
 * delegator server.
 */
@Slf4j
public class Producer extends Client {
	private static final int MIN_DELAY = 1000;
	private static final int MAX_DELAY = 10000;

	public Producer(String ip, int port) throws IOException {
		super(ip, port);
	}

	@Override
	public void beforeStart() {
		try {
			this.send(new Message(MessageType.CLIENT_IDENT, ClientType.PRODUCER.name()));
			log.info("Sent client ident message to delegator.");
		} catch (IOException e) {
			log.error("Could not send client identification message to delegator.");
			throw new RuntimeException("Cannot start client.");
		}
	}

	@Override
	public void step() {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int number = random.nextInt(1, Integer.MAX_VALUE);
		int delay = random.nextInt(MIN_DELAY, MAX_DELAY);
		try {
			this.send(new Message(MessageType.NUMBER, Integer.toString(number)));
			log.info("Produced number {} and sent it to delegator.", number);
			Thread.sleep(delay);
		} catch (IOException e) {
			log.error("IOException occurred while sending message to delegator.");
		} catch (InterruptedException e) {
			log.warn("Interrupted while waiting on random delay before sending another number.");
		}
	}
}
