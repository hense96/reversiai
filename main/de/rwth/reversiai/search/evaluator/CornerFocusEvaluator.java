package de.rwth.reversiai.search.evaluator;

import de.rwth.reversiai.game.Player;
import de.rwth.reversiai.game.State;
import de.rwth.reversiai.heuristics.CornerStoneHeuristics;
import de.rwth.reversiai.heuristics.OTHeuristics;
import de.rwth.reversiai.heuristics.SimpleHeuristics;
import de.rwth.reversiai.search.GameTree;
import de.rwth.reversiai.search.nodeeval.ParanoidDouble;

/**
 * Evaluator assigning ParanoidDouble evaluations to Nodes.
 * It combines corner heuristics with OT heuristics.
 */
public class CornerFocusEvaluator extends Evaluator< ParanoidDouble >
{
    /**
     * The used heuristic classes.
     */
    private CornerStoneHeuristics cornerHeur;

    /**
     * Flag indicating whether the cornerHeur attribute is already instantiated.
     */
    private boolean heatMapBuilt;

    /**
     * Parameter: choose from [0,1]. A high value indicates that OT heuristics is more important (1 - only OT).
     * A low value indicates that there is a focus on corners (0 - only corners).
     */
    private double p1;

    /**
     * @param p1 Choose from [0,1]. A high value indicates that OT heuristics is more important (1 - only OT).
     *           A low value indicates that there is a focus on corners (0 - only corners).
     */
    public CornerFocusEvaluator( double p1 )
    {
        this.heatMapBuilt = false;
        this.p1 = p1;
    }

    @Override
    public ParanoidDouble evaluate( GameTree.Node node )
    {
        if ( !this.heatMapBuilt )
        {
            this.cornerHeur = new CornerStoneHeuristics( node.getState().getBoard() );
        }

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
                    SimpleHeuristics.relativeNumberOfBombableTiles( state, this.gameTree.getMaxPlayer(), 0.5 ),
                    this.gameTree.getMaxPlayer()
            );
        }
        else
        {
            double sum = p1 * OTHeuristics.relativeOTIndex( state, this.gameTree.getMaxPlayer(), 0.1, 8, 0 )
                         + ( 1 - p1 ) * this.cornerHeur
                    .heurRelativeWeightedCornerStones( state, this.gameTree.getMaxPlayer(), 0.2, 0.3 );

            eval = new ParanoidDouble(
                    sum,
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
