package nl.andrewlalis.network_integration_demo;

import lombok.extern.slf4j.Slf4j;
import nl.andrewlalis.network_integration_demo.network.client.ClientFactory;
import nl.andrewlalis.network_integration_demo.network.client.Consumer;
import nl.andrewlalis.network_integration_demo.network.client.Producer;
import nl.andrewlalis.network_integration_demo.network.server.DelegatorServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to ensure that the application works as a whole.
 */
@Slf4j
public class NetworkIntegrationDemoTest {

	@Test
	public void testApplication() throws IOException, InterruptedException {
		// Start by setting up the delegator server.
		DelegatorServer delegator = new DelegatorServer(NetworkIntegrationDemo.PORT);
		assertFalse(delegator.isRunning());
		delegator.start();
		// Wait a little bit to ensure that the thread has started.
		Thread.sleep(100);
		assertTrue(delegator.isRunning());
		assertEquals(0, delegator.getNumberOfConnectedClients());

		// Start up a consumer which consumes any number that's a multiple of 2.
		Consumer twoConsumer = new Consumer(NetworkIntegrationDemo.IP, NetworkIntegrationDemo.PORT, 2);
		assertEquals(0, twoConsumer.getNumbersConsumed().length);
		assertEquals(2, twoConsumer.getPreferredMultiple());
		// Start up the consumer, wait a little bit, and make sure it did connect.
		twoConsumer.start();
		Thread.sleep(100);
		assertEquals(1, delegator.getNumberOfConnectedClients());

		// Prepare a list of producers that'll generate numbers.
		List<Producer> producers = new ArrayList<>();
		ClientFactory factory = new ClientFactory(NetworkIntegrationDemo.IP, NetworkIntegrationDemo.PORT);
		for (int i = 0; i < 3; i++) {
			producers.add(factory.buildProducer());
		}
		producers.forEach(Thread::start);
		// Wait a little bit, then ensure they're connected and ready to go.
		Thread.sleep(100);
		assertEquals(1 + producers.size(), delegator.getNumberOfConnectedClients());
		producers.forEach(producer -> assertTrue(producer.isRunning()));

		// Wait for each producer to have produced its first number.
		log.info("Waiting for producers to produce their first number.");
		Thread.sleep(Producer.MAX_DELAY);
		for (Producer producer : producers) {
			assertTrue(producer.getNumbersProduced().length > 0);
		}
		// Ensure that at least one producer produced a multiple of 2.
		this.waitForMultipleOfN(2, producers);
		assertTrue(twoConsumer.getNumbersConsumed().length > 0);
		// Ensure that all consumed numbers are multiples of 2.
		for (Integer i : twoConsumer.getNumbersConsumed()) {
			assertEquals(0, i % 2);
		}

		// Shut down the delegator, wait a little bit, and make sure everything's cleaned up.
		delegator.shutdown();
		Thread.sleep(100);
		assertFalse(delegator.isRunning());
	}

	/**
	 * Helper method that'll wait until at least one multiple of n was produced
	 * by at least one producer.
	 * @param n A number for which a multiple should be generated.
	 * @param producers The list of producers.
	 */
	private void waitForMultipleOfN(int n, List<Producer> producers) throws InterruptedException {
		boolean containsMultiple = false;
		while (!containsMultiple) {
			for (Producer producer : producers) {
				for (Integer i : producer.getNumbersProduced()) {
					if (i % n == 0) {
						containsMultiple = true;
						break;
					}
				}
			}
			// Wait a while for producers to produce more.
			Thread.sleep(1000);
		}
	}
}
