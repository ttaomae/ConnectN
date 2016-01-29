package ttaomae.connectn.network;

import ttaomae.connectn.network.ProtocolEvent.Message;

public interface ProtocolListener
{
    void eventReceived(ProtocolEvent receivedEvent);

    void messageSent(Message sentMessage);

    void moveSent(Message moveMessage, int move);
}
