package ttaomae.connectn.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.Test;

import ttaomae.connectn.network.ProtocolEvent.Message;

public class ProtocolEventTest
{
    @Test
    public void testCreateProtocolEvent()
    {
        for (Message message : Message.getNormalMessages()) {
            ProtocolEvent event = ProtocolEvent.createProtocolEvent(message);
            assertEquals(message, event.getMessage());
            assertFalse(event.getMove().isPresent());
        }
    }

    @Test
    public void testCreateProtocolEvent_invalidMessage()
    {
        try {
            ProtocolEvent.createProtocolEvent(Message.PLAYER_MOVE);
            fail();
        } catch (IllegalArgumentException expected) {}

        try {
            ProtocolEvent.createProtocolEvent(Message.OPPONENT_MOVE);
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    @Test
    public void testCreateMoveProtocolEvent()
    {
        for (int i = 0; i < 10; i++) {
            ProtocolEvent event = ProtocolEvent.createProtocolMoveEvent(Message.PLAYER_MOVE, i);
            assertEquals(Message.PLAYER_MOVE, event.getMessage());
            assertEquals(Integer.valueOf(i), event.getMove().get());
        }

        for (int i = 0; i < 10; i++) {
            ProtocolEvent event = ProtocolEvent.createProtocolMoveEvent(Message.OPPONENT_MOVE, i);
            assertEquals(Message.OPPONENT_MOVE, event.getMessage());
            assertEquals(Integer.valueOf(i), event.getMove().get());
        }
    }

    @Test
    public void testCreateMoveProtocolEvent_invalidMessage()
    {
        for (Message message : Message.getNormalMessages()) {
            try {
                ProtocolEvent.createProtocolMoveEvent(message, 0);
                fail();
            } catch (IllegalArgumentException expected) {}
        }
    }
}
