package de.rwth.reversiai.search.generator;

import de.rwth.reversiai.move.Move;
import de.rwth.reversiai.search.GameTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Generates successor nodes from all valid moves without considering ordering.
 */
public class SimpleGenerator extends Generator
{
    @Override
    public List< GameTree.Node > generateSuccessors( GameTree.Node node )
    {
        List< GameTree.Node > succNodes = new ArrayList<>();

        Set< Move > validMoves = node.getState().getAllValidMoves();

        for ( Move move : validMoves )
        {
            succNodes.add(
                    node.createSuccessorNode(
                            move.execute(),
                            move
                    )
            );
        }

        return succNodes;
    }
}
