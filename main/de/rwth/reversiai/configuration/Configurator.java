package de.rwth.reversiai.configuration;

import de.rwth.reversiai.AI;
import de.rwth.reversiai.search.SearchAlgorithm;
import de.rwth.reversiai.search.cutter.Cutter;
import de.rwth.reversiai.search.cutter.SimpleBombDepthCutter;
import de.rwth.reversiai.search.evaluator.CornerFocusEvaluator;
import de.rwth.reversiai.search.evaluator.Evaluator;
import de.rwth.reversiai.search.generator.Generator;
import de.rwth.reversiai.search.generator.RestrictedGenerator;
import de.rwth.reversiai.search.window.AspirationWindow;
import de.rwth.reversiai.search.window.HardDeltaDoubleWindow;
import de.rwth.reversiai.time.PredictiveTimeStrategy;
import de.rwth.reversiai.time.TimeStrategy;
import de.rwth.reversiai.util.FileLogger;
import de.rwth.reversiai.util.LogTopic;

/**
 * The configurator initializes all relevant AI parameter, creates a new AI configuration and configures some static
 * aspects of the software system.
 * For creating other configurations, one can override this class and especially the {@code configure} method.
 */
public class Configurator
{
    /**
     * The desired search algorithm.
     */
    protected SearchAlgorithm searchAlgorithm;

    /**
     * The desired cutter.
     */
    protected Cutter cutter;

    /**
     * The desired evaluator.
     */
    protected Evaluator evaluator;

    /**
     * The desired generator.
     */
    protected Generator generator;

    /**
     * The desired aspiration window. This may also be {@code null}.
     */
    protected AspirationWindow window;

    /**
     * The desired time strategy.
     */
    protected TimeStrategy timeStrategy;

    /**
     * The path for writing logging data into a file.
     */
    protected String logPath = null;

    /**
     * Builds a new AI configuration with the parameter chosen in {@code configure} method.
     *
     * @return a new AI configuration that may be utilized to create a new {@code AI}.
     */
    public final AIConfiguration buildAIConfiguration()
    {
        this.configure();

        return new AIConfiguration(
                this.searchAlgorithm,
                this.cutter,
                this.evaluator,
                this.generator,
                this.window,
                this.timeStrategy
        );
    }

    /**
     * Initialize desired parameters here!
     */
    protected void configure()
    {
        this.searchAlgorithm = SearchAlgorithm.AspirationWindows;
        this.cutter = new SimpleBombDepthCutter( 1 );
        this.evaluator = new CornerFocusEvaluator( 0.3 );
        this.generator = new RestrictedGenerator( this.evaluator, 100 );
        this.window = new HardDeltaDoubleWindow( 0.3, 0.3, 2 );
        this.timeStrategy = new PredictiveTimeStrategy( 1 );
    }

    /**
     * Method to configure static aspects of the whole software system.
     */
    public void configureSystem()
    {
        for ( LogTopic logTopic : LogTopic.values() )
        {
            logTopic.infoStreams.add( System.out );
            logTopic.errorStreams.add( System.err );
        }

        FileLogger.setLogPath( logPath );

        AI.disableStatistics();
    }

    /**
     * @param logPath A path for writing logging data into a file.
     */
    public final void setLogPath( String logPath )
    {
        this.logPath = logPath;
    }
}
