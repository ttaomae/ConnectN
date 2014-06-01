package ttaomae.connectn.network.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.Socket;

import org.junit.Test;

public class NetworkGameManagerTest
{

    @Test
    public void test() throws IOException
    {
        try {
            new NetworkGameManager(null, new Socket(), new Socket());
            fail("constructor with null server");
        } catch (IllegalArgumentException e) {
            assertEquals("failure - null server", "server must not be null", e.getMessage());
        }

        try {
            new NetworkGameManager(new Server(1234), null, new Socket());
            fail("constructor with null socket");
        } catch (IllegalArgumentException e) {
            assertEquals("failure - null socket 1", "socket must not be null", e.getMessage());
        }

        try {
            new NetworkGameManager(new Server(1234), new Socket(), null);
            fail("constructor with null socket");
        } catch (IllegalArgumentException e) {
            assertEquals("failure - null socket 2", "socket must not be null", e.getMessage());
        }

    }
}
