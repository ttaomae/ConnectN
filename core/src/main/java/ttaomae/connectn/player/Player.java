package ttaomae.connectn.player;

import java.util.Optional;

import ttaomae.connectn.ImmutableBoard;

/**
 * An interface of a Connect-N player.
 *
 * @author Todd
 *
 */
public interface Player
{
    /**
     * Return this Player's move for the specified Board. A move is the column
     * number that this Player selects.
     *
     * @param board the board being played on
     * @return the Player's move.
     */
    Optional<Integer> getMove(ImmutableBoard board);
}
