package ttaomae.connectn.network.server;

public class ClientDisconnectedException extends NetworkGameException
{
    private static final long serialVersionUID = -4669119110845430570L;

    public ClientDisconnectedException(String message, Throwable cause,
            NetworkGameManager networkGameManager)
    {
        super(message, cause, networkGameManager);
    }

}
