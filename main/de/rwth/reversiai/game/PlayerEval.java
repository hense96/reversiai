package de.rwth.reversiai.game;

import java.util.HashMap;

/**
 * Simple class for encapsulating evaluations for players bases on {@link java.util.Map}.
 * Evaluations may be of arbitrary type.
 *
 * @author Julius Hense
 */
public class PlayerEval< E >
{
    /**
     * The PlayerPool containing players to be evaluated. This PlayerPool is the context
     * of the evaluation and should always contain all players that take part in the game,
     * but not subsets.
     */
    private final PlayerPool players;

    /**
     * Evaluation map that assigns a value of given type E to each player.
     */
    private final HashMap< Integer, E > eval;

    /**
     * Initially, no player has an evaluation.
     *
     * @param players {@code PlayerPool} containing all players that may be evaluated.
     */
    public PlayerEval( PlayerPool players )
    {
        assert players != null;

        this.players = players;

        this.eval = new HashMap<>();
        for ( Player player : this.players )
        {
            this.eval.put( (int) player.getID(), null );
        }
    }

    /**
     * @param player A player that potentially has an evaluation.
     * @return the player's evaluation, {@code null} if she does not have one.
     */
    public E getEval( Player player )
    {
        assert player != null;

        return this.getEval( player.getID() );
    }

    /**
     * @param playerID An ID of a player that potentially has an evaluation.
     * @return the player's evaluation, {@code null}  if she does not have one.
     */
    public E getEval( int playerID )
    {
        assert players.contains( playerID );

        return this.eval.get( playerID );
    }

    /**
     * @param player A player that potentially has an evaluation.
     * @return {@code true} if the player has an evaluation
     */
    public boolean hasEval( Player player )
    {
        assert player != null;

        return this.hasEval( player.getID() );
    }

    /**
     * @param playerID An ID of a player that may have an evaluation.
     * @return {@code true} if the player has an evaluation.
     */
    public boolean hasEval( int playerID )
    {
        assert players.contains( playerID );

        return this.eval.get( playerID ) != null;
    }

    /**
     * @param player A player to be evaluated.
     * @param eval   The evaluation.
     */
    public void setEval( Player player, E eval )
    {
        assert player != null;

        this.setEval( player.getID(), eval );
    }

    /**
     * @param playerID An ID of a player to be evaluated.
     * @param eval     The evaluation.
     */
    public void setEval( int playerID, E eval )
    {
        assert players.contains( playerID );

        this.eval.put( playerID, eval );
    }
}
