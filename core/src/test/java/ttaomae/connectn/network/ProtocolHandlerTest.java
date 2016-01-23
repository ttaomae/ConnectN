package ttaomae.connectn.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import ttaomae.connectn.network.ProtocolEvent.Message;

public class ProtocolHandlerTest
{
    private Socket clientSocket;
    private Socket serverSocket;
    private ProtocolHandler client;
    private ProtocolHandler server;
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Before
    public void setup() throws IOException
    {
        // setup Piped[Input|Output]Streams
        PipedInputStream clientIn = new PipedInputStream();
        PipedOutputStream clientOut = new PipedOutputStream();
        PipedInputStream serverIn = new PipedInputStream();
        PipedOutputStream serverOut = new PipedOutputStream();

        serverIn.connect(clientOut);
        serverOut.connect(clientIn);

        // create mock sockets with piped streams
        clientSocket = mock(Socket.class);
        when(clientSocket.getInputStream()).thenReturn(clientIn);
        when(clientSocket.getOutputStream()).thenReturn(clientOut);
        doAnswer(invocation -> {
            clientIn.close();
            clientOut.close();
            return null;
        }).when(clientSocket).close();

        serverSocket = mock(Socket.class);
        when(serverSocket.getInputStream()).thenReturn(serverIn);
        when(serverSocket.getOutputStream()).thenReturn(serverOut);
        doAnswer(invocation -> {
            serverIn.close();
            serverOut.close();
            return null;
        }).when(serverSocket).close();

        this.client = new ProtocolHandler(clientSocket);
        this.server = new ProtocolHandler(serverSocket);
    }

    @AfterClass
    public static void cleanup()
    {
        executorService.shutdownNow();
    }

    @Test
    public void testSendMessage_synchronous() throws IOException, LostConnectionException
    {
        for (Message message : Message.getNormalMessages()) {
            testSendMessage(client, server, message);
            testSendMessage(server, client, message);
        }
    }

    @Test
    public void testSendMessage_asynchronous() throws IOException, LostConnectionException
    {
        executorService.submit(() -> {
            for (Message message : Message.getNormalMessages()) {
                server.sendMessage(message);
            }
            // return an arbitrary value so that the lambda is interpreted as a
            // Callable, which throws Exception
            return null;
        });

        // since getRegularMessages returns an EnumSet, the iteration occurs
        // according to the enum's natural order, so it will be consistent
        // across multiple iterations
        for (Message message : Message.getNormalMessages()) {
            ProtocolEvent event = client.receiveEvent();
            assertEquals(event.getMessage(), message);
            assertFalse(event.getMove().isPresent());
        }
    }

    @Test
    public void testSendPlayerMove() throws LostConnectionException
    {
        for (int i = 0; i < 10; i++) {
            client.sendPlayerMove(i);
            ProtocolEvent event = server.receiveEvent();
            assertEquals(Message.PLAYER_MOVE, event.getMessage());
            assertEquals(new Integer(i), event.getMove().get());
        }
    }

    @Test
    public void testSendOpponentMove() throws LostConnectionException
    {
        for (int i = 0; i < 10; i++) {
            server.sendOpponentMove(i);
            ProtocolEvent event = client.receiveEvent();
            assertEquals(Message.OPPONENT_MOVE, event.getMessage());
            assertEquals(new Integer(i), event.getMove().get());
        }
    }

    @Test
    public void testReceiveEvent_ignorePing() throws LostConnectionException, InterruptedException
    {
        executorService.submit(() -> {
            for (Message message : Message.getNormalMessages()) {
                server.sendMessage(Message.PING);
                server.sendMessage(message);
            }
            // return an arbitrary value so that the lambda is interpreted as a
            // Callable, which throws Exception
            return null;
        });

        // since getRegularMessages returns an EnumSet, the iteration occurs
        // according to the enum's natural order, so it will be consistent
        // across multiple iterations
        for (Message message : Message.getNormalMessages()) {
            ProtocolEvent event = client.receiveEvent();
            assertEquals(event.getMessage(), message);
            assertFalse(event.getMove().isPresent());
        }
    }

    @Test
    public void testReceiveEvent_ignoreMultiplePing()
            throws LostConnectionException, InterruptedException
    {
        executorService.submit(() -> {
            for (Message message : Message.getNormalMessages()) {
                server.sendMessage(Message.PING);
                server.sendMessage(Message.PING);
                server.sendMessage(Message.PING);
                server.sendMessage(message);
            }
            // return an arbitrary value so that the lambda is interpreted as a
            // Callable, which throws Exception
            return null;
        });

        // since getRegularMessages returns an EnumSet, the iteration occurs
        // according to the enum's natural order, so it will be consistent
        // across multiple iterations
        for (Message message : Message.getNormalMessages()) {
            ProtocolEvent event = client.receiveEvent();
            assertEquals(event.getMessage(), message);
            assertFalse(event.getMove().isPresent());
        }
    }

    @Test
    public void testSendMessage_closedSocket() throws IOException
    {
        clientSocket.close();

        try {
            client.sendMessage(Message.START_GAME);
            fail();
        } catch (LostConnectionException expected) {}

        try {
            server.sendMessage(Message.START_GAME);
            fail();
        } catch (LostConnectionException expected) {}
    }

    @Test
    public void testRecieveMessage_closedSocket() throws IOException
    {
        clientSocket.close();

        try {
            client.receiveEvent();
            fail();
        } catch (LostConnectionException expected) {}

        try {
            server.receiveEvent();
            fail();
        } catch (LostConnectionException expected) {}
    }

    @Test
    public void testSendPlayerMove_closedSocket() throws IOException
    {
        clientSocket.close();

        try {
            client.sendPlayerMove(0);
            fail();
        } catch (LostConnectionException expected) {}

        try {
            server.sendPlayerMove(0);
            fail();
        } catch (LostConnectionException expected) {}
    }

    @Test
    public void testSendOpponentMove_closedSocket() throws IOException
    {
        clientSocket.close();

        try {
            client.sendOpponentMove(0);
            fail();
        } catch (LostConnectionException expected) {}

        try {
            server.sendOpponentMove(0);
            fail();
        } catch (LostConnectionException expected) {}
    }

    @Test
    public void testEquals() throws IOException
    {
        Socket mockSocket = mock(Socket.class);
        ProtocolHandler handler1 = new ProtocolHandler(mockSocket);
        ProtocolHandler handler2 = new ProtocolHandler(mockSocket);

        assertTrue(handler1.equals(handler2));
        assertTrue(handler2.equals(handler1));

        assertTrue(client.equals(client));
        assertTrue(server.equals(server));

        assertFalse(client.equals(null));
        assertFalse(server.equals(null));

        assertFalse(client.equals(server));
        assertFalse(server.equals(client));

        assertFalse(handler1.equals(mockSocket));
        assertFalse(handler1.equals("foo"));
    }

    @Test
    public void testHashCode() throws IOException
    {
        Socket mockSocket = mock(Socket.class);
        ProtocolHandler handler1 = new ProtocolHandler(mockSocket);
        ProtocolHandler handler2 = new ProtocolHandler(mockSocket);

        assertEquals(handler1.hashCode(), handler2.hashCode());
    }

    private static void testSendMessage(ProtocolHandler from, ProtocolHandler to,
            Message message) throws LostConnectionException
    {
        from.sendMessage(message);
        ProtocolEvent event = to.receiveEvent();
        assertEquals(event.getMessage(), message);
        assertFalse(event.getMove().isPresent());
    }
}
