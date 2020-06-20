package de.rwth.reversiai.time;

import java.util.HashMap;
import java.util.Map;

/**
 * The time strategy class is an interface class for time strategies in iterative deepening. The time strategy
 * decides whether
 * <ul>
 * <li>the depth level in iterative deepening should be increased (and how much) and a new game tree calculation should
 * be performed in order to gain a better move <strong>or</strong></li>
 * <li>whether it is considered more useful to save time and return the currently best known move.</li>
 * </ul>
 */
public abstract class TimeStrategy
{
    /**
     * This depth is used as maximum depth for the first computed game tree of one move calculation.
     */
    protected final int minDepth;

    /**
     * The given maximum search tree depth for calculating a move. 0, if no depth limit applies.
     */
    protected int maxDepth = 0;

    /**
     * The given time limit for calculating a move. 0, if no time limit applies.
     */
    protected long timeLimit;

    /**
     * The duration of the last calculated move.
     */
    protected long moveDuration;

    /**
     * Current maximum search tree depth.
     */
    protected int currentDepth;

    /**
     * Mapping for the time needed for computing whole search trees with respective maximum depth.
     */
    protected Map< Integer, Long > depthDurationMap = new HashMap<>();

    /**
     * @param minDepth Maximum depth for the first computed game tree of one move calculation.
     */
    public TimeStrategy( int minDepth )
    {
        this.minDepth = minDepth;
        this.maxDepth = minDepth;

        this.timeLimit = 0L;
        this.moveDuration = 0L;
        this.currentDepth = minDepth - 1; // Sentinel depth
    }

    /**
     * @return The next search depth to consider. 0 if the time strategy recommends not to do further search.
     */
    public abstract int nextSearchDepth();

    /**
     * @param timeLimit The given time limit.
     */
    public void setTimeLimit( long timeLimit )
    {
        this.timeLimit = timeLimit;
    }

    /**
     * @param maxDepth The given maximum search depth.
     */
    public void setMaxDepth( int maxDepth )
    {
        this.maxDepth = maxDepth;
    }

    /**
     * @param duration The duration of the last calculated move.
     */
    public void setMoveDuration( long duration )
    {
        this.moveDuration = duration;
    }

    /**
     * Registers how long a game tree computation with a certain maximum search depth took.
     *
     * @param duration The duration of the computation.
     * @param depth    The maximum search depth of the computed game tree.
     */
    public void addComputationMetrics( long duration, int depth )
    {
        depthDurationMap.put( depth, duration );
    }

    /**
     * Clear all temporary data for the calculation of one move.
     */
    public void reset()
    {
        this.moveDuration = 0L;
        this.depthDurationMap.clear();
        this.currentDepth = this.minDepth - 1; // Sentinel depth
    }
}
