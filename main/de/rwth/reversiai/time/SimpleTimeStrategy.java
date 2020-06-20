package de.rwth.reversiai.time;

/**
 * The simple time strategy returns the next higher depth level as long as possible.
 */
public class SimpleTimeStrategy extends TimeStrategy
{
    /**
     * @param minDepth Maximum depth for the first computed game tree of one move calculation.
     */
    public SimpleTimeStrategy( int minDepth )
    {
        super( minDepth );
    }

    @Override
    public int nextSearchDepth()
    {
        if ( ( this.timeLimit == 0 || this.timeLimit > this.moveDuration )
             && ( this.maxDepth == 0 || this.maxDepth > this.currentDepth ) )
        {
            this.currentDepth++;

            return this.currentDepth;
        }

        return 0;
    }
}
