# ConnectN
This is a clone of [Connect Four](http://en.wikipedia.org/wiki/Connect_Four), with adjustable board size and win condition.

It was developed using test-driven development, so most non-GUI and non-network components are tested.

The GUI is built using JavaFX.

## Versions
Currently, there are two versions of this game. One is a stand-alone version and another is a network multiplayer version.

### Stand-alone
The stand-alone version allows you to select whether each player will be a human or computer. A human player selects a move by clicking on the desired column on the board. The computer uses a minimax algorithm with alpha-beta pruning. The difficulty (search depth) can be selected using the slider below the player select buttons.

The height, width, and win condition can be adjusted using the sliders to the left, bottom, and top of the board, respectively. Be aware that increasing any of the parameters, especially the computer difficulty, can greatly increase the time (and CPU cycles) that the computer will need to select a move.

### Network Multiplayer
The network multiplayer version consists of two components: the client and the server.

When launching the server you must specify a port number. The server will continuously accept connections and start a match whenever there is at least two players connected (and not in a game).

The client must connect to the server by specifying a host and port. The client will be notified when a game is starting. The player can select moves by clicking on the desired column. After a game has finished the player will be asked if they want a rematch. If both players agree, they will start another game. Otherwise they will be added back to the player pool and, when possible, will be matched up with another player that is not the one that they have most recently played.

# Building
This project consists of three modules: core, client, and server.

The core module contains all the core components necessary for running the stand-alone version as well as components that may be shared by other modules. This included components such as the board and players, as well as GUI components. It also contains the network protocol used by the client and server.
The client and server modules both depend on the core module and contain only their respective code.

This project is built using [Maven](http://maven.apache.org/).

## Testing
To run the tests, navigate to the `core` directory and run the following command:
```
mvn test
```

## Packaging
To compile and package the project, navigate to the root directory of the project and type the following command:
```
mvn clean package
```

This will create several `target` directories (one for each module and one the in the root directory). The root `target` directory will contain all the executable JAR files as well as the core module packaged as a library.

# Running
The stand-alone and client JARs should include everything they need to run on their own. To launch them simply double-click their respective JAR files. You can copy or distribute these as-is.

The server on the other hand will output two files. One with and one without dependencies. In order to use the one without dependencies, you must include the `lib` directory which should include the `ConnectN-core-XX.jar` file. The file with dependencies can, as with the stand-alone and client JARs, be copied or distributed as-is.
To run the server, navigate to the directory with the JAR file and type the following command:
```
java -jar ConnectN-server-XX[-jar-with-dependencies].jar <port_number>
```

Once you have started the server you can launch a client and connect to the server. If you are running the client and server from your own machine, you can use `localhost` or `127.0.0.1` as the host and whatever port number you used to launch the server.
