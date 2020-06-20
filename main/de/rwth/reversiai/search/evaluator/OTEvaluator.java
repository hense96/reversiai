package de.rwth.reversiai.search.evaluator;

import de.rwth.reversiai.game.Player;
import de.rwth.reversiai.game.State;
import de.rwth.reversiai.heuristics.OTHeuristics;
import de.rwth.reversiai.heuristics.SimpleHeuristics;
import de.rwth.reversiai.search.GameTree;
import de.rwth.reversiai.search.nodeeval.ParanoidDouble;

/**
 * Evaluator assigning ParanoidDouble evaluations to Nodes.
 * It uses SimpleHeuristics heuristics considering (O) opportunities and (T) threats of the players' stones.
 */
public class OTEvaluator extends Evaluator< ParanoidDouble >
{
    @Override
    public ParanoidDouble evaluate( GameTree.Node node )
    {
        State state = node.getState();

        ParanoidDouble eval;

        if ( state.isOver() )
        {
            eval = new ParanoidDouble(
                    SimpleHeuristics.relativeRank( state, this.gameTree.getMaxPlayer() ),
                    this.gameTree.getMaxPlayer()
            );
        }
        else if ( state.isBombingPhase() )
        {
            eval = new ParanoidDouble(
                    SimpleHeuristics.relativeNumberOfBombableTiles( state, this.gameTree.getMaxPlayer(), 0 ),
                    this.gameTree.getMaxPlayer()
            );
        }
        else
        {
            eval = new ParanoidDouble(
                    OTHeuristics.relativeOTIndex( state, this.gameTree.getMaxPlayer(), 0, 4, 0 ),
                    this.gameTree.getMaxPlayer()
            );
        }

        return eval;
    }

    /**
     * @param player the perspective Player
     * @return 0 for max, 1 for min
     */
    @Override
    public ParanoidDouble getLowerBound( Player player )
    {
        if ( player.equals( this.gameTree.getMaxPlayer() ) )
        {
            return new ParanoidDouble( 0.0, this.gameTree.getMaxPlayer() );
        }
        else
        {
            return new ParanoidDouble( 1.0, this.gameTree.getMaxPlayer() );
        }
    }

    /**
     * @param player the perspective Player
     * @return 1 for max, 0 for min
     */
    @Override
    public ParanoidDouble getUpperBound( Player player )
    {
        if ( player.equals( this.gameTree.getMaxPlayer() ) )
        {
            return new ParanoidDouble( 1.0, this.gameTree.getMaxPlayer() );
        }
        else
        {
            return new ParanoidDouble( 0.0, this.gameTree.getMaxPlayer() );
        }
    }
}
