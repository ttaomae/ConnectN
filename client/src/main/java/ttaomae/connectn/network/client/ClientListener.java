package ttaomae.connectn.network.client;

public interface ClientListener
{
    /**
     * Invoked when the client receives a message from the server
     * 
     * @param message the message received from the server
     */
    public void clientReceivedMessage(String message);

    /**
     * Invoked when the client sends a message to the server.
     * 
     * @param message the message sent to the server
     */
    public void clientSentMessage(String message);
}
