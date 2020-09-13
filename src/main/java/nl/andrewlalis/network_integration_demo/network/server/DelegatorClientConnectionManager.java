package nl.andrewlalis.network_integration_demo.network.server;

import lombok.extern.slf4j.Slf4j;
import nl.andrewlalis.network_integration_demo.network.Message;
import nl.andrewlalis.network_integration_demo.network.MessageType;
import nl.andrewlalis.network_integration_demo.network.client.ClientType;
import nl.andrewlalis.network_integration_demo.util.StoppableThread;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Dedicated thread for handling the connection between the delegator and a
 * single client, such as a producer or consumer.
 */
@Slf4j
public class DelegatorClientConnectionManager extends StoppableThread {
	private final DelegatorServer delegatorServer;
	private final Socket clientSocket;
	private final ObjectOutputStream objectOutputStream;
	private final ObjectInputStream objectInputStream;

	private ClientType type = null;
	private Integer preferredMultiple = null;

	public DelegatorClientConnectionManager(Socket clientSocket, DelegatorServer delegatorServer) throws IOException {
		this.clientSocket = clientSocket;
		this.delegatorServer = delegatorServer;
		this.objectOutputStream = new ObjectOutputStream(this.clientSocket.getOutputStream());
		this.objectInputStream = new ObjectInputStream(this.clientSocket.getInputStream());
	}

	@Override
	public void shutdown() {
		super.shutdown();
		try {
			this.clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Before beginning the loop, wait for and read an initial message which
	 * will be sent by the client once it connects. This is used to identify the
	 * type of client that connected.
	 */
	@Override
	public void beforeStart() {
		try {
			Message message = (Message) this.objectInputStream.readObject();
			if (message.getType() == MessageType.CLIENT_IDENT) {
				this.type = ClientType.valueOf(message.getContent());
				log.info("Received client ident message from a {}.", this.type.name());
				if (this.type == ClientType.CONSUMER) {
					Message preferenceMessage = (Message) this.objectInputStream.readObject();
					this.preferredMultiple = Integer.parseInt(preferenceMessage.getContent());
					log.info("Received preferred multiple of {} from consumer.", this.preferredMultiple);
				}
			}
		} catch (ClassNotFoundException | IOException e) {
			log.error("Exception encountered while receiving initial message from client.");
			throw new RuntimeException("Cannot start delegator client connection manager.");
		}
	}

	@Override
	public void step() {
		try {
			Message message = (Message) this.objectInputStream.readObject();
			if (message.getType() == MessageType.DISCONNECT) {
				this.shutdown();
			} else if (message.getType() == MessageType.NUMBER) {
				if (this.getClientType() == ClientType.PRODUCER) {
					this.delegatorServer.onNumberReceived(message);
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			this.shutdown();
		}
	}

	public ClientType getClientType() {
		return this.type;
	}

	public Integer getPreferredMultiple() {
		return this.preferredMultiple;
	}

	public void sendToClient(Message message) throws IOException {
		this.objectOutputStream.writeObject(message);
		this.objectOutputStream.flush();
	}
}
