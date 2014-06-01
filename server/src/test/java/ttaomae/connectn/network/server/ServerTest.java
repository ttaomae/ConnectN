package ttaomae.connectn.network.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class ServerTest
{
    @Test
    public void testConstructor()
    {
        try {
            new Server(-1);
            fail("constructor with negative port");
        } catch (IllegalArgumentException e) {
            assertEquals("failure - negative port", "port out of range: -1", e.getMessage());
        }

        try {
            new Server(1000000);
            fail("constructor with negative port");
        } catch (IllegalArgumentException e) {
            assertEquals("failure - port out of range", "port out of range: 1000000", e.getMessage());
        }
    }
}
