# ConnectN
This is a clone of [Connect Four](http://en.wikipedia.org/wiki/Connect_Four), with adjustable board size and win condition.

It was developed using test-driven development, so all non-GUI components are tested.
The GUI is built using JavaFX.

It allows you to select whether each player will be a human or computer. A human player selects a move by clicking on the desired column on the board. The computer uses a minimax algorithm with alpha-beta pruning. The difficulty (search depth) can be selected using the slider below the player select buttons.

The height, width, and win condition can be adjusted using the sliders to the left, bottom, and top of the board, respectively. Be aware that increasing any of the parameters, especially the computer difficulty, can greatly increase the time (and CPU cycles) that the computer will need to select a move.

# Build
This project is built using [Maven](http://maven.apache.org/).

To compile the project, navigate to the root directory of the project and type the following command:
```
mvn compile
```

To run the tests, type the following command:
```
mvn test
```

To build an executable jar file, type the following command:
```
mvn jfx:jar
```

The resulting jar file will be in `target/jfx/app/`.
