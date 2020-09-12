package nl.andrewlalis.network_integration_demo.network;

import lombok.Getter;

import java.io.Serializable;

/**
 * Contains some simple messages to send back and forth.
 */
@Getter
public class Message implements Serializable {
	MessageType type;
	String content;

	public Message(MessageType type, String content) {
		this.type = type;
		this.content = content;
	}
}
