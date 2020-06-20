package de.rwth.reversiai.search.cutter;

import de.rwth.reversiai.search.GameTree;
import de.rwth.reversiai.search.evaluator.Evaluator;
import de.rwth.reversiai.search.nodeeval.NodeEval;
import de.rwth.reversiai.search.nodeeval.ParanoidDouble;

/**
 * Best Double Eval Delta Cutter. Firstly, it evaluates a node with the passed evaluator's method. It cuts off
 * a node if this evaluation is at least delta worse than the best reachable value for the MAX player (i.e.
 * MAX player's beta value). This evaluation is only done up to a passed depth.
 * <p>
 * Only usable for alphabeta. For safety reasons, it simply performs simple bomb depth cutting if MiniMax is used.
 * Only usable for ParanoidDouble NodeEval.
 */
public class BDEDCutter extends SimpleBombDepthCutter
{
    private int evalDepth;
    private double delta;
    private Evaluator< ParanoidDouble > evaluator;

    /**
     * @param maxDepth  the hard cutoff depth
     * @param evalDepth the maximum depth where a node should still be evaluated
     * @param delta     the accepted delta between best known evaluation and the node's evaluation
     * @param evaluator the evaluator (recommended: use the one used in GameTree)
     */
    public BDEDCutter( int maxDepth, int evalDepth, double delta, Evaluator< ParanoidDouble > evaluator )
    {
        super( maxDepth );

        assert evalDepth > 0;
        assert evalDepth < maxDepth;
        assert -1 <= delta && delta <= 1;
        assert evaluator != null;

        this.evalDepth = evalDepth;
        this.delta = delta;
        this.evaluator = evaluator;
    }

    @Override
    public boolean cutoff( GameTree.Node node )
    {
        if ( node.isRootNode() )
            return false;
        else if ( super.cutoff( node ) )
            return true;

        if ( node.getDepth() <= this.evalDepth && node.getParent().getAlphaBeta() != null )
        {
            NodeEval bestNodeEval = this.gameTree.getRoot().getAlphaBeta().getAlpha();

            assert bestNodeEval instanceof ParanoidDouble;

            double bestEval = ( (ParanoidDouble) bestNodeEval ).getValue();
            double prediction = 1;

            if ( bestEval >= this.delta )
            {
                prediction = this.evaluator.evaluate( node ).getValue();
            }

            return bestEval - this.delta >= prediction;
        }

        return false;
    }

}
