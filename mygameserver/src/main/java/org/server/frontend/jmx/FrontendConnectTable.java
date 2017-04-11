package org.server.frontend.jmx;

import java.io.Serializable;

public class FrontendConnectTable implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final long id, sendBytes, receivedBytes, sendMessages, receivedMessages;
	
	public FrontendConnectTable(long id, long sendBytes, long receivedBytes, long sendMessages, long receivedMessages) {
        this.id = id;
        this.sendBytes = sendBytes;
        this.receivedBytes = receivedBytes;
        this.sendMessages = sendMessages;
        this.receivedMessages = receivedMessages;
    }
	public long getId() {
        return id;
    }

    public long getSendBytes() {
        return sendBytes;
    }

    public long getReceivedBytes() {
        return receivedBytes;
    }

    public long getSendMessages() {
        return sendMessages;
    }

    public long getReceivedMessages() {
        return receivedMessages;
    }
}
