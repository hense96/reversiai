package de.rwth.reversiai.search;

import de.rwth.reversiai.AI;
import de.rwth.reversiai.exceptions.TimerInterruptException;
import de.rwth.reversiai.game.Player;
import de.rwth.reversiai.game.State;
import de.rwth.reversiai.move.Move;
import de.rwth.reversiai.search.cutter.Cutter;
import de.rwth.reversiai.search.evaluator.Evaluator;
import de.rwth.reversiai.search.generator.Generator;
import de.rwth.reversiai.search.nodeeval.AlphaBeta;
import de.rwth.reversiai.search.nodeeval.NodeEval;
import de.rwth.reversiai.search.window.AspirationWindow;
import de.rwth.reversiai.util.LogTopic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a game tree which is able to calculate a good move according to a search strategy.
 * It maintains objects of the internal class @{link GameTree.Node}.
 * <p>
 * Possible search strategies are:
 * <ul>
 * <li>MiniMax: create full game tree and derive the best evaluation for the root node player,</li>
 * <li>AlphaBeta: additionally prune a path if you already know that this path can not deliver
 * an evaluation better than the best known evaluation,</li>
 * <li>AspirationWindow: additionally use initial evaluation windows for AlphaBeta.</li>
 * </ul>
 * <p>
 * These search strategies make use of interface classes that allow registering different implmentations. Therefore,
 * these interfaces are considered to be parameters. Such parameters for the game tree are:
 * <ul>
 * <li>a {@link Cutter} that decides when to cut off the tree, i.e. not further exploring a path,</li>
 * <li>an {@link Evaluator} that evaluates states,</li>
 * <li>a {@link Generator} that generates successor nodes for a inner node in the game tree,</li>
 * <li>an {@link AspirationWindow} that creates initial windows for alpha beta search.</li>
 * </ul>
 */
public class GameTree
{
    /**
     * Root node, i.e. node containing the current game state.
     */
    private final Node root;

    /**
     * A Cutter that is able to decide whether a tree Node should be cut off, i.e. make it a leaf node.
     */
    private final Cutter cutter;

    /**
     * An Evaluator that is able to evaluate leaf Nodes and defines the type of NodeEval used.
     */
    private final Evaluator evaluator;

    /**
     * A Generator that is able to generate successor Nodes from a Node.
     */
    private final Generator generator;

    /**
     * A AspirationWindow that is able to generate initial windows for alpha beta search.
     * Since the aspiration window is only utilized in the according search strategy, it may also be {@code null}
     * if it is not needed.
     */
    private final AspirationWindow window;

    /**
     * The player who takes the MAX perspective of the search algorithms.
     */
    private final Player maxPlayer;

    /**
     * An instance of the <code>Statistics</code> class recording performance metrics on the current game tree.
     * {@code null} if the collection of statistical data is disabled.
     */
    private Statistics statistics = null;

    /**
     * @param initialState state for the root node
     * @param cutter       a cutter
     * @param evaluator    an evaluator
     * @param generator    a generator
     * @param window       an aspiration window ({@code null} allowed if not used)
     */
    public GameTree(
            State initialState, Cutter cutter, Evaluator evaluator, Generator generator, AspirationWindow window )
    {
        assert initialState != null;
        assert cutter != null;
        assert generator != null;
        assert evaluator != null;

        cutter.attachToGameTree( this );
        evaluator.attachToGameTree( this );
        generator.attachToGameTree( this );
        if ( window != null )
        {
            window.attachToGameTree( this );
        }

        this.root = new Node( initialState );
        this.cutter = cutter;
        this.evaluator = evaluator;
        this.generator = generator;
        this.window = window;
        this.maxPlayer = initialState.getTurnPlayer();
    }

