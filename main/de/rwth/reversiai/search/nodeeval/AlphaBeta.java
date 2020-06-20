package de.rwth.reversiai.search.nodeeval;

import de.rwth.reversiai.game.Player;

import java.util.HashMap;

/**
 * AlphaBeta offers functions for using an alphabeta evaluation structure for nodes.
 * <p>
 * ATTENTION: AlphaBeta becomes a heuristic approach if the underlying NodeEval is not a total order!
 */
public class AlphaBeta
{
    /**
     * The alpha value is the best NodeEval from the perspective of a single player that she is able
     * to reach for sure.
     */
    private NodeEval alpha;

    /**
     * Beta is to be considered as a set of best possible NodeEvals from the perspective of all players who
     * have already made a move in the current game tree path. Since the players are able to enforce the
     * respective beta evaluations, one is allowed to prune if the alpha is worse for one of the players
     * contributing to the beta set.
     */
    private HashMap< Player, NodeEval > beta;

    /**
     * Constructor for creating a new alpha beta evaluation with empty beta.
     *
     * @param alpha the alpha evaluation
     */
    public AlphaBeta( NodeEval alpha )
    {
        assert alpha != null;

        this.alpha = alpha;
        this.beta = new HashMap<>();
    }

    /**
     * Constructor for creating a new alpha beta evaluation with a beta set.
     *
     * @param alpha the alpha evaluation
     * @param beta  the beta evaluations
     */
    public AlphaBeta( NodeEval alpha, HashMap< Player, NodeEval > beta )
    {
        assert alpha != null;
        assert beta != null;

        this.alpha = alpha;
        this.beta = beta;
    }

    /**
     * Takes the current beta list and adds the alpha value if it is better than the current beta value
     * of the players with the same strategy as the turn (i.e. alpha) player.
     *
     * @param turn the player related to the alpha NodeEval
     * @return a HashMap containing all relevant beta values for successor nodes
     */
    public HashMap< Player, NodeEval > calcSucBeta( Player turn )
    {
        HashMap< Player, NodeEval > newBeta = (HashMap< Player, NodeEval >) this.beta.clone();
        boolean found = false;

        for ( Player p : newBeta.keySet() )
        {
            if ( this.alpha.haveSameStrategy( turn, p ) )
            {
                if ( this.alpha.isBetter( newBeta.get( p ), p ) )
                {
                    newBeta.put( p, this.alpha );
                }
                found = true;
                break;
            }
        }

        if ( !found )
        {
            newBeta.put( turn, this.alpha );
        }

        return newBeta;
    }

    /**
     * Returns true if there exists a beta NodeEval in the beta list which is better for the respective player
     * than the current alpha. Only considers players with a strategy differing from the turn player's strategy.
     *
     * @param turn the turn (i.e. alpha) player
     * @return true if there exists a blocking beta
     */
    public boolean betaBlocker( Player turn )
    {
        for ( Player p : this.beta.keySet() )
        {
            if ( !this.alpha.haveSameStrategy( turn, p ) )
            {
                if ( this.beta.get( p ).isBetter( this.alpha, p ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @return alpha evaluation
     */
    public NodeEval getAlpha()
    {
        return this.alpha;
    }

    /**
     * @return set of beta evaluations
     */
    public HashMap< Player, NodeEval > getBeta()
    {
        return this.beta;
    }

    /**
     * @param player1 A player.
     * @param player2 Another player.
     * @return {@code true} if both players try to optimize the NodeEval in the same manner.
     */
    public boolean haveSameStrategy( Player player1, Player player2 )
    {
        return this.alpha.haveSameStrategy( player1, player2 );
    }

    /**
     * @param eval alpha evaluation
     */
    public void setAlpha( NodeEval eval )
    {
        assert eval != null;

        this.alpha = eval;
    }

    /**
     * @param eval set of beta evaluations
     */
    public void setBeta( HashMap< Player, NodeEval > eval )
    {
        assert eval != null;

        this.beta = eval;
    }
}
