package ttaomae.connectn.network.server;

/**
 * Signals that a client has disconnected from the server.
 *
 * @author Todd Taomae
 */
public class ClientDisconnectedException extends Exception
{
    private static final long serialVersionUID = -2012277466247744994L;

    private final ClientHandler clientHandler;

    public ClientDisconnectedException(String message, Throwable cause, ClientHandler clientHandler)
    {
        super(message, cause);
        this.clientHandler = clientHandler;
    }

    public ClientHandler getClientHandler()
    {
        return this.clientHandler;
    }
}