    /**
     * Performs the Minimax search algorithm on the current game tree.
     *
     * @return The best possible move to execute. If the search exceeded the deadline or if there are no moves to be
     * executed, the search returns <code>null</code>.
     * @throws TimerInterruptException if the timelimit was reached
     */
    public Move doMinimax() throws TimerInterruptException
    {
        if ( AI.statisticsEnabled() )
            this.statistics = new Statistics();

        Move bestMove = this.root.doMinimax().getCreatingMove();

        if ( AI.statisticsEnabled() )
            this.statistics.endComputation();

        return bestMove;
    }

    /**
     * Performs the Alpha-Beta Pruning search algorithm.
     *
     * @return The best possible move to execute. If the search exceeded the deadline or if there are no moves to be
     * executed, the search returns <code>null</code>.
     * @throws TimerInterruptException if the timelimit was reached
     */
    public Move doAlphaBeta() throws TimerInterruptException
    {
        if ( AI.statisticsEnabled() )
            this.statistics = new Statistics();

        // Initialize alpha and beta values for root node
        this.root.setAlphaBeta( new AlphaBeta( evaluator.getLowerBound( root.getState().getTurnPlayer() ) ) );

        Move bestMove = this.root.doAlphaBeta().getCreatingMove();

        if ( AI.statisticsEnabled() )
            this.statistics.endComputation();

        return bestMove;
    }

    /**
     * Performs the Alpha-Beta Pruning search algorithm with Aspiration Windows based on evaluations found
     * in search trees with lower depths.
     *
     * @return The best possible move to execute. If the search exceeded the deadline or if there are no moves to
     * be executed, the search returns <code>null</code>.
     * @throws TimerInterruptException if the timelimit was reached
     */
    public Move doAspirationWindows() throws TimerInterruptException
    {
        assert window != null;

        if ( AI.statisticsEnabled() )
            this.statistics = new Statistics();
        Move bestMove;
        HashMap< Player, NodeEval > initialBeta;

        /* repeat search until a move is found */
        do
        {
            AI.checkDeadline();

            window.widen();

            if ( window.getWidth() > 1 )
            {
                if ( AI.statisticsEnabled() )
                    statistics.addRedo();
                LogTopic.deepening.log( "Redo search since aspiration window was too narrow." );
            }

            /* calculate an aspiration window */
            initialBeta = window.getWindow( this.root );

            /* initialize root alphabeta eval with aspiration window as initial beta eval */
            this.root.setAlphaBeta( new AlphaBeta( evaluator.getLowerBound( root.getState().getTurnPlayer() ),
                                                   initialBeta ) );

            AI.checkDeadline();

            bestMove = this.root.doAlphaBeta().getCreatingMove();
        }
        /* repeat search if there is no move found that fits in the window */
        while ( !this.isInWindow( this.root.getAlphaBeta().getAlpha(), initialBeta ) );

        /* communicate found evaluation on this current tree depth to aspiration window*/
        window.update( this.cutter.getMaxDepth(), this.root.getAlphaBeta().getAlpha() );

        if ( AI.statisticsEnabled() )
            this.statistics.endComputation();

        return bestMove;
    }

    /**
     * Supporting function to check whether an alpha evaluation is in a beta window.
     *
     * @param alpha An alpha evaluation.
     * @param beta  A beta evaluation, i.e. a window for the alpha evaluation.
     * @return true if the alpha evaluation fits in the given beta evaluation/window.
     */
    private boolean isInWindow( NodeEval alpha, HashMap< Player, NodeEval > beta )
    {
        assert alpha != null;
        assert beta != null;

        for ( Player p : beta.keySet() )
        {
            if ( beta.get( p ).isBetter( alpha, p ) )
            {
                return false;
            }
        }

        return true;
    }

    /**
     * @return the recorded statistics, null if the collection of statistical data is disabled.
     */
    public Statistics getStatistics()
    {
        return this.statistics;
    }

    /**
     * @return the root node.
     */
    public Node getRoot()
    {
        return this.root;
    }

