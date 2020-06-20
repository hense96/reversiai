package de.rwth.reversiai.heuristics;

import de.rwth.reversiai.board.Board;
import de.rwth.reversiai.board.Direction;
import de.rwth.reversiai.board.iterator.BoardIterator;
import de.rwth.reversiai.board.iterator.RayIterator;
import de.rwth.reversiai.game.Player;
import de.rwth.reversiai.game.PlayerEval;
import de.rwth.reversiai.game.PlayerPool;
import de.rwth.reversiai.game.State;

import java.util.HashMap;
import java.util.HashSet;

/**
 * This class is a heuristic class that contains heuristic functions that store data for corner stones.
 * Every heuristic function rates a situation (given by a node or a state) from the perspective of a player.
 */
public class CornerStoneHeuristics
{
    /**
     * Set of corner tiles, which is calculated in the constructor.
     * Corner tiles are considered to be positions on the map that may never be captured without using
     * an override stone.
     */
    private HashSet< Short > cornerTiles;

    /**
     * Evaluations for the corners. Since corners may have different shapes and effects, it may be useful to somehow
     * measure the corners' relevance. A stone capturing a corner with a higher corner stone evaluation is considered
     * to be more useful for the respective player.
     * The HashMap is calculated in the constructor.
     */
    private HashMap< Short, Double > cornerStoneValues;

    /**
     * Sum of all corner evaluations.
     */
    private double cornerStoneValueSum;

    /**
     * Computes the corner stone data by iterating over the whole board. The function is potentially able to
     * execute O(n^2) steps where n is the board size.
     *
     * @param board The board the match is played on.
     */
    public CornerStoneHeuristics( Board board )
    {
        this.cornerTiles = FanBase.findCornerTiles( board );
        this.cornerStoneValues = this.calcCornerStoneValuesBFS( board, this.cornerTiles );

        this.cornerStoneValueSum = 0;
        for ( Short pos : this.cornerTiles )
        {
            this.cornerStoneValueSum += cornerStoneValues.get( pos );
        }
    }

    /**
     * Calculates a corner stone evaluation based on how many tiles are directly and indirectly affected by this
     * corner. For that purpose, an average ray length starting from a corner tile gets computed and
     * after that, a BFS taking this average length as depth limit counts how many tiles may be affected.
     *
     * @param board       The board the match is played on.
     * @param cornerTiles The set of corners.
     * @return An evaluation mapping.
     */
    private HashMap< Short, Double > calcCornerStoneValuesBFS( Board board, HashSet< Short > cornerTiles )
    {
        BoardIterator it = board.getBoardIterator();
        HashMap< Short, Double > stoneValues = new HashMap<>();

        int[] rayLengths;
        int avgLength;
        double stoneValue;

        /* calculate evaluation for each corner tile */
        for ( Short pos : cornerTiles )
        {
            it.moveTo( pos );

            rayLengths = FanBase.getRayLengths( board, it.getX(), it.getY() );

            /* calcluate an average length assuming that there may be at most four possible rays */
            avgLength = 0;
            for ( int l : rayLengths )
            {
                avgLength += l;
            }
            avgLength = avgLength / 4;

            stoneValue = FanBase.getNumberOfBFSReachableTiles( board, it.getX(), it.getY(), avgLength );

            stoneValues.put( pos, stoneValue );
        }

        return stoneValues;
    }

