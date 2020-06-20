package de.rwth.reversiai.search.cutter;

import de.rwth.reversiai.search.GameTree;

/**
 * Cuts off nodes when they are at a certain depth in the game tree.
 * Cuts off immediately when a state reached bombing phase.
 */
public class SimpleBombDepthCutter extends Cutter
{
    public SimpleBombDepthCutter( int maxDepth )
    {
        super( maxDepth );
    }

    @Override
    public boolean cutoff( GameTree.Node node )
    {
        return super.cutoff( node ) || ( !node.isRootNode() && node.getState().isBombingPhase() );
    }
}