    /**
     * @return the player who takes the MAX perspective of the search algorithms.
     */
    public Player getMaxPlayer()
    {
        return this.maxPlayer;
    }

    /**
     * A Node maintains all relevant state and tree data for calculating game trees.
     */
    public class Node
    {
        /**
         * The state a Node operates on.
         */
        private final State state;

        /**
         * The move that lead to the creation of that Node.
         */
        private final Move createdBy;

        /**
         * The parent node. {@code null} iff Node is the root Node.
         */
        private final Node parent;

        /**
         * List of successor Nodes. Needs to be created by a Generator.
         * Before that, successors is an empty list.
         */
        private List< Node > successors = new ArrayList<>();

        /**
         * The Node depth in the game tree.
         */
        private final int depth;

        /**
         * Flag that indicates whether the Node is pruned. Needs to be set during Alpha-Beta Pruning.
         * Pruned Nodes are usually not further considered or branched.
         */
        private boolean isPruned = false;

        /**
         * NodeEval cache for this node. Every NodeEval is indexed by the hash code of the evaluator that created
         * it.
         */
        private Map< Evaluator, NodeEval > evaluations = new HashMap<>( 5 );

        /**
         * The alpha and beta values for the Alpha-Beta Pruning Algorithm.
         */
        private AlphaBeta alphaBeta;

        /**
         * Constructor for creating a root node.
         *
         * @param state state of the node
         */
        private Node( State state )
        {
            assert state != null;

            this.state = state;
            this.parent = null;
            this.createdBy = null;
            this.depth = 0;
        }

        /**
         * Constructor for creating a successor node.
         *
         * @param state     the state to operate on
         * @param parent    the parent node
         * @param createdBy the move that created the state
         */
        private Node( State state, Node parent, Move createdBy )
        {
            assert state != null;
            assert parent != null;
            assert createdBy != null;

            this.state = state;
            this.parent = parent;
            this.createdBy = createdBy;
            this.depth = parent.getDepth() + 1;
        }

        /**
         * Needs to be called instead of a constructor as the inner class requires the context of a {@code GameTree}
         * object.
         *
         * @param state     The state the node should operate on.
         * @param createdBy The move the node was created by.
         * @return the resulting new node.
         */
        public Node createSuccessorNode( State state, Move createdBy )
        {
            return new Node( state, this, createdBy );
        }

        /**
         * @return the parent node
         */
        public Node getParent()
        {
            return this.parent;
        }

        /**
         * @return true if the node does not have a parent
         */
        public boolean isRootNode()
        {
            return this.parent == null;
        }

        /**
         * @return depth of the node in the search tree
         */
        public int getDepth()
        {
            return this.depth;
        }

        /**
         * @return the creating move
         */
        public Move getCreatingMove()
        {
            return this.createdBy;
        }

        /**
         * @return the state of the node
         */
        public State getState()
        {
            return this.state;
        }

        /**
         * Generates all successor nodes in a queue according to priorities (first successor is most important).
         *
         * @return an ordered list of successor nodes
         * @throws TimerInterruptException If the calculation runs into a time limit.
         */
        public List< Node > generateSuccessors() throws TimerInterruptException
        {
            this.successors = GameTree.this.generator.generateSuccessors( this );

            return this.getSuccessors();
        }

        /**
         * Successor states need to be generated using generateSuccessors( Generator ) at first.
         * Otherwise, this function will return an empty List.
         *
         * @return list of generated successor nodes
         */
        public List< Node > getSuccessors()
        {
            return this.successors;
        }

        /**
         * Clears the list of successors.
         */
        public void removeSuccessors()
        {
            this.successors = null;
        }

        /**
         * Evaluates the state of the node using heuristics and caches the value.
         *
         * @param evaluator an evaluator that is able to evaluate leaf nodes
         * @return the evaluation
         */
        public NodeEval evaluate( Evaluator evaluator )
        {
            if ( !this.evaluations.containsKey( evaluator.hashCode() ) )
            {
                this.evaluations.put( evaluator, evaluator.evaluate( this ) );
            }

            return this.getEval( evaluator );
        }

