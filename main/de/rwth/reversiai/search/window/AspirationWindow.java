package de.rwth.reversiai.search.window;


import de.rwth.reversiai.game.Player;
import de.rwth.reversiai.search.GameTree;
import de.rwth.reversiai.search.nodeeval.NodeEval;

import java.util.HashMap;

public abstract class AspirationWindow< E extends NodeEval >
{
    /**
     * The corresponding game tree.
     */
    protected GameTree gameTree;

    /**
     * A list of found evaluations for the maximum depth's of each game tree that has already been explored
     * during an iterative deepening run.
     */
    protected HashMap< Integer, E > evalList;

    /**
     * Depth of the last game tree that delivered a result.
     */
    protected int searchDepth = 0;

    /**
     * Counts how often getWindow is called for one search depth, i.e. search failed n-1 times if callcounter is n.
     * One can consider this attribute in the getWindow function to implement a successive widening of the window for the
     * case that it has been chosen too narrow, initially.
     */
    protected int callcounter = 0;

    public void attachToGameTree( GameTree gameTree )
    {
        this.gameTree = gameTree;
    }

    /**
     * Returns an initial beta evaluation for alpha beta search, i.e. an aspiration window.
     *
     * @param node the node the window is needed for
     * @return a hash map from which you can create an initial alpha beta evaluation
     */
    public abstract HashMap< Player, E > getWindow( GameTree.Node node );

    /**
     * @return the amount of calls of getWindow function without calling update
     */
    public int getWidth()
    {
        return this.callcounter;
    }

    /**
     * @return a window without bounds
     */
    protected HashMap< Player, E > openWindow()
    {
        return new HashMap<>();
    }

    /**
     * Resets data of the aspiration window. Call this function before the start of an iterative deepening iteration.
     */
    public void reset()
    {
        this.gameTree = null;
        this.evalList.clear();
        this.searchDepth = 0;
        this.callcounter = 0;
    }

    /**
     * Function to register an evaluation found in a game tree with given maximum search depth.
     *
     * @param depth     the current maximum search depth in iterative deepening
     * @param foundEval the evaluation found for this depth
     */
    public void update( int depth, E foundEval )
    {
        assert depth > this.searchDepth;
        assert foundEval != null;
        assert this.evalList != null;

        this.callcounter = 0;
        this.evalList.put( depth, foundEval );
        this.searchDepth = depth;
    }

    /**
     * Increments the call counter for getWindow function.
     */
    public void widen()
    {
        ++this.callcounter;
    }
}
