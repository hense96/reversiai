package de.rwth.reversiai.search.nodeeval;

import de.rwth.reversiai.game.Player;

/**
 * A NodeEval defines how to evaluate Nodes. NodeEvals of one type should be comparable from
 * the perspectives of different players.
 */
public abstract class NodeEval
{
    /**
     * @param eval        another NodeEval
     * @param perspective the perspective of the player
     * @return 1, if this NodeEval is better than eval,
     * 0, if this NodeEval is neither better nor worse than eval,
     * -1, if this NodeEval is worse than eval
     */
    public abstract int compareTo( NodeEval eval, Player perspective );

    /**
     * @param eval        another NodeEval
     * @param perspective the perspective of the player
     * @return {@code true} if this NodeEval is better than eval
     */
    public abstract boolean isBetter( NodeEval eval, Player perspective );

    /**
     * @param eval        another NodeEval
     * @param perspective the perspective of the player
     * @return {@code true} if this NodeEval is neither better nor worse than eval
     */
    public abstract boolean isEqual( NodeEval eval, Player perspective );

    /**
     * @param eval        another NodeEval
     * @param perspective the perspective of the player
     * @return {@code true} if this NodeEval is worse than eval
     */
    public abstract boolean isWorse( NodeEval eval, Player perspective );

    /**
     * @param player1 a player
     * @param player2 another player
     * @return {@code true} if both players try to optimize the NodeEval in the same manner
     */
    public abstract boolean haveSameStrategy( Player player1, Player player2 );
}
