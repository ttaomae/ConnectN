package ttaomae.connectn.network.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import ttaomae.connectn.Board;
import ttaomae.connectn.RandomPlayer;

public class ClientTest
{
    @Test
    public void testConstructor() throws IOException
    {
        try {
            new Client("localhost", 1234, new RandomPlayer(), null);
            fail("constructor with null board");
        } catch (NullPointerException e) {
            assertEquals("failure - null board", "board must not be null", e.getMessage());
        }

        try {
            new Client("localhost", 1234, null, new Board());
            fail("constructor with null player");
        } catch (NullPointerException e) {
            assertEquals("failure - null player", "player must not be null", e.getMessage());
        }

        try {
            new Client("localhost", -1, new RandomPlayer(), new Board());
            fail("constructor with negative port");
        } catch (IllegalArgumentException e) {
            assertTrue("failure - negative port", e.getMessage().contains("port"));
        }

        try {
            new Client("localhost", 1000000, new RandomPlayer(), new Board());
            fail("constructor with port out of range");
        } catch (IllegalArgumentException e) {
            assertTrue("failure - port out of range", e.getMessage().contains("port"));
        }
    }
}
