package ttaomae.connectn.network.server;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.Optional;

import ttaomae.connectn.ImmutableBoard;
import ttaomae.connectn.Player;
import ttaomae.connectn.network.LostConnectionException;
import ttaomae.connectn.network.ProtocolEvent;
import ttaomae.connectn.network.ProtocolEvent.Message;
import ttaomae.connectn.network.ProtocolException;
import ttaomae.connectn.network.ProtocolHandler;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class ClientHandler implements Player
{
    private final ProtocolHandler protocolHandler;

    ClientHandler(Socket socket) throws IOException
    {
        checkNotNull(socket, "socket must not be null.");

        this.protocolHandler = new ProtocolHandler(socket);
    }

    public void startMatch() throws LostConnectionException
    {
        this.protocolHandler.sendMessage(Message.START_GAME);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method may return {@code null} indicating that the connection with
     * the client has been lost.
     */
    @Override
    public Optional<Integer> getMove(ImmutableBoard board)
    {
        ProtocolEvent event;
        try {
            this.protocolHandler.sendMessage(Message.REQUEST_MOVE);
            event = this.protocolHandler.receiveEvent();
        }
        catch (LostConnectionException e) {
            @SuppressFBWarnings(value="NP_OPTIONAL_RETURN_NULL",
                    justification="null return value has special meaning; documented in Javadoc.")
            Optional<Integer> result = null;
            return result;
        }

        if (event.getMessage() != Message.PLAYER_MOVE) {
            throw new ProtocolException(String.format("Expected %s but received %s.",
                    Message.PLAYER_MOVE, event.getMessage()));
        }

        return event.getMove();
    }

    public void sendOpponentMove(int move) throws LostConnectionException
    {
        this.protocolHandler.sendOpponentMove(move);
    }

    public void sendMessage(Message message) throws LostConnectionException
    {
        this.protocolHandler.sendMessage(message);
    }

    public boolean requestRematch() throws LostConnectionException
    {
        this.protocolHandler.sendMessage(Message.REQUEST_REMATCH);
        ProtocolEvent response = this.protocolHandler.receiveEvent();

        switch (response.getMessage()) {
            case ACCEPT_REMATCH:
                return true;
            case DENY_REMATCH:
                return false;
            default:
                throw new ProtocolException("Invalid response to rematch request: "
                        + response.getMessage());
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ClientHandler)) {
            return false;
        }

        ClientHandler other = (ClientHandler) obj;

        return Objects.equals(this.protocolHandler, other.protocolHandler);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.protocolHandler);
    }
}
