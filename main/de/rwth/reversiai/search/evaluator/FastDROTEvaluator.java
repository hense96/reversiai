package de.rwth.reversiai.search.evaluator;

import de.rwth.reversiai.game.Player;
import de.rwth.reversiai.heuristics.FastDROTHeuristics;
import de.rwth.reversiai.search.GameTree;
import de.rwth.reversiai.search.nodeeval.NodeEval;
import de.rwth.reversiai.search.nodeeval.ParanoidDouble;

public class FastDROTEvaluator extends Evaluator
{
    @Override
    public NodeEval evaluate( GameTree.Node node )
    {
        return new ParanoidDouble(
                FastDROTHeuristics.evaluate(
                        node.getState(),
                        this.gameTree.getMaxPlayer(),
                        1, 1, 1, 1 ),
                this.gameTree.getMaxPlayer()
        );
    }

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
