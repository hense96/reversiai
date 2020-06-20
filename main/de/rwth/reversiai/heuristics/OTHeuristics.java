package de.rwth.reversiai.heuristics;

import de.rwth.reversiai.board.Board;
import de.rwth.reversiai.board.Direction;
import de.rwth.reversiai.board.TileType;
import de.rwth.reversiai.board.iterator.BoardIterator;
import de.rwth.reversiai.game.Player;
import de.rwth.reversiai.game.PlayerEval;
import de.rwth.reversiai.game.PlayerPool;
import de.rwth.reversiai.game.State;

/**
 * The opportunity-thread heuristics described in the report of assignment 4.
 */
public class OTHeuristics
{
    /**
     * author Julius Hense
     * <p>
     * Derives a real measure between 0 (bad) and 1 (good) based on the potential/opportunities and the
     * threats of each player's stones.
     *
     * @param state  a state to be evaluated
     * @param player a perspective to evaluate the state from
     * @param p1     Choose from [0,1]. The higher p1, the more important is the potential of the stones. The lower it
     *               is, the more important is how many stones a player owns.
     * @param p2     Choose from [1,infinity). The higher it is, the stronger and faster reduces the threat of a stone
     *               its value.
     * @param p3     Choose from [0,1]. The higher it is, the more important is the comparison to the best player. The
     *               lower it is, the more important is the comparison to the average of the players.
     * @return a real number between 0 and 1
     */
    public static double relativeOTIndex( State state, Player player, double p1, double p2, double p3 )
    {
        assert state != null;
        assert player != null;
        assert 0 <= p1 && p1 <= 1;
        assert 1 <= p2;
        assert 0 <= p3 && p3 <= 1;

        PlayerPool players = state.getPlayers();

        PlayerEval< Double > otscore = new PlayerEval<>( players );
        for ( Player p : players )
        {
            otscore.setEval( p, 0.0 );
        }

        BoardIterator it = state.getBoard().getBoardIterator();

        /* current variables */
        Player stoneplayer;
        double stonepotential;
        double stonethreat;
        double maxthreat = 8 * players.getNumberOfPlayers();
        int[] stonethreatperdir;
        double stonescore;

        for ( int x = 0; x < state.getBoard().getWidth(); x++ )
        {
            for ( int y = 0; y < state.getBoard().getHeight(); y++ )
            {
                it.moveTo( x, y );

                if ( it.getTileType() != TileType.ABSENT && it.isOccupied() && !it.isOccupiedByExpansionStone() )
                {
                    stoneplayer = players.getPlayer( it.getOccupant() );
                    stonepotential = OTHeuristics.getStoneCapturingPotential( state, it.getX(), it.getY(), false );
                    stonethreat = 0;
                    stonethreatperdir =
                            FanBase.getStoneNumberOfThreatsPerDirection( state, it.getX(), it.getY(), false );
                    for ( int i = 0; i < stonethreatperdir.length; ++i )
                    {
                        stonethreat += stonethreatperdir[ i ];
                    }

                    // TODO maybe weighted linear function better performance
                    stonescore = ( 1 + p1 * stonepotential ) * Math.pow( 1 - ( stonethreat / maxthreat ), p2 );

                    otscore.setEval( stoneplayer, otscore.getEval( stoneplayer ) + stonescore );
                }
            }
        }

        /* now calculate a measure from the derived data */
        double otscorePlayer = 0;
        double otscoreMax = 0;
        double otscoreSum = 0;

        for ( Player p : players )
        {
            if ( otscore.getEval( p ).doubleValue() > otscoreMax )
            {
                otscoreMax = otscore.getEval( p ).doubleValue();
            }
            otscoreSum += otscore.getEval( p ).doubleValue();
        }
        otscorePlayer = otscore.getEval( player );

        if ( otscoreMax == 0 )
            return 0.0;

        return p3 * ( otscorePlayer / otscoreMax ) + ( 1 - p3 ) * ( otscorePlayer / otscoreSum );
    }

    /**
     * author: Julius Hense
     * <p>
     * Calculates how many stones may be captured using this stone as second capturing stone while placing
     * another stone somewhere else. For that purpose, the function iterates over all directions starting
     * at the given position and finds possibilities to place another stone in order to capture stones.
     * <p>
     * Complexity: (number of directions)*(maximal capturing path length) and therefore linear in mapsize,
     * but practically much faster. In principle, the time effort of this method grows with the number
     * of placed stones on the board.
     *
     * @param state              a state to be considered
     * @param x,y                the position where a stone is placed
     * @param considerOverriding pass true if the possibility of using override stones should be considered
     * @return the number of stones that may be captured using the given stone
     */
    public static int getStoneCapturingPotential( State state, int x, int y, boolean considerOverriding )
    {
        assert state != null;
        assert state.getBoard().hasPosition( x, y );

        Board board = state.getBoard();
        BoardIterator it = board.getBoardIterator( x, y );
        int stoneCounter = 0;

        if ( it.getTileType() == TileType.ABSENT || !it.isOccupied() || it.isOccupiedByExpansionStone() )
        {
            return 0;
        }

        Player player = state.getPlayers().getPlayer( it.getOccupant() );
        Direction curDir;
        Direction nextDir;
        int dirStoneCounter;

        for ( Direction initialDir : Direction.values() )
        {
            it.moveTo( x, y );
            curDir = initialDir;
            dirStoneCounter = 0;

            while ( it.hasNext( curDir ) )
            {
                nextDir = it.peekNeighborIncomingDirection( curDir ).invert();
                it.next( curDir );
                curDir = nextDir;

                if ( it.getX() == x && it.getY() == y )
                {
                    if ( !considerOverriding )
                        dirStoneCounter = 0;
                    break;
                }
                else if ( !it.isOccupied() )
                {
                    if ( dirStoneCounter > 0 )
                        ++dirStoneCounter;
                    break;
                }
                else if ( it.getOccupant() == player.getID() )
                {
                    if ( !considerOverriding )
                        dirStoneCounter = 0;
                    break;
                }
                else
                {
                    dirStoneCounter++;
                }
            }

            if ( !it.hasNext( curDir ) && !considerOverriding )
            {
                dirStoneCounter = 0;
            }

            stoneCounter += dirStoneCounter;
        }

        return stoneCounter;
    }
}
