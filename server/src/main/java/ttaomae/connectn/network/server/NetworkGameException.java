package ttaomae.connectn.network.server;

/**
 * Signals that an error occurred while running a {@link NetworkGameManager}.
 *
 * @author Todd Taomae
 */
public class NetworkGameException extends Exception
{
    private static final long serialVersionUID = -3103603898189444539L;

    private final NetworkGameManager networkGameManager;

    public NetworkGameException(String message, Throwable cause,
            NetworkGameManager networkGameManager)
    {
        super(message, cause);
        this.networkGameManager = networkGameManager;
    }

    public NetworkGameManager getNetworkGameManager()
    {
        return this.networkGameManager;
    }
}
