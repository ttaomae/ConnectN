package ttaomae.connectn.network;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

import ttaomae.connectn.network.ProtocolEvent.Message;

/**
 * A handler for managing the client-server protocol.
 *
 * @author Todd Taomae
 */
public class ProtocolHandler
{
    // cache of Message values
    private static final Message[] MESSAGE_VALUES = Message.values();

    private final Socket socket;
    private final DataOutputStream writer;
    private final DataInputStream reader;

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
            } while (MESSAGE_VALUES[messageIndex] == Message.PING);

            if (messageIndex < 0 || messageIndex > MESSAGE_VALUES.length) {
                throw new ProtocolException("Unknown message received: " + messageIndex);
            }

            Message message = MESSAGE_VALUES[messageIndex];
            if (message.isMoveMessage()) {
                return ProtocolEvent.createProtocolMoveEvent(message, this.reader.readInt());
            }
            else {
                return ProtocolEvent.createProtocolEvent(message);
            }
        }
        catch (IOException e) {
            // also handles EOFException
            throw new LostConnectionException(e);
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
