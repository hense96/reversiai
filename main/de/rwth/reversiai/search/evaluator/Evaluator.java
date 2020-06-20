package de.rwth.reversiai.search.evaluator;

import de.rwth.reversiai.game.Player;
import de.rwth.reversiai.search.GameTree;
import de.rwth.reversiai.search.nodeeval.NodeEval;

/**
 * An Evaluator should be able to calculate a NodeEval for a leaf node (and other Nodes). It furthermore
 * defines which kind of NodeEval should be used by setting type parameter E.
 *
 * @param <E> a type of NodeEval
 */
public abstract class Evaluator< E extends NodeEval >
{
    /**
     * The game tree the cutter currently operates on.
     */
    protected GameTree gameTree;

    /**
     * Evaluation method. Should be overriden in specializations.
     *
     * @param node The game tree node to be evaluated.
     * @return an evaluation.
     */
    public abstract E evaluate( GameTree.Node node );

    /**
     * @param player the perspective Player
     * @return worst possible evaluation from the perspective of a Player
     */
    public abstract E getLowerBound( Player player );

    /**
     * @param player the perspective Player
     * @return best possible evaluation from the perspective of a Player
     */
    public abstract E getUpperBound( Player player );

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
