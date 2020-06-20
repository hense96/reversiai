package de.rwth.reversiai.search.generator;

import de.rwth.reversiai.exceptions.TimerInterruptException;
import de.rwth.reversiai.search.GameTree;
import de.rwth.reversiai.search.evaluator.Evaluator;

import java.util.List;

/**
 * Combines {@link RestrictedBombGenerator} with {@link BestMovesOnlyGenerator}.
 */
public class RestrictedGenerator extends Generator
{
    private BestMovesOnlyGenerator stoneGenerator;
    private RestrictedBombGenerator bombGenerator;

    /**
     * @param evaluator                  This evaluator's evaluations are the basis for sorting the states.
     * @param maxNumberOfMovesToConsider Maximum number of moves that should be taken into consideration.
     */
    public RestrictedGenerator( Evaluator evaluator, int maxNumberOfMovesToConsider )
    {
        this.stoneGenerator = new BestMovesOnlyGenerator( evaluator, 1, 20,
                                                          0.5, 1.0, maxNumberOfMovesToConsider );
        this.bombGenerator = new RestrictedBombGenerator( maxNumberOfMovesToConsider );
    }

    @Override
    public List< GameTree.Node > generateSuccessors( GameTree.Node node ) throws TimerInterruptException
    {
        if ( !node.getState().isBombingPhase() )
            return stoneGenerator.generateSuccessors( node );
        else
            return bombGenerator.generateSuccessors( node );
    }
}
