# ConnectN Network Protocol
The server and clients communicate by sending String messages to each other. The messages are constants defined in `ttaomae.connectn.network.ConnectNProtocol.java`. In this document these constants will be shown in **`THIS_FONT`**. Each message is terminated with a newline, which can be either a line feed, a carriage return, or a carriage return followed immediately by a line feed.

## Pinging Clients
The server may send a **`PING`** to the client on certain occassions. If the client does not respond, the connection will be closed.

When a client is not in a game, the server will **`PING`** the client every **`PING_INTERVAL`** milliseconds. The server may also ping the client during a game. If one client does not respond, the opponent will be sent a **`DISCONNECTED`** message.

## Gameplay
When the server matches up two clients (players) it will begin a series of games. At the start of each game, a **`START`** message is sent to both players. During each turn the server will send a **`READY`** message to the player whose turn it is. The player should respond with a **`MOVE`** message. **`MOVE`** messages are unique in that they also contain additional information. In order to construct a proper **`MOVE`** message, the client should use the ConnectNProtocol.constructMove method. When a player sends a **`MOVE`** to the server, the server will send the same **`MOVE`** to the opponent. If a game ends successfully (as opposed to ending when a player disconnects), both players are sent a **`REMATCH`** message. The players should respond with either a **`YES`** or **`NO`** message. If both players respond with a **`YES`**, the server will send another START message to both players to indicate the start of another game. This process will repeat until one or both players disconnect or respond with a **`NO`**.
