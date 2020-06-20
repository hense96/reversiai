package de.rwth.reversiai.time;

import de.rwth.reversiai.util.LogTopic;
import de.rwth.reversiai.util.StatisticMath;

/**
 * The {@link PredictiveTimeStrategy} uses linear regression in order to estimate using the durations of previous
 * searches. The essence of the algorithm used is described in the report for assignment 5.
 */
public class PredictiveTimeStrategy extends TimeStrategy
{
    /**
     * Threshold value used to control how conservative the prediction will be
     */
    private double threshold = 1.0;

    /**
     * The predicted duration of the next computation
     */
    private double predictedDuration;

    /**
     * Constructs a {@link PredictiveTimeStrategy} with the given minimum search depth.
     *
     * @param minDepth The minimum search depth.
     */
    public PredictiveTimeStrategy( int minDepth )
    {
        super( minDepth );
    }

    /**
     * The search depth of the next search or 0 if the predicted execution time of the next search will exceed the time
     * limit.
     */
    @Override
    public int nextSearchDepth()
    {
        long expectedFinishTime = 0;

        // Linear regression needs at least two values
        if ( depthDurationMap.size() >= 2 )
        {
            // Calculate the linear regression coefficients of the logarithmed durations and the depths
            double[] coeffs = StatisticMath.linearRegressionCoefficients(
                    depthDurationMap.keySet(),
                    depthDurationMap.values()
            );

            double a = coeffs[ 0 ];
            double b = coeffs[ 1 ];

            // Estimate the duration using the coefficients
            double duration = Math.exp( a + threshold * b * ( this.currentDepth + 1 ) );

            this.predictedDuration = duration;

            expectedFinishTime = this.moveDuration + (long) duration;

            LogTopic.deepening.log( "Predicted execution time of %.0f µs", duration / 1000 );
        }

        // Check if the search on the next level will be possible
        if ( ( this.timeLimit == 0 || this.timeLimit > this.moveDuration )
             && ( this.maxDepth == 0 || this.maxDepth > this.currentDepth )
             && ( this.timeLimit > expectedFinishTime ) )
        {
            this.currentDepth++;

            return this.currentDepth;
        }

        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @param duration The duration of the computation.
     * @param depth    The maximum search depth of the computed game tree.
     */
    @Override
    public void addComputationMetrics( long duration, int depth )
    {
        // Linear regression needs at least two values
        if ( depth >= this.minDepth + 2 )
        {
            LogTopic.deepening.log(
                    "Execution time delta of %.0f µs",
                    ( this.predictedDuration - duration ) / 1000.0
            );
        }

        // We need the logarithm of the computation time
        super.addComputationMetrics( (long) Math.log( duration ), depth );
    }
}