        /**
         * Evaluates the state of the node using the evaluator that has been passed to the <code>GameTree</code>
         * constructor and caches it.
         *
         * @return the evaluation
         */
        public NodeEval evaluate()
        {
            return this.evaluate( GameTree.this.evaluator );
        }

        /**
         * Returns the <code>NodeEval</code> that has been set by the given evaluator.
         *
         * @param evaluator An evaluator that has potentially evaluated the node.
         * @return the node evaluation calculated by this evaluator.
         */
        public NodeEval getEval( Evaluator evaluator )
        {
            return this.evaluations.get( evaluator );
        }

        /**
         * Manually set an evaluation and place it as if it had been set by the given evaluator.
         *
         * @param evaluator  An evaluator to evaluate the node.
         * @param evaluation The evaluation calculated by this evaluator.
         */
        public void setEvaluation( Evaluator evaluator, NodeEval evaluation )
        {
            this.evaluations.put( evaluator, evaluation );
        }

        /**
         * Sets an alpha beta evaluation.
         *
         * @param alphaBeta The alpha beta evaluation.
         */
        public void setAlphaBeta( AlphaBeta alphaBeta )
        {
            this.alphaBeta = alphaBeta;
        }

        /**
         * @return the alpha beta evaluation of the node.
         */
        public AlphaBeta getAlphaBeta()
        {
            return this.alphaBeta;
        }

        /**
         * @return true if the node was pruned by prune function
         */
        public boolean isPruned()
        {
            return this.isPruned;
        }

        /**
         * Function to prune a Node.
         */
        public void prune()
        {
            this.isPruned = true;
        }

        /**
         * Recursive helper method performing the Alpha-Beta Pruning search algorithm starting from this node in the
         * search tree.
         * <p>
         * The original algorithm is slightly modified here.
         * Instead of using one alpha value as best eval for a MAX player and one beta value as the best eval for
         * MIN players, alpha represents the best value for the desired player (i.e. our group) and beta is a collection
         * of best evaluations that may be reached by the other players. In the beta collection, there is always only at
         * most one best evaluation for each group of players with the same strategy. Therefore, the alpha beta generalization
         * works like the normal alpha beta when there are only MAX and MIN players (except for the fact that alpha is
         * always the best reachable value for the turn player, not only of MAX).
         * </p>
         *
         * @return The best successor node. If the search was interrupted or if the node does not have any successors,
         * the method returns <code>null</code>.
         * @throws TimerInterruptException If the timelimit was reached.
         */
        private Node doAlphaBeta() throws TimerInterruptException
        {
            // Checking whether a node should be a leaf might be computationally heavy, so check the deadline first
            AI.checkDeadline();

            // Check whether the node should be a leaf
            if ( GameTree.this.cutter.cutoff( this ) )
            {
                // Evaluating a node via heuristics is usually computationally heavy, so check the deadline first
                AI.checkDeadline();

                long tic = System.nanoTime();

                // Evaluate node
                this.setAlphaBeta( new AlphaBeta( this.evaluate() ) );

                long toc = System.nanoTime();

                if ( AI.statisticsEnabled() )
                    GameTree.this.statistics.addLeafNode( this.getDepth(), toc - tic );

                // Leaf nodes do not have any successors, so they don't have a best successor
                return null;
            }
            else
            {
                // Generating a node's successors is usually computationally heavy, so check the deadline first
                AI.checkDeadline();

                long tic = System.nanoTime();

                // Generate successor states
                this.generateSuccessors();

                AI.checkDeadline();

                long toc = System.nanoTime();

                int exploredBranches = 0;

                Player turn = this.getState().getTurnPlayer();

                Node bestSucc = this.getSuccessors().get( 0 );

                // Search for the best successor node
                branching:
                for ( Node succ : this.getSuccessors() )
                {
                    ++exploredBranches;

                    AI.checkDeadline();

                    // Derive new beta and calculate best successor alpha
                    succ.setAlphaBeta(
                            new AlphaBeta(
                                    GameTree.this.evaluator.getLowerBound( succ.getState().getTurnPlayer() ),
                                    this.alphaBeta.calcSucBeta( turn )
                            )
                    );

                    succ.doAlphaBeta();

                    AI.checkDeadline();

                    if ( !succ.isPruned() )
                    {
                        NodeEval succAlpha = succ.getAlphaBeta().getAlpha();

                        // Check whether the best value of this successor is better than current alpha
                        if ( succAlpha.isBetter( this.alphaBeta.getAlpha(), turn ) )
                        {
                            this.alphaBeta.setAlpha( succAlpha );
                            bestSucc = succ;

                            // Check whether there is a beta contradicting this new alpha
                            if ( this.alphaBeta.betaBlocker( turn ) )
                            {
                                this.prune();

                                break branching;
                            }
                        }
                    }
                    else if ( succ.getAlphaBeta().getAlpha()
                                  .haveSameStrategy( turn, succ.getState().getTurnPlayer() ) )
                    {
                        // If current player has same strategy as successor and successor is pruned, prune too
                        this.prune();

                        break branching;
                    }
                }

                if ( AI.statisticsEnabled() )
                {
                    GameTree.this.statistics.addInteriorNode( exploredBranches, toc - tic );

                    for ( int i = 0; i < this.getSuccessors().size() - exploredBranches; i++ )
                    {
                        GameTree.this.statistics.addLeafNode( this.getDepth() + 1, 0L );
                    }
                }

                this.removeSuccessors();

                AI.checkDeadline();

                return bestSucc;
            }
        }

