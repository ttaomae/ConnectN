package ttaomae.connectn.network;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ttaomae.connectn.network.ProtocolEvent.Message;

import com.google.common.collect.ImmutableList;

/**
 * A handler for managing the client-server protocol.
 *
 * @author Todd Taomae
 */
public class ProtocolHandler
{
    // cache of Message values
    private static final List<Message> MESSAGE_VALUES = ImmutableList.copyOf(Message.values());

    private final Socket socket;
    private final DataOutputStream writer;
    private final DataInputStream reader;

    private final List<ProtocolListener> listeners;

    /**
     * Constructs a new protocol handler which communicates using the specified
     * socket.
     *
     * @param socket the socket on which communication occurs
     * @throws IOException if the input or output stream could not be obtained
     *          from the socket
     */
    public ProtocolHandler(Socket socket) throws IOException
    {
        checkNotNull(socket, "socket must not be null.");

        this.socket = socket;
        this.writer = new DataOutputStream(socket.getOutputStream());
        this.reader = new DataInputStream(socket.getInputStream());

        this.listeners = new ArrayList<>();
    }

    /**
     * Sends a simple message.
     *
     * @param message the message to send
     * @throws LostConnectionException if the connection was lost while sending
     *          the message
     */
    public void sendMessage(Message message) throws LostConnectionException
    {
        try {
            this.writer.writeInt(message.ordinal());
            if (!message.isMoveMessage()) {
                this.notifyListenersMessageSent(message);
            }
        } catch (IOException e) {
            throw new LostConnectionException(e);
        }
    }

    /**
     * Sends a {@link Message#PLAYER_MOVE PLAYER_MOVE} event.
     *
     * @param move the move to send
     * @throws LostConnectionException if the connection was lost while sending
     *          the move
     */
    public void sendPlayerMove(int move) throws LostConnectionException
    {
        sendMessage(Message.PLAYER_MOVE);
        try {
            this.writer.writeInt(move);
            this.notifyListenersMoveSent(Message.PLAYER_MOVE, move);
        }
        catch (IOException e) {
            throw new LostConnectionException(e);
        }
    }

    /**
     * Sends an {@link Message#OPPONENT_MOVE OPPONENT_MOVE} event.
     *
     * @param move the move to send
     * @throws LostConnectionException if the connection was lost while sending
     *          the move
     *
     */
    public void sendOpponentMove(int move) throws LostConnectionException
    {
        sendMessage(Message.OPPONENT_MOVE);
        try {
            this.writer.writeInt(move);
            this.notifyListenersMoveSent(Message.OPPONENT_MOVE, move);
        }
        catch (IOException e) {
            throw new LostConnectionException(e);
        }
    }

    /**
     * Listens for a protocol event. This method blocks until an event is
     * received. All {@link Message#PING PING} messages are ignored.
     *
     * @return the received event
     * @throws LostConnectionException if the connection was lost while waiting
     *          for an event
     */
    public ProtocolEvent receiveEvent() throws LostConnectionException
    {
        try {
            // read repeatedly until a non-PING message is received
            int messageIndex;
            do {
                messageIndex = this.reader.readInt();
            } while (MESSAGE_VALUES.get(messageIndex) == Message.PING);

            if (messageIndex < 0 || messageIndex > MESSAGE_VALUES.size()) {
                throw new ProtocolException("Unknown message received: " + messageIndex);
            }

            Message message = MESSAGE_VALUES.get(messageIndex);
            ProtocolEvent receivedEvent = message.isMoveMessage()
                    ? ProtocolEvent.createProtocolMoveEvent(message, this.reader.readInt())
                    : ProtocolEvent.createProtocolEvent(message);
            this.notifyListenersEventReceived(receivedEvent);
            return receivedEvent;
        }
        catch (IOException e) {
            // also handles EOFException
            throw new LostConnectionException(e);
        }
    }

    public boolean isConnected()
    {
        try {
            this.sendMessage(Message.PING);
            return true;
        }
        catch (LostConnectionException e) {
            return false;
        }
    }

    public void addListener(ProtocolListener listener)
    {
        checkNotNull(listener, "listener must not be null");

        this.listeners.add(listener);
    }

    private void notifyListenersEventReceived(ProtocolEvent receivedEvent)
    {
        for (ProtocolListener listener : this.listeners) {
            assert listener != null : "listener should not be null";
            listener.eventReceived(receivedEvent);
        }
    }

    private void notifyListenersMessageSent(Message sentMessage)
    {
        for (ProtocolListener listener : this.listeners) {
            assert listener != null : "listener should not be null";
            listener.messageSent(sentMessage);
        }
    }

    private void notifyListenersMoveSent(Message moveMessage, int move)
    {
        for (ProtocolListener listener : this.listeners) {
            assert listener != null : "listener should not be null";
            listener.moveSent(moveMessage, move);
        }
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.socket);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        ProtocolHandler other = (ProtocolHandler) obj;
        return Objects.equals(this.socket, other.socket);
    }
}
