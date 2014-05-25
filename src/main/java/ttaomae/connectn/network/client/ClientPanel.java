package ttaomae.connectn.network.client;

import java.io.IOException;

import javafx.scene.layout.GridPane;
import ttaomae.connectn.Board;
import ttaomae.connectn.Player;
import ttaomae.connectn.gui.BoardPanel;
import ttaomae.connectn.gui.MousePlayer;

public class ClientPanel extends GridPane
{
    public ClientPanel(String hostname, int portNumber) throws IOException
    {
        Board b = new Board();
        BoardPanel bp = new BoardPanel(450, 400, b);
        this.add(bp, 0, 0);
        Player mp = new MousePlayer(bp);
        Thread myThread = new Thread(new Client(hostname, portNumber, mp, b));
        myThread.setDaemon(true);
        myThread.start();
    }
}
