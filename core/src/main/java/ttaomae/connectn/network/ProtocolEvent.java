package ttaomae.connectn.network;

import java.util.EnumSet;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A client-server protocol event.
 * <p>
 * An event consists of a single {@link Message} and an optional move if the
 * message was a {@link Message#PLAYER_MOVE PLAYER_MOVE} or
 * {@link Message#OPPONENT_MOVE OPPONENT_MOVE}.
 *
 * @author Todd Taomae
 */
public final class ProtocolEvent
{
    private final Message message;
    private final Optional<Integer> move;

    private ProtocolEvent(Message message, Integer move)
    {
        assert message.isMoveMessage() ? move != null : move == null;

        this.message = message;
        this.move = Optional.ofNullable(move);
    }

    /**
     * Factory method for creating a protocol non-move event.
     */
    static ProtocolEvent createProtocolEvent(Message message)
    {
        checkArgument(!message.isMoveMessage(), "message must not be a move message");

        return new ProtocolEvent(message, null);
    }

    /**
     * Factory method for creating a protocol move event.
     */
    static ProtocolEvent createProtocolMoveEvent(Message message, int move)
    {
        checkArgument(message.isMoveMessage(), "message must be a move message.");
        return new ProtocolEvent(message, move);
    }

    /**
     * Returns the message associated with this event.
     *
     * @return the message associated with this event
     */
    public Message getMessage()
    {
        return this.message;
    }

    /**
     * Returns the move associated with this event.
     *
     * @return the move associated with this event
     */
    public Optional<Integer> getMove()
    {
        return this.move;
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder(this.message.toString());
        if (this.move.isPresent()) {
            result.append(" : ").append(move.get());
        }
        return result.toString();
    }

    /**
     * A message sent as part of the client-server protocol.
     *
     * @author Todd Taomae
     */
    public enum Message
    {
        /** Sent by the server at the start of a game. */
        START_GAME,

        /**
         * Sent by the server to request that the client sends a
         * {@link #PLAYER_MOVE} back to the server.
         */
        REQUEST_MOVE,

        /** Sent by the client to indicate that a move will be sent. */
        PLAYER_MOVE,

        /**
         * Sent by the server to indicate that the client's opponent's move will
         * be sent.
         */
        OPPONENT_MOVE,

        /** Sent by the server after a match completes  */
        REQUEST_REMATCH,

        /** Sent by the client to indicate that they accept a rematch request. */
        ACCEPT_REMATCH,

        /** Sent by the client to indicate that they deny a rematch request. */
        DENY_REMATCH,

        /**
         * Sent by the server to indicate that the client's opponent has
         * disconnected.
         */
        OPPONENT_DISCONNECTED,

        /**
         * Sent by the server to check if the connection is still active. This
         * should be ignored by the client.
         */
        PING;

        public boolean isMoveMessage()
        {
            return this == PLAYER_MOVE || this == OPPONENT_MOVE;
        }

        public boolean isRematchResponse()
        {
            return this == ACCEPT_REMATCH || this == DENY_REMATCH || this == OPPONENT_DISCONNECTED;
        }

        public static EnumSet<Message> getNormalMessages()
        {
            EnumSet<Message> messages = EnumSet.allOf(Message.class);
            messages.removeAll(getSpecialMessage());
            return messages;
        }

        public static EnumSet<Message> getSpecialMessage()
        {
            return EnumSet.of(PLAYER_MOVE, OPPONENT_MOVE, PING);
        }
    }
}
