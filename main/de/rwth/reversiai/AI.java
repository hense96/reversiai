package de.rwth.reversiai;

import de.rwth.reversiai.configuration.AIConfiguration;
import de.rwth.reversiai.exceptions.TimerInterruptException;
import de.rwth.reversiai.game.State;
import de.rwth.reversiai.move.Move;
import de.rwth.reversiai.search.GameTree;
import de.rwth.reversiai.search.SearchAlgorithm;
import de.rwth.reversiai.search.Statistics;
import de.rwth.reversiai.util.LogTopic;

import java.util.LinkedList;
import java.util.List;

/**
 * This class is the coordinating class making use of heuristic and deterministic approaches to repeatedly calculate
 * the best moves throughout a whole game according to its configuration. One AI belongs to exactly one player.
 * An AI should always be kept up to date regarding the current game state.
 */
public class AI
{
    /**
     * The AI maintains the current game state.
     */
    private State state;

    /**
     * The ID of the player the AI belongs to.
     */
    private final byte playerID;

    /**
     * The configuration of the AI.
     */
    private final AIConfiguration configuration;

    /**
     * The best move for the current state that is currently known by the AI.
     */
    private Move bestMove;

    /**
     * Flag indicating whether statistical data should be collected.
     */
    private static boolean statsEnabled = false;

    /**
     * List of statistics for the game trees of the current move calculation.
     */
    private List< Statistics > statistics;

    /**
     * The point of time when the current calculation of a move should stop.
     */
    private static long interruptTime = Long.MAX_VALUE;


    /**
     * @param state           The current/initial game state.
     * @param playerID        The ID of the player the AI should belong to.
     * @param AIConfiguration The configuration of the AI.
     */
    public AI( State state, byte playerID, AIConfiguration AIConfiguration )
    {
        this.state = state;
        this.playerID = playerID;
        this.configuration = AIConfiguration;
        this.statistics = new LinkedList<>();
    }

    /**
     * @param state The current game state.
     */
    public void setState( State state )
    {
        this.state = state;
    }

    /**
     * @return the current game state that the AI knows.
     */
    public State getState()
    {
        return this.state;
    }

    /**
     * @param timeLimit The time limit for the computation in nanoseconds or 0 if no limit applies.
     * @param maxDepth  The depth limit for the computation or 0 if no depth limit applies.
     * @param threshold Multiplier to reduce the time used in the algorithm to prevent timeouts if the interruption
     *                  takes too long.
     * @return the best move according to the given configuration.
     */
    public Move computeBestMove( long timeLimit, int maxDepth, double threshold )
    {
        long start = System.nanoTime();

        // Discard the previous results and set current best move to security move
        this.bestMove = this.getSecurityMove();

        if ( this.configuration.searchAlgorithm == SearchAlgorithm.FirstPossibleMove )
        {
            return this.bestMove;
        }

        if ( AI.statisticsEnabled() )
            this.statistics.clear();

        // Pass move requirements to time strategy
        this.configuration.timeStrategy.setTimeLimit( timeLimit );
        this.configuration.timeStrategy.setMaxDepth( maxDepth );

        // If there is a time limit, set a watchdog timer to interrupt the computation if necessary
        if ( timeLimit != 0 )
        {
            AI.interruptTime = System.nanoTime() + (long) ( threshold * timeLimit - 500000000 );

            long duration = (long) ( threshold * timeLimit );

            LogTopic.deepening.log(
                    "Will interrupt computation after %d ms",
                    (long) ( ( threshold * timeLimit - 500000000 ) / 1000000 )
            );
        }
        else
        {
            AI.interruptTime = Long.MAX_VALUE;
        }

        long tic, toc;

        /* get initial search depth from the time strategy */
        int searchDepth = this.configuration.timeStrategy.nextSearchDepth();

        GameTree gameTree;

        /* perform iterative deepening according to the time strategy */
        while ( searchDepth > 0 )
        {
            tic = System.nanoTime();

            /* communicate new search depth to the used cutter */
            this.configuration.cutter.setMaxDepth( searchDepth );

            /* initialize new game tree with the given parameters */
            gameTree = new GameTree(
                    state,
                    this.configuration.cutter,
                    this.configuration.evaluator,
                    this.configuration.generator,
                    this.configuration.window
            );

            /* calculate best move according to chosen search strategy until the time limit is reached */
            try
            {
                switch ( this.configuration.searchAlgorithm )
                {
                    case Minimax:
                        this.bestMove = gameTree.doMinimax();
                        break;

                    case AlphaBetaPruning:
                        this.bestMove = gameTree.doAlphaBeta();
                        break;

                    case AspirationWindows:
                        this.bestMove = gameTree.doAspirationWindows();
                        break;

                    default:
                        throw new RuntimeException( "This should not happen!" );
                }
            }
            catch ( TimerInterruptException e )
            {
                LogTopic.deepening.log( "Timer interrupt on depth %d", searchDepth );

                LogTopic.deepening.log(
                        "Interrupted %.3f ms later than scheduled",
                        ( System.nanoTime() - AI.interruptTime ) / 1000000.0
                );

                break;
            }

            toc = System.nanoTime();

            LogTopic.deepening.log( "Done search of depth %d", searchDepth );

            /* communicate time data to time strategy */
            this.configuration.timeStrategy.setMoveDuration( toc - start );
            this.configuration.timeStrategy.addComputationMetrics( toc - tic, searchDepth );

            if ( AI.statisticsEnabled() )
                this.statistics.add( gameTree.getStatistics() );

            /* get next search depth according to time strategy */
            searchDepth = this.configuration.timeStrategy.nextSearchDepth();
        }

        /* reset time strategy and aspiration window */
        this.configuration.timeStrategy.reset();

        if ( this.configuration.window != null )
            this.configuration.window.reset();

        assert this.bestMove != null;

        return this.bestMove;
    }

    /**
     * Calculates a valid move very fast.
     *
     * @return a valid move
     */
    private Move getSecurityMove()
    {
        return this.state.getAllValidMoves().iterator().next();
    }

    /**
     * Checks whether the time limit for the current calculation is already reached.
     *
     * @throws TimerInterruptException If the time limit is reached.
     */
    public static void checkDeadline() throws TimerInterruptException
    {
        if ( System.nanoTime() >= AI.interruptTime )
        {
            LogTopic.deepening.log( "Interrupted at " + new Throwable().getStackTrace()[ 1 ].toString() );
            throw new TimerInterruptException();
        }
    }

    /**
     * Function to enable the collection of statistical data.
     */
    public static void enableStatistics()
    {
        AI.statsEnabled = true;
    }

    /**
     * Function to disable the collection of statistical data.
     */
    public static void disableStatistics()
    {
        AI.statsEnabled = false;
    }

    /**
     * @return {@code true} iff the collection of statistical data is enabled.
     */
    public static boolean statisticsEnabled()
    {
        return AI.statsEnabled;
    }

    /**
     * @return The list of statistics for the game trees of the current move calculation,
     * empty list if the collection of statistics is disabled.
     */
    public List< Statistics > getStatistics()
    {
        return this.statistics;
    }

    /**
     * @return The ID of the player belonging to this AI.
     */
    public byte getPlayerID()
    {
        return this.playerID;
    }
}
