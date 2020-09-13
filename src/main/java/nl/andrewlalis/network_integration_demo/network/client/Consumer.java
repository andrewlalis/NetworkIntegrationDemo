package nl.andrewlalis.network_integration_demo.network.client;

import lombok.extern.slf4j.Slf4j;
import nl.andrewlalis.network_integration_demo.network.Message;
import nl.andrewlalis.network_integration_demo.network.MessageType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The consumer will wait for and receive numbers, and store them.
 */
@Slf4j
public class Consumer extends Client {
	private List<Integer> numbers;
	private final int preferredMultiple;

	public Consumer(String ip, int port, int preferredMultiple) throws IOException {
		super(ip, port);
		this.numbers = new ArrayList<>();
		this.preferredMultiple = preferredMultiple;
	}

	@Override
	public void beforeStart() {
		try {
			this.send(new Message(MessageType.CLIENT_IDENT, ClientType.CONSUMER.name()));
			log.info("Sent client ident message to delegator.");
			this.send(new Message(MessageType.NUMBER, Integer.toString(this.preferredMultiple)));
			log.info("Sent preferred multiple.");
		} catch (IOException e) {
			log.error("Could not send client identification message to delegator.");
			throw new RuntimeException("Cannot start client.");
		}
	}

	@Override
	public void step() {
		try {
			Message message = (Message) this.objectInputStream.readObject();
			if (message.getType() == MessageType.DISCONNECT) {
				this.shutdown();
			} else if (message.getType() == MessageType.NUMBER) {
				int number = Integer.parseInt(message.getContent());
				this.numbers.add(number);
				log.info("Received number {}, bringing total received numbers to {}.", number, this.numbers.size());
			}
		} catch (IOException | ClassNotFoundException e) {
			this.shutdown();
		}
	}

	public int getPreferredMultiple() {
		return this.preferredMultiple;
	}

	public Integer[] getNumbersConsumed() {
		return this.numbers.toArray(new Integer[0]);
	}
}
