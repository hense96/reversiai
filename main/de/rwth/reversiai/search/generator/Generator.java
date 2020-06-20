package de.rwth.reversiai.search.generator;

import de.rwth.reversiai.exceptions.TimerInterruptException;
import de.rwth.reversiai.move.Move;
import de.rwth.reversiai.search.GameTree;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * A Generator is able to create successor Nodes of a node. These successor Nodes are ordered in a
 * LinkedList according to priorities. The first node is most important.
 */
public abstract class Generator
{
    /**
     * The game tree the cutter currently operates on.
     */
    protected GameTree gameTree;

    /**
     * The maximum number of moves that should be taken into consideration. By default, it is set to
     * infinity, but specializations may make use of this value. Especially, if this value is set one
     * can use {@code chooseRandomMoves( Set< Move > )} function to reduce the move set to the desired size.
     */
    protected int maxNumberOfMovesToConsider = Integer.MAX_VALUE;

    /**
     * The successor generation function. Should be overriden in specializations.
     *
     * @param node The node that one wants to calculate successor nodes for.
     * @return an ordered list of successor nodes.
     * @throws TimerInterruptException If the calculation runs into a time limit.
     */
    public abstract List< GameTree.Node > generateSuccessors( GameTree.Node node ) throws TimerInterruptException;

    /**
     * Registers the current game tree.
     *
     * @param gameTree The current game tree.
     */
    public final void attachToGameTree( GameTree gameTree )
    {
        this.gameTree = gameTree;
    }

    /**
     * Supporting function to reduce the move set to the given maximum number.
     *
     * @param moves Initial move set.
     * @return a reduced move set.
     */
    protected Set< Move > chooseRandomMoves( Set< Move > moves )
    {
        if ( moves.size() < this.maxNumberOfMovesToConsider )
        {
            return moves;
        }
        else
        {
            Set< Move > subset = new HashSet<>();
            Iterator< Move > it = moves.iterator();
            for ( int i = 0; i < this.maxNumberOfMovesToConsider; ++i )
            {
                subset.add( it.next() );
            }
            return subset;
        }
    }
}
