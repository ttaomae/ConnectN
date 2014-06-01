package ttaomae.connectn.network.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class ClientManagerTest
{
    @Test
    public void testConstructor()
    {
        try {
            new ClientManager(null);
            fail("constructor with null server");
        } catch (IllegalArgumentException e) {
            assertEquals("failure - null server", "server must not be null", e.getMessage());
        }
    }
}
