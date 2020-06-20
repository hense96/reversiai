package de.rwth.reversiai.search.generator;

import de.rwth.reversiai.AI;
import de.rwth.reversiai.exceptions.TimerInterruptException;
import de.rwth.reversiai.search.GameTree;
import de.rwth.reversiai.search.evaluator.Evaluator;

import java.util.List;

/**
 * Generator that only generates a usergiven number or fraction of successor nodes. These nodes are the best-evaluated
 * nodes according to the given sortedGenerator.
 */
public class BestMovesOnlyGenerator extends Generator
{
    /**
     * The sorted generator this generator uses to decide which moves should be used.
     */
    private SortedGenerator sortedGenerator;

    /**
     * The minimum number of successor nodes. Should at least be 1. This number is always enforced,
     * even if the percentage or delta induced number is smaller.
     */
    private int minSuccessors;

    /**
     * The maximum number of successor nodes.
     */
    private int maxSuccessors;

    /**
     * A percentage of all possible successor nodes to be created.
     */
    private double percentage;

    /**
     * A successor node is ignored if its evaluation is worse than best evaluation - delta.
     * Only use if you use ParanoidDouble node evaluation.
     */
    private double evalDelta;

    /**
     * @param evaluator                  An evaluator.
     * @param minSuccessors              Minimum number of successors (greater than 0).
     * @param maxSuccessors              Maximum number of successors (greater than 0).
     * @param percentage                 Percentage of successors to consider.
     * @param evalDelta                  <strong>Unsupported</strong>.
     * @param maxNumberOfMovesToConsider Maximum number of moves to consider.
     */
    public BestMovesOnlyGenerator(
            Evaluator evaluator, int minSuccessors, int maxSuccessors, double percentage, double evalDelta,
            int maxNumberOfMovesToConsider )
    {
        assert evaluator != null;
        assert minSuccessors > 0;
        assert maxSuccessors > 0;
        assert maxSuccessors >= minSuccessors;
        assert 0 < percentage && percentage <= 1;
        assert 0 <= evalDelta && evalDelta <= 1;

        this.sortedGenerator = new SortedGenerator( evaluator, maxNumberOfMovesToConsider );
        this.minSuccessors = minSuccessors;
        this.maxSuccessors = maxSuccessors;
        this.percentage = percentage;
        this.evalDelta = evalDelta;
    }

    @Override
    public List< GameTree.Node > generateSuccessors( GameTree.Node node ) throws TimerInterruptException
    {
        List< GameTree.Node > sortedList = sortedGenerator.generateSuccessors( node );

        assert !sortedList.isEmpty();

        int succs = sortedList.size();
        int percLimit = (int) ( succs * percentage );
        int deltaLimit = succs;

        /* TODO check deltaLimit if ParanoidDouble is used */

        int limit = this.maxSuccessors;
        if ( limit > percLimit )
            limit = percLimit;
        if ( limit > deltaLimit )
            limit = deltaLimit;
        if ( limit < this.minSuccessors )
            limit = this.minSuccessors;

        AI.checkDeadline();

        for ( int i = succs - 1; i >= limit; --i )
            sortedList.remove( limit );

        return sortedList;
    }
}
