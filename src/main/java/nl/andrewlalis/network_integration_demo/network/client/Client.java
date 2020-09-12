package nl.andrewlalis.network_integration_demo.network.client;

import lombok.extern.slf4j.Slf4j;
import nl.andrewlalis.network_integration_demo.network.Message;
import nl.andrewlalis.network_integration_demo.util.StoppableThread;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * A client which connects to the delegator.
 */
@Slf4j
public abstract class Client extends StoppableThread {
	private final Socket clientSocket;
	protected final ObjectInputStream objectInputStream;
	protected final ObjectOutputStream objectOutputStream;

	public Client(String ip, int port) throws IOException {
		this.clientSocket = new Socket(ip, port);
		this.objectInputStream = new ObjectInputStream(this.clientSocket.getInputStream());
		this.objectOutputStream = new ObjectOutputStream(this.clientSocket.getOutputStream());
	}

	protected void send(Message message) throws IOException {
		this.objectOutputStream.writeObject(message);
		this.objectOutputStream.flush();
	}

	@Override
	public void afterStop() {
		try {
			this.objectInputStream.close();
			this.objectOutputStream.close();
			this.clientSocket.close();
		} catch (IOException e) {
			log.error("Could not close client socket components.");
		}
	}
}
