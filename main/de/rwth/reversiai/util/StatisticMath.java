package de.rwth.reversiai.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Supporting class offering functions for standard statistical calculations.
 */
public class StatisticMath
{
    /**
     * @param values The values.
     * @param <N>    The number type.
     * @return the arithmetic mean of the given values.
     */
    public static < N extends Number > double arithmeticMean( Iterable< N > values )
    {
        long n = 0;
        double sum = 0;

        for ( Number value : values )
        {
            sum += value.doubleValue();
            n++;
        }

        return sum / n;
    }

    /**
     * @param values The values.
     * @param <T>    The data type (should be {@code Comparable}).
     * @return the median of the given values.
     */
    public static < T extends Comparable > T median( Collection< T > values )
    {
        ArrayList< T > copy = new ArrayList<>( values );

        Collections.sort( copy );

        return copy.get( copy.size() / 2 );
    }

    /**
     * @param values The values.
     * @param <N>    The number type.
     * @param mean   The arithmetic mean of the values.
     * @return the variance of the given values.
     */
    public static < N extends Number > double variance( Iterable< N > values, double mean )
    {
        return StatisticMath.covariance( values, mean, values, mean );
    }

    /**
     * @param values1 The first value set.
     * @param <N1>    The number type of the first value set.
     * @param mean1   The arithmetic mean of the first value set.
     * @param values2 The second value set.
     * @param <N2>    The number type of the second value set.
     * @param mean2   The arithmetic mean of the second value set.
     * @return the covariance of the given values.
     */
    public static < N1 extends Number, N2 extends Number > double covariance(
            Iterable< N1 > values1,
            double mean1,
            Iterable< N2 > values2,
            double mean2
    )
    {
        Iterator< N1 > it1 = values1.iterator();
        Iterator< N2 > it2 = values2.iterator();

        long n = 0;
        double sum = 0;

        while ( it1.hasNext() && it2.hasNext() )
        {
            sum += it1.next().doubleValue() * it2.next().doubleValue();
            n++;
        }

        return sum / n - mean1 * mean2;
    }

    /**
     * f(x) = res[0] + res[1] * x
     *
     * @param xValues The first value set.
     * @param yValues The second value set.
     * @param <N1>    The number type of the first value set.
     * @param <N2>    The number type of the second value set.
     * @return the regression coefficients as described above.
     */
    public static < N1 extends Number, N2 extends Number > double[] linearRegressionCoefficients(
            Iterable< N1 > xValues,
            Iterable< N2 > yValues
    )
    {
        double xMean = StatisticMath.arithmeticMean( xValues );
        double yMean = StatisticMath.arithmeticMean( yValues );

        double b =
                StatisticMath.covariance( xValues, xMean, yValues, yMean ) / StatisticMath.variance( xValues, xMean );

        double a = yMean - b * xMean;

        return new double[] { a, b };
    }
}
