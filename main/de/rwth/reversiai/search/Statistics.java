package de.rwth.reversiai.search;

import de.rwth.reversiai.util.LogTopic;
import de.rwth.reversiai.util.StatisticMath;

import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Class for collecting data to evaluate the performance of the algorithms.
 *
 * @author Roman Karwacik, Marvin Pförtner
 */
public class Statistics
{
    /**
     * Stores the time (in nanoseconds) it took to generate the successor nodes in the case of an interior node or the
     * time it took to evaluate the heuristics algorithm in the case of a leaf node.
     */
    private LinkedList< Long > computationTimes;

    /**
     * Stores the amount of successor nodes which have been explored (branching = total successor nodes - pruned
     * successor nodes) for each interior node.
     */
    private LinkedList< Integer > branching;

    /**
     * Stores the depth within the search tree for each leaf node.
     */
    private LinkedList< Integer > depths;

    /**
     * The starting timestamp of the search algorithm's computations (in milliseconds).
     */
    private long start;

    /**
     * The ending timestamp of the search algorithm's computations (in milliseconds).
     */
    private long end;

    /**
     * The average value of <code>computationTimes</code>.
     */
    private double averageTime;

    /**
     * The median of <code>computationTimes</code>
     */
    private double medianTime;

    /**
     * The average value of <code>branching</code>.
     */
    private double averageBranching;

    /**
     * The median of <code>branching</code>
     */
    private int medianBranching;

    /**
     * The average value of <code>depths</code>.
     */
    private double averageDepth;

    /**
     * The median of <code>depths</code>
     */
    private int medianDepth;

    /**
     * Number of aspiration window fails.
     */
    private int redos;

    /**
     * Initializes the start time as well as the other variables (duh)
     */
    public Statistics()
    {
        this.start = System.currentTimeMillis();
        this.computationTimes = new LinkedList<>();
        this.branching = new LinkedList<>();
        this.depths = new LinkedList<>();
        this.end = 0;
        this.averageTime = 0;
        this.averageBranching = 0;
    }

    /**
     * Calculates the average computation time in µs
     *
     * @return (previously) calculated average
     */
    private double getAverageComputationTimeMicro()
    {
        if ( this.averageTime == 0 )
        {
            this.averageTime = StatisticMath.arithmeticMean( this.computationTimes ) / 1000;
        }

        return this.averageTime;
    }

    /**
     * @return number of apsiration window fails.
     */
    public int getRedo()
    {
        return redos;
    }

    /**
     * Increase number of aspiration window fails by one.
     */
    public void addRedo()
    {
        redos++;
    }

    /**
     * Calculates the average branching
     *
     * @return (previously) calculated average
     */
    private double getAverageBranching()
    {
        if ( this.averageBranching == 0 )
        {
            this.averageBranching = StatisticMath.arithmeticMean( this.branching );
        }

        return this.averageBranching;
    }

    /**
     * Calculates the average depth
     *
     * @return (previously) calculated average
     */
    public double getAverageDepth()
    {
        if ( this.averageDepth == 0 )
        {
            this.averageDepth = StatisticMath.arithmeticMean( this.depths );
        }

        return this.averageDepth;
    }

    /**
     * Calculates the median branching (excluding leafs)
     *
     * @return (previously) calculated median
     */
    private int getMedianBranching()
    {
        if ( this.medianBranching == 0 )
        {
            this.medianBranching = StatisticMath.median( this.branching );
        }
        return this.medianBranching;
    }

    /**
     * Calculates the median depth
     *
     * @return (previously) calculated median
     */
    private int getMedianDepth()
    {
        if ( this.medianDepth == 0 )
        {
            this.medianDepth = StatisticMath.median( this.depths );
        }
        return this.medianDepth;
    }


    /**
     * Calculates the median computation time
     *
     * @return (previously) calculated median
     */
    private double getMedianComputationTimeMicro()
    {
        if ( this.medianTime == 0 )
        {
            this.medianTime = StatisticMath.median( this.computationTimes ) / (double) 1000;
        }
        return this.medianTime;
    }

    /**
     * @return total amount of processed states
     */
    private long getTotalProcessedStatesAmount()
    {
        return this.getLeafNodesAmount() + this.getInteriorNodesAmount();
    }

    /**
     * @return amount of leaf nodes
     */
    private long getLeafNodesAmount()
    {
        return this.depths.size();
    }

    /**
     * @return amount of interior nodes
     */
    private int getInteriorNodesAmount()
    {
        return this.branching.size();
    }

    /**
     * @return the time it took to run the search algorithm
     */
    private long getTotalComputationTimeMs()
    {
        return this.end - this.start;
    }

