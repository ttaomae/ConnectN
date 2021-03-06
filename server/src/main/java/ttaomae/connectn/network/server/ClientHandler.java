package ttaomae.connectn.network.server;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.Optional;

import ttaomae.connectn.ImmutableBoard;
import ttaomae.connectn.network.LostConnectionException;
import ttaomae.connectn.network.ProtocolEvent;
import ttaomae.connectn.network.ProtocolEvent.Message;
import ttaomae.connectn.network.ProtocolException;
import ttaomae.connectn.network.ProtocolHandler;
import ttaomae.connectn.player.Player;

public class ClientHandler implements Player
{
    private final ProtocolHandler protocolHandler;

    ClientHandler(Socket socket) throws IOException
    {
        checkNotNull(socket, "socket must not be null.");

        this.protocolHandler = new ProtocolHandler(socket);
    }

    /**
     * {@inheritDoc}
     * @throws RuntimeException if a {@link LostConnectionException} is
     *          encountered, it will be wrapped in a RuntimeException; callers
     *          of this method should catch RuntimeExceptions and check if the
     *          {@linkplain Throwable#getCause() cause} was a
     *          LostConnectionException
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
            throw new RuntimeException(e);
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

    public boolean isConnected()
    {
        return this.protocolHandler.isConnected();
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