    /**
     * author Julius Hense
     * <p>
     * Derives a measure between 0 (bad) and 1 (good) based on the stones placed on corner tiles. It furthermore
     * takes the threat of such stones in the current state into account.
     *
     * @param state  A state to be evaluated.
     * @param player A perspective to evaluate the state from.
     * @param p1     Choose from [0,1]. The higher the value is, the lower becomes the evaluation when there is
     *               one (in this sense best) opponent player with many high-evaluated corner stones. If this value
     *               is 0, only the sum of the opponent players' corner evaluations is taken into account, but not
     *               the distribution of the corners among the opponent players.
     * @param p2     Choose from [0,1]. The higher the value is the more do own override stones increase the evaluation.
     *               This particularly means that for a high value it becomes more likely that the player keeps his override
     *               stones. Assumption: the player needs 1/p2 override stones to capture an average-valued corner tile.
     * @return a real number between 0 and 1.
     */
    public double heurRelativeWeightedCornerStones( State state, Player player, double p1, double p2 )
    {
        /* default value is 0.5 */
        if ( this.cornerStoneValueSum == 0 )
            return 0.5;

        /* local variables */
        PlayerPool players = state.getPlayers();
        PlayerEval< Double > scorecard = new PlayerEval<>( players );
        for ( Player p : players )
            scorecard.setEval( p, 0.0 );

        BoardIterator it = state.getBoard().getBoardIterator();
        double riskReducedScore;

        /* add reduced corner stone values of all captured corners to respective player's scorecard */
        for ( Short pos : this.cornerTiles )
        {
            it.moveTo( pos );
            if ( it.isOccupied() && !it.isOccupiedByExpansionStone() )
            {
                /* reduce value if the stone may be captured soon */
                riskReducedScore = this.riskReduction( state, pos );

                scorecard.setEval( it.getOccupant(), scorecard.getEval( it.getOccupant() ) + riskReducedScore );
            }
        }

        /* derive some measures */
        double scorePlayer;
        double scoreMax = 0;
        double scoreSum = 0;

        for ( Player p : players )
        {
            if ( scorecard.getEval( p ).doubleValue() > scoreMax )
            {
                scoreMax = scorecard.getEval( p ).doubleValue();
            }
            scoreSum += scorecard.getEval( p ).doubleValue();
        }
        scorePlayer = scorecard.getEval( player ).doubleValue();

        /* calculate score
         * if no corner is captured, the result is 0.5
         * for each corner owned by the player, the respective percentage of the corner stone eval sum is added
         * for all corners owned by opponents, a value is subtracted
         * if there is a strong opponent player owning many corners, another subtraction is performed */
        double score = 0.5 + ( 1.0 / ( 2.0 * this.cornerStoneValueSum ) ) * (
                scorePlayer -
                scoreSum / players.getNumberOfPlayers() -
                ( scoreMax - scoreSum / players.getNumberOfPlayers() ) * p1
        );

        /* consider own override stones */
        score += p2 * players.getPlayer( player.getID() ).getNOverrideStones() *
                 ( 1.0 / this.cornerTiles.size() );

        /* bound check */
        if ( score < 0 )
            score = 0;
        else if ( score > 1 )
            score = 1;

        return score;
    }

    /**
     * Calculates a reduced corner stone evaluation if the stone placed on the corner is likely to
     * be captured soon.
     *
     * @param state The current game state.
     * @param pos   The position of the corner.
     * @return the reduced evaluation.
     */
    private double riskReduction( State state, Short pos )
    {
        double value = this.cornerStoneValues.get( pos );

        BoardIterator it = state.getBoard().getBoardIterator();
        it.moveTo( pos );

        double overrideThreats =
                CornerStoneHeuristics.getCornerStoneOverrideThreatPlayers( state, it.getX(), it.getY() );

        value = value * ( 1.0 - ( overrideThreats / ( state.getPlayers().getNumberOfPlayers() - 1 ) ) );

        return value;
    }

    /**
     * author Julius Hense
     * <p>
     * Calculates how many opponent players may capture a player's corner stone by placing an override stone on
     * this corner stone.
     * <p>
     * Complexity: linear in mapsize.
     *
     * @param state The current game state.
     * @param x,y   The corner stone position.
     * @return the number of players that may capture this corner stone.
     */
    public static int getCornerStoneOverrideThreatPlayers( State state, int x, int y )
    {
        RayIterator it = state.getBoard().getRayIterator( x, y );

        if ( !it.isOccupied() )
            return 0;

        PlayerPool players = state.getPlayers();
        PlayerEval< Boolean > flags = new PlayerEval<>( players );

        Player player = players.getPlayer( it.getOccupant() );
        Player opponent;
        int counter = 0;

        for ( Direction dir : Direction.values() )
        {
            it.reset( dir );
            while ( it.hasNext() )
            {
                it.next();
                if ( !it.isOccupied() || it.isOccupiedByExpansionStone() )
                {
                    break;
                }
                else
                {
                    opponent = players.getPlayer( it.getOccupant() );

                    if ( !opponent.equals( player ) && flags.getEval( opponent ) == null && !opponent.disqualified()
                         && opponent.hasOverrideStone() )
                    {
                        flags.setEval( opponent, true );
                    }
                }
            }
        }

        for ( Player p : players )
        {
            if ( flags.getEval( p ) != null )
            {
                ++counter;
            }
        }

        return counter;
    }
}
