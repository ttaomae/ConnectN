# ConnectN
This is a clone of [Connect Four](http://en.wikipedia.org/wiki/Connect_Four), with adjustable board size and win condition.

It was developed using test-driven development, so most non-GUI and non-network components are tested.

The GUI is built using JavaFX.

## Versions
Currently, there are two versions of this game. One is a stand-alone version and another is a network multiplayer version.

### Local / Stand-alone
The stand-alone version allows you to select whether each player will be a human or computer. A human player selects a move by clicking on the desired column on the board. The computer uses a minimax algorithm with alpha-beta pruning. The difficulty (search depth) can be selected using the slider below the player select buttons.

The height, width, and win condition can be adjusted using the sliders to the left, bottom, and top of the board, respectively. Be aware that increasing any of the parameters, especially the computer difficulty, can greatly increase the time (and CPU cycles) that the computer will need to select a move.

### Network Multiplayer
The network multiplayer version consists of two components: the client and the server.

When launching the server you must specify a port number. The server will continuously accept connections and start a match whenever there is at least two players connected (and not in a game).

The client must connect to the server by specifying a host and port. The client will be notified when a game is starting. The player can select moves by clicking on the desired column. After a game has finished the player will be asked if they want a rematch. If both players agree, they will start another game. Otherwise they will be added back to the player pool and, when possible, will be matched up with another player that is not the one that they have most recently played.

# Building
This project requires Java 11+ and [Maven](http://maven.apache.org/) to build.

## Modules
This project consists of four modules: `core`, `local`, `client`, and `server`.

The `core` module contains components that may be shared by other modules. This includes components such as the board and players, as well as certain GUI components. It also contains the network protocol used by the client and server.

The `local` module contains the code for the stand-alone version.

The `client` and `server` modules contain code for the network multiplayer version.

## Testing & Verification
To run the tests, navigate to the root directory and run the following command:
```
> mvn test
```

To perform static analysis with [PMD](https://pmd.github.io/) and [FindBugs](https://spotbugs.github.io/), you can use the following command:
```
> mvn verify
```

## Packaging
To compile and package the project, navigate to the root directory of the project and type the following command:
```
> mvn package
```

This will create a `target/` directory in each of the modules.

If you wish to build only a specific module, you must first install the `core` module into your local Maven repository by navigating into the `core/` directory and using the following command:
```
> mvn install
```

# Running
The `*-with-dependencies.jar` files found in the `<module-name>/target/` directories should include everything they need to run on their own. They can be launched using the following command:

```
> java -jar connectn-<module>-<version>-with-dependencies.jar
```

The `server` module requires an additional argument specifying the port number.
```
> java -jar connectn-server-<version>-jar-with-dependencies <port_number>
```

Once you have started the server you can launch a client and connect to the server. If you are running the client and server from your own machine, you can use `localhost` or `127.0.0.1` as the host and whatever port number you used to launch the server.
