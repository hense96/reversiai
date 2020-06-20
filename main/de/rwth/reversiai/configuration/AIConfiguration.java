package de.rwth.reversiai.configuration;

import de.rwth.reversiai.search.SearchAlgorithm;
import de.rwth.reversiai.search.cutter.Cutter;
import de.rwth.reversiai.search.evaluator.Evaluator;
import de.rwth.reversiai.search.generator.Generator;
import de.rwth.reversiai.search.window.AspirationWindow;
import de.rwth.reversiai.time.TimeStrategy;

/**
 * An AI configuration maintains all relevant parameter for an AI.
 */
public class AIConfiguration
{
    /**
     * The desired search algorithm.
     */
    public final SearchAlgorithm searchAlgorithm;

    /**
     * The desired cutter.
     */
    public final Cutter cutter;

    /**
     * The desired evaluator.
     */
    public final Evaluator evaluator;

    /**
     * The desired generator.
     */
    public final Generator generator;

    /**
     * The desired aspiration window.
     */
    public final AspirationWindow window;

    /**
     * The desired time strategy.
     */
    public final TimeStrategy timeStrategy;

    /**
     * @param searchAlgorithm The desired search algorithm.
     * @param cutter          The desired cutter.
     * @param evaluator       The desired evaluator.
     * @param generator       The desired generator.
     * @param window          The desired aspiration window. This may also be {@code null}.
     * @param timeStrategy    The desired time strategy.
     */
    public AIConfiguration(
            SearchAlgorithm searchAlgorithm, Cutter cutter, Evaluator evaluator, Generator generator,
            AspirationWindow window, TimeStrategy timeStrategy )
    {
        this.searchAlgorithm = searchAlgorithm;
        this.cutter = cutter;
        this.evaluator = evaluator;
        this.generator = generator;
        this.window = window;
        this.timeStrategy = timeStrategy;
    }

    /**
     * @return a textual representation of the AI configuration.
     */
    public String toString()
    {
        return String.format(
                "Search Algorithm: %s\n" +
                "Cutter: %s\n" +
                "Evaluator: %s\n" +
                "Generator: %s",
                this.searchAlgorithm,
                this.cutter,
                this.evaluator,
                this.generator
        );
    }
}
