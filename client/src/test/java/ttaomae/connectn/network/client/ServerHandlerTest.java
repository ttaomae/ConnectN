package ttaomae.connectn.network.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.Socket;

import org.junit.Test;

import ttaomae.connectn.Board;
import ttaomae.connectn.Player;

public class ServerHandlerTest
{
    @Test
    public void testConstructor_illegalArguments() throws IOException
    {
        Socket mockSocket = mock(Socket.class);
        Player mockPlayer = mock(Player.class);
        Board mockBoard = mock(Board.class);

        try {
            new ServerHandler(mockSocket, mockPlayer, null);
            fail("constructor with null board");
        } catch (NullPointerException e) {
            assertEquals("failure - null board", "board must not be null", e.getMessage());
        }

        try {
            new ServerHandler(mockSocket, null, mockBoard);
            fail("constructor with null player");
        } catch (NullPointerException e) {
            assertEquals("failure - null player", "player must not be null", e.getMessage());
        }

        try {
            new ServerHandler(null, mockPlayer, mockBoard);
            fail("constructor with null socket");
        } catch (NullPointerException e) {
            assertEquals("failure - null player", "socket must not be null", e.getMessage());
        }

    }
}
