package de.rwth.reversiai.search.evaluator;

import de.rwth.reversiai.game.Player;
import de.rwth.reversiai.search.GameTree;
import de.rwth.reversiai.search.nodeeval.ParanoidDouble;

/**
 * This evaluator is just a dummy evaluator to cache the state evaluation on which basis the move ordering done in
 * {@link de.rwth.reversiai.search.generator.RestrictedBombGenerator}.
 */
public class RestrictedBombEvaluator extends Evaluator< ParanoidDouble >
{
    @Override
    public ParanoidDouble evaluate( GameTree.Node node )
    {
        throw new UnsupportedOperationException( "This is just a dummy class!" );
    }

    @Override
    public ParanoidDouble getLowerBound( Player player )
    {
        throw new UnsupportedOperationException( "This is just a dummy class!" );
    }

    @Override
    public ParanoidDouble getUpperBound( Player player )
    {
        throw new UnsupportedOperationException( "This is just a dummy class!" );
    }
}
