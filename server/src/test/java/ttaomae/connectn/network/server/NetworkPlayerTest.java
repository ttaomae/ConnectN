package ttaomae.connectn.network.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.Socket;

import org.junit.Test;

public class NetworkPlayerTest
{
    @Test
    public void test() throws IOException
    {
        try {
            new NetworkPlayer(null, new Socket());
            fail("constructor with null server");
        } catch (NullPointerException e) {
            assertEquals("failure - null server", "server must not be null", e.getMessage());
        }

        try {
            Server server = new Server(1234);
            new Thread(server).start();
            Socket socket1 = new Socket("localhost", 1234);
            Socket socket2 = new Socket("localhost", 1234);
            new NetworkPlayer(new NetworkGameManager(server, socket1, socket2), null);
            fail("constructor with null socket");
        } catch (NullPointerException e) {
            assertEquals("failure - null socket", "socket must not be null", e.getMessage());
        }

        try {
            new NetworkPlayer(null, new Socket());
            fail("constructor with null server");
        } catch (NullPointerException e) {
            assertEquals("failure - null server", "server must not be null", e.getMessage());
        }

    }
}
