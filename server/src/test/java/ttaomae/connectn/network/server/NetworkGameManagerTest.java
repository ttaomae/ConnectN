package ttaomae.connectn.network.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.Test;

public class NetworkGameManagerTest
{
    @Test
    public void testConstructor_illegalArguments() throws IOException
    {
        ClientManager mockClientManager = mock(ClientManager.class);
        ClientHandler mockClientHandlerOne = mock(ClientHandler.class);
        ClientHandler mockClientHandlerTwo = mock(ClientHandler.class);

        try {
            new NetworkGameManager(null, mockClientHandlerOne, mockClientHandlerTwo);
            fail("constructor with null client manager");
        } catch (NullPointerException e) {
            assertEquals("failure - null client manager",
                    "clientManager must not be null", e.getMessage());
        }

        try {
            new NetworkGameManager(mockClientManager, null, mockClientHandlerTwo);
            fail("constructor with null client handler 1");
        } catch (NullPointerException e) {
            assertEquals("failure - null client handler 1",
                    "playerOneHandler must not be null", e.getMessage());
        }

        try {
            new NetworkGameManager(mockClientManager, mockClientHandlerOne, null);
            fail("constructor with null client handler 2");
        } catch (NullPointerException e) {
            assertEquals("failure - null client handler 2",
                    "playerTwoHandler must not be null", e.getMessage());
        }
    }
}
