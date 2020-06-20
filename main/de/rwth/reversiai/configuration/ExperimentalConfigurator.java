package de.rwth.reversiai.configuration;

import de.rwth.reversiai.search.SearchAlgorithm;
import de.rwth.reversiai.search.cutter.SimpleBombDepthCutter;
import de.rwth.reversiai.search.evaluator.FastDROTEvaluator;
import de.rwth.reversiai.search.generator.SimpleGenerator;
import de.rwth.reversiai.time.PredictiveTimeStrategy;

/**
 * Configurator creating an experimental configuration.
 */
public class ExperimentalConfigurator extends Configurator
{
    @Override
    protected void configure()
    {
        this.searchAlgorithm = SearchAlgorithm.AlphaBetaPruning;
        this.cutter = new SimpleBombDepthCutter( 1 );
        this.evaluator = new FastDROTEvaluator();
        this.generator = new SimpleGenerator();
        this.window = null;
        this.timeStrategy = new PredictiveTimeStrategy( 1 );
    }
}
