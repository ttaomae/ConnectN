# ConnectN Client-Server Protocol
This document describes the communication protocol used between the server and
clients.

## Messages
All data sent between the server and clients are interpreted as big-endian, 
4-byte integers. Unless otherwise specified, a single integer represents a
specific "message."

The value of a message represented by a given integer is defined by the
`ProtocolEvent.Message` enum. More specifically messages start at `0` and
increase in the order specified by the enum.

## Protocol
### Pinging Clients
At any time the server may send a `**PING**` message in order to test
the connection with the client. The client should ignore these messages.

If the server finds that a client is no longer connected and that client
was in the middle of a match, it will send the opponent an
`**OPPONENT_DISCONNECTED**` message.

### Playing a Game
When the server matches up two players to play each other, the server 
will send a `**START_GAME**` message to both players. On each turn, the
server will send a `**REQUEST_MOVE**` messages to the player whose turn
is next.The player should respond with a `**PLAYER_MOVE**` message 
followed immediately by their move, as an integer. When the server 
receives the move, it will send a `**OPPONENT_MOVE**` message, followed
by the move as an integer, to the opponent.

After a match has completed, the server will send a 
`**REQUEST_REMATCH**` message to both players. The players should 
respond with either a `**ACCEPT_REMATCH**` or a `**DENY_REMATCH**`
message. If both players accept the rematch, a new match will begin 
between the same players. This process will repeat until one of the 
players denies the rematch or disconnects. If one or both of the players deny
the rematch, they will both become eligible to be matched up with other players.