        /**
         * Recursive helper method performing the Minimax search algorithm starting from this node in the game tree.
         *
         * @return The best successor node. If the search was interrupted or if the node does not have any successors,
         * the method returns <code>null</code>.
         * @throws TimerInterruptException if the timelimit was reached
         */
        private Node doMinimax() throws TimerInterruptException
        {
            // Checking whether a node should be a leaf might be computationally heavy, so check the deadline first
            AI.checkDeadline();

            // Check whether the node should be a leaf
            if ( GameTree.this.cutter.cutoff( this ) )
            {
                // Evaluating a node via heuristics is usually computationally heavy, so check the deadline first
                AI.checkDeadline();

                long tic = System.nanoTime();

                // Evaluate node
                this.evaluate();

                long toc = System.nanoTime();

                if ( AI.statisticsEnabled() )
                    GameTree.this.statistics.addLeafNode( this.getDepth(), toc - tic );

                // Leaf nodes do not have any successors, so they don't have a best successor
                return null;
            }
            else
            {
                // Generating a node's successors is usually computationally heavy, so check the deadline first
                AI.checkDeadline();

                long tic = System.nanoTime();

                // Generate successor nodes
                this.generateSuccessors();

                long toc = System.nanoTime();

                if ( AI.statisticsEnabled() )
                    GameTree.this.statistics.addInteriorNode( this.getSuccessors().size(), toc - tic );

                Node bestSucc = this.getSuccessors().get( 0 );

                // Search for the best successor node
                for ( Node succ : this.getSuccessors() )
                {
                    succ.doMinimax();

                    if ( succ.getEval( GameTree.this.evaluator ).isBetter( bestSucc.getEval( GameTree.this.evaluator ),
                                                                           this.getState().getTurnPlayer() ) )
                    {
                        bestSucc = succ;
                    }
                }

                // The current node has the same evaluation as its successor node
                this.setEvaluation( GameTree.this.evaluator, bestSucc.getEval( GameTree.this.evaluator ) );

                // Clean up
                this.removeSuccessors();

                return bestSucc;
            }
        }
    }
}
