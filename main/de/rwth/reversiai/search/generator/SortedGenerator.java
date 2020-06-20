package de.rwth.reversiai.search.generator;

import de.rwth.reversiai.AI;
import de.rwth.reversiai.exceptions.TimerInterruptException;
import de.rwth.reversiai.move.Move;
import de.rwth.reversiai.search.GameTree;
import de.rwth.reversiai.search.evaluator.Evaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Evaluates all successor states with the evaluator provided and orders the nodes in a way that the best evaluation
 * comes first according to the perspective of the turn player in the passed parent node.
 */
public class SortedGenerator extends Generator
{
    /**
     * The evaluator used for creating evaluations for ordering the nodes.
     */
    private Evaluator evaluator;

    /**
     * Constructor for registering a maximum number of moves to consider.
     *
     * @param evaluator                  This evaluator's evaluations are the basis for sorting the states.
     * @param maxNumberOfMovesToConsider A maximum number of moves to consider.
     */
    public SortedGenerator( Evaluator evaluator, int maxNumberOfMovesToConsider )
    {
        assert evaluator != null;
        assert maxNumberOfMovesToConsider > 0;

        this.evaluator = evaluator;
        this.maxNumberOfMovesToConsider = maxNumberOfMovesToConsider;
    }

    @Override
    public List< GameTree.Node > generateSuccessors( GameTree.Node node ) throws TimerInterruptException
    {
        assert node != null;

        List< GameTree.Node > sucNodes = new ArrayList<>();

        AI.checkDeadline();

        Set< Move > validMoves = this.chooseRandomMoves( node.getState().getAllValidMoves() );

        GameTree.Node sucNode;

        for ( Move move : validMoves )
        {
            AI.checkDeadline();

            sucNode = node.createSuccessorNode( move.execute(), move );

            AI.checkDeadline();

            sucNode.evaluate( this.evaluator );

            sucNodes.add( sucNode );
        }

        AI.checkDeadline();

        sucNodes.sort( ( GameTree.Node n1, GameTree.Node n2 ) -> -1 * n1.getEval( this.evaluator )
                                                                        .compareTo( n2.getEval( this.evaluator ),
                                                                                    node.getState().getTurnPlayer() ) );

        return sucNodes;
    }
}
