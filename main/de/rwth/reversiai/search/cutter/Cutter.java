package de.rwth.reversiai.search.cutter;

import de.rwth.reversiai.search.GameTree;

/**
 * A Cutter should be able to decide whether to cutoff a Node in a game tree in order to make it a leaf Node.
 * It must not cut off the root node and it must cut off nodes with terminal game states!
 */
public class Cutter
{
    /**
     * The game tree the cutter currently operates on.
     */
    protected GameTree gameTree;

    /**
     * The maximum depth until which the cut off has to be executed.
     */
    private int maxDepth;

    /**
     * @param maxDepth the maximum depth.
     */
    public Cutter( int maxDepth )
    {
        assert maxDepth > 0;

        this.maxDepth = maxDepth;
    }

    /**
     * The (default) cutoff function. Should be overriden in specializations.
     *
     * @param node The node to check.
     * @return {@code true} if the node should not be further explored, i.e. no successor nodes should be considered.
     */
    public boolean cutoff( GameTree.Node node )
    {
        assert node != null;

        return node.getDepth() >= this.maxDepth || node.getState().isOver();
    }

    /**
     * @param maxDepth Change maximum depth to this value.
     */
    public void setMaxDepth( int maxDepth )
    {
        this.maxDepth = maxDepth;
    }

    /**
     * @return maximum depth.
     */
    public int getMaxDepth()
    {
        return this.maxDepth;
    }

    /**
     * Registers the current game tree.
     *
     * @param gameTree The current game tree.
     */
    public final void attachToGameTree( GameTree gameTree )
    {
        this.gameTree = gameTree;
    }
}
