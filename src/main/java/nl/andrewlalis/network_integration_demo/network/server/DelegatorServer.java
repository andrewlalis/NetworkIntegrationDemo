package nl.andrewlalis.network_integration_demo.network.server;

import lombok.extern.slf4j.Slf4j;
import nl.andrewlalis.network_integration_demo.network.Message;
import nl.andrewlalis.network_integration_demo.network.client.ClientType;
import nl.andrewlalis.network_integration_demo.util.StoppableThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The central delegator server. Keeps a list of producers and consumers,
 */
@Slf4j
public class DelegatorServer extends StoppableThread {
	private final ServerSocket serverSocket;
	private final List<DelegatorClientConnectionManager> connectionManagers;

	public DelegatorServer(int port) throws IOException {
		this.connectionManagers = new ArrayList<>();
		this.serverSocket = new ServerSocket(port);
	}

	@Override
	public void step() {
		try {
			log.info("Waiting for client connection on port {}.", this.serverSocket.getLocalPort());
			Socket clientSocket = this.serverSocket.accept();
			DelegatorClientConnectionManager connectionManager = new DelegatorClientConnectionManager(
					clientSocket,
					this
			);
			this.connectionManagers.add(connectionManager);
			log.info("Initializing connection manager for newly connected client.");
			connectionManager.start();
		} catch (IOException e) {
			log.error("IOException occurred while accepting client connection.");
		}
	}

	/**
	 * After the main server shuts down, shutdown all client connection managers.
	 */
	@Override
	public void afterStop() {
		log.info("Shutting down all connection managers.");
		this.connectionManagers.forEach(StoppableThread::shutdown);
		for (DelegatorClientConnectionManager connectionManager : this.connectionManagers) {
			try {
				connectionManager.join();
			} catch (InterruptedException e) {
				log.error("Could not join on connection manager thread after shutting it down.");
			}
		}
		try {
			this.serverSocket.close();
		} catch (IOException e) {
			log.error("Could not close server socket.");
		}
	}

	/**
	 * This method should be called when a delegator receives a number from a
	 * producer.
	 * @param message The message containing the number.
	 */
	public void onNumberReceived(Message message) {
		int number = Integer.parseInt(message.getContent());
		List<DelegatorClientConnectionManager> eligibleClientManagers = new ArrayList<>();
		for (DelegatorClientConnectionManager clientManager : this.connectionManagers) {
			if (
					clientManager.isRunning()
					&& clientManager.getClientType() == ClientType.CONSUMER
					&& clientManager.getPreferredMultiple() != null
					&& number % clientManager.getPreferredMultiple() == 0
			) {
				eligibleClientManagers.add(clientManager);
			}
		}
		if (eligibleClientManagers.isEmpty()) {
			log.warn("No eligible consumers for the number {}.", number);
		} else {
			DelegatorClientConnectionManager clientManager = eligibleClientManagers.get(
					ThreadLocalRandom.current().nextInt(eligibleClientManagers.size())
			);
			try {
				clientManager.sendToClient(message);
			} catch (IOException e) {
				log.error("Could not forward number to consumer.");
			}
		}
	}
}