    /**
     * Signals the end of the search algorithm and stops the timer
     */
    void endComputation()
    {
        this.end = System.currentTimeMillis();
    }

    /**
     * Add data of a interior node of the game tree.
     *
     * @param successors      Number of successors that were explored starting from this node.
     * @param computationTime Calculation time for this node.
     */
    void addInteriorNode( int successors, long computationTime )
    {
        this.branching.add( successors );

        this.computationTimes.add( computationTime );
    }

    /**
     * Add data for a leaf node of the game tree.
     *
     * @param depth           Depth of the node.
     * @param computationTime Calculation time for this node.
     */
    void addLeafNode( int depth, long computationTime )
    {
        this.depths.add( depth );

        this.computationTimes.add( computationTime );
    }

    /**
     * @return A textual representation of the statistics.
     */
    public String toString()
    {
        String buffer = "Total Nodes                 : " +
                        this.getTotalProcessedStatesAmount() +
                        "\nInterior Nodes              : " +
                        this.getInteriorNodesAmount() +
                        "\nLeaf Nodes                  : " +
                        this.getLeafNodesAmount() +
                        "\nTotal ComputationTime (ms)  : " +
                        this.getTotalComputationTimeMs() +
                        "\nAverage ComputationTime (µs): " +
                        this.getAverageComputationTimeMicro() +
                        "\nAverage Branching           : " +
                        this.getAverageBranching() +
                        "\nAverage Depth               : " +
                        this.getAverageDepth() +
                        "\nMedian ComputationTime (µs) : " +
                        this.getMedianComputationTimeMicro() +
                        "\nMedian Branching            : " +
                        this.getMedianBranching() +
                        "\nMedian Depth                : " +
                        this.getMedianDepth();

        return buffer;
    }

    /**
     * @return A csv representation of the statistics.
     */
    public String toRawCSV()
    {
        StringBuilder buffer = new StringBuilder();

        buffer.append( "ComputationTime(µs),Branching,Depth" );

        for ( int i = 0; i < this.computationTimes.size() || i < this.branching.size() || i < this.depths.size(); i++ )
        {
            buffer.append( '\n' );
            buffer.append( this.computationTimes.size() > i ? this.computationTimes.get( i ) / 1000 : "" );
            buffer.append( ',' );
            buffer.append( this.branching.size() > i ? this.branching.get( i ) : "" );
            buffer.append( ',' );
            buffer.append( this.depths.size() > i ? this.depths.get( i ) : "" );
        }

        return buffer.toString();
    }

    /**
     * Exports all information as a simple to read csv-table.
     *
     * @param filename File to save csv in.
     */
    public void saveAsCSV( String filename )
    {
        try
        {
            FileOutputStream fos = new FileOutputStream( filename );
            fos.write( this.toRawCSV().getBytes() );
        }
        catch ( Exception e )
        {
            System.err.println( "Error while writing statistics file!\n" + e.toString() );
            e.printStackTrace();
        }
    }

    /**
     * Exports a list of statistics as a simple to read csv-table.
     *
     * @param stats    A list of statistics to be saved
     * @param filename File to save csv in.
     */
    public static void saveAsCSV( List< Statistics > stats, String filename )
    {
        StringBuilder buffer = new StringBuilder();

        buffer.append(
                "Total Nodes,Interior Nodes,Leaf Nodes,Total ComputationTime (ms),Average ComputationTime (µs)," +
                "Average Branching,Average Depth,Median ComputationTime (µs),Median Branching,Median Depth,Redos\n"
        );

        for ( Statistics s : stats )
        {
            buffer.append( s.getTotalProcessedStatesAmount() );
            buffer.append( ',' );
            buffer.append( s.getInteriorNodesAmount() );
            buffer.append( ',' );
            buffer.append( s.getLeafNodesAmount() );
            buffer.append( ',' );
            buffer.append( s.getTotalComputationTimeMs() );
            buffer.append( ',' );
            buffer.append( s.getAverageComputationTimeMicro() );
            buffer.append( ',' );
            buffer.append( s.getAverageBranching() );
            buffer.append( ',' );
            buffer.append( s.getAverageDepth() );
            buffer.append( ',' );
            buffer.append( s.getMedianComputationTimeMicro() );
            buffer.append( ',' );
            buffer.append( s.getMedianBranching() );
            buffer.append( ',' );
            buffer.append( s.getMedianDepth() );
            buffer.append( ',' );
            buffer.append( s.getRedo() );
            buffer.append( '\n' );
        }

        try
        {
            FileOutputStream fos = new FileOutputStream( filename );
            fos.write( buffer.toString().getBytes() );
        }
        catch ( Exception e )
        {
            LogTopic.error.error( "Error while writing statistics file!" );
            LogTopic.error.errorStackTrace( e );
        }

    }
}
