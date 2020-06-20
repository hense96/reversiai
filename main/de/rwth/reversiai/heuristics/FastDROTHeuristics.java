package de.rwth.reversiai.heuristics;

import de.rwth.reversiai.board.Direction;
import de.rwth.reversiai.board.TileType;
import de.rwth.reversiai.board.iterator.BFSBoardIterator;
import de.rwth.reversiai.board.iterator.BoardIterator;
import de.rwth.reversiai.board.iterator.RayIterator;
import de.rwth.reversiai.game.Player;
import de.rwth.reversiai.game.State;

/**
 * This heuristic function is still under construction and thus neither commented nor formatted nicely.
 */
public class FastDROTHeuristics
{
    public static double evaluate( State state, Player maxPlayer, double p1, double p2, double p3, double p4 )
    {
        assert state != null;
        assert maxPlayer != null;

        // Normalization value
        int totalStones = 0;

        // Relative rank
        int playerStones = 0;

        // Capturing opportunities (mobility)
        int opportunities = 0;

        // Capturing threats
        int threatened = 0;

        // Density for bombing
        double accDensity = 0.0;
        double accEnemyDensity = 0.0;
        int bombableTiles = 0;

        byte maxPlayerID = maxPlayer.getID();

        BoardIterator iterator = state.getBoard().getBoardIterator();
        RayIterator rayIterator = state.getBoard().getRayIterator();
        BFSBoardIterator bfsIterator = state.getBoard().getBFSBoardIterator( state.getBoard().getBombRadius() );

        int allPlayersFound = ( 1 << state.getBoard().getPlayers() ) - 1;

        for ( int y = 0; y < state.getBoard().getHeight(); y++ )
        {
            for ( int x = 0; x < state.getBoard().getWidth(); x++ )
            {
                iterator.moveTo( x, y );

                if ( iterator.isOccupied() && !iterator.isOccupiedByExpansionStone() )
                {
                    // Total stones for relative rank
                    totalStones++;

                    if ( iterator.getOccupant() == maxPlayerID )
                    {
                        // Player stones for relative rank
                        playerStones++;

                        // Capturing opportunities and threats
                        rayIterator.moveTo( x, y );

                        boolean stoneThreatened = false;

                        rays:
                        for ( Direction direction : Direction.values() )
                        {
                            rayIterator.reset( direction );

                            // Directional opportunity
                            int capturable = 0;

                            // Directional threat
                            int playersInRay = 0;

                            if ( rayIterator.hasNext() )
                            {
                                rayIterator.next();

                                if ( !rayIterator.isOccupied() || rayIterator.getOccupant() == maxPlayerID )
                                {
                                    continue rays;
                                }
                                else
                                {
                                    capturable++;
                                }
                            }

                            ray:
                            while ( rayIterator.hasNext() )
                            {
                                rayIterator.next();

                                if ( rayIterator.getX() == x && rayIterator.getY() == y )
                                {
                                    break ray;
                                }

                                if ( rayIterator.isOccupied() )
                                {
                                    if ( rayIterator.getOccupant() == maxPlayerID )
                                    {
                                        if ( maxPlayer.hasOverrideStone() )
                                        {
                                            opportunities += capturable;
                                        }

                                        break ray;
                                    }
                                    else
                                    {
                                        capturable++;

                                        if ( !rayIterator.isOccupiedByExpansionStone() )
                                        {
                                            playersInRay |= 1 << ( rayIterator.getOccupant() - 1 );
                                        }

                                        continue ray;
                                    }
                                }
                                else // !iterator.isOccupied()
                                {
                                    opportunities += capturable;

                                    break ray;
                                }
                            }

                            if ( rayIterator.isOccupied() && !stoneThreatened )
                            {
                                extRay:
                                while ( rayIterator.hasNext() && playersInRay < allPlayersFound )
                                {
                                    rayIterator.next();

                                    if ( rayIterator.getX() == x && rayIterator.getY() == y )
                                    {
                                        break extRay;
                                    }

                                    if ( rayIterator.isOccupied() )
                                    {
                                        if ( !rayIterator.isOccupiedByExpansionStone()
                                             && rayIterator.getOccupant() != maxPlayerID )
                                        {
                                            playersInRay |= 1 << ( rayIterator.getOccupant() - 1 );
                                        }
                                    }
                                    else
                                    {
                                        break extRay;
                                    }
                                }
                            }

                            if ( playersInRay > 0 && !stoneThreatened )
                            {
                                int playersInInvRay = 0;

                                rayIterator.reset( direction.invert() );

                                if ( rayIterator.hasNext() )
                                {
                                    rayIterator.next();

                                    if ( iterator.isOccupied() )
                                    {
                                        for ( Player player : state.getPlayers() )
                                        {
                                            if ( !player.equals( maxPlayer ) && player.hasOverrideStone()
                                                 && ( playersInRay & ( 1 << player.getID() ) ) > 0 )
                                            {
                                                stoneThreatened = true;

                                                break;
                                            }
                                        }
                                    }
                                    else
                                    {
                                        stoneThreatened = true;
                                    }
                                }

                                invRay:
                                while ( rayIterator.hasNext() && playersInInvRay < allPlayersFound && !stoneThreatened )
                                {
                                    rayIterator.next();

                                    if ( rayIterator.getX() == x && rayIterator.getY() == y )
                                    {
                                        break invRay;
                                    }

                                    if ( iterator.isOccupied() )
                                    {
                                        if ( !rayIterator.isOccupiedByExpansionStone() )
                                        {
                                            playersInInvRay |= 1 << ( rayIterator.getOccupant() - 1 );
                                        }
                                    }
                                    else
                                    {
                                        if ( ( playersInRay & playersInInvRay ) != playersInRay )
                                        {
                                            stoneThreatened = true;
                                        }
                                    }
                                }

                                if ( stoneThreatened )
                                {
                                    threatened++;
                                }
                            }
                        }
                    }
                }
                else if ( iterator.isOccupiedByExpansionStone() && maxPlayer.hasOverrideStone() )
                {
                    opportunities++;
                }

                if ( iterator.getTileType() != TileType.ABSENT )
                {
                    bombableTiles++;

                    bfsIterator.reset( x, y );

                    int tilesFound = 0;
                    int ownStonesFound = 0;
                    int enemyStonesFound = 0;

                    while ( bfsIterator.hasNext() )
                    {
                        bfsIterator.next();

                        tilesFound++;

                        if ( bfsIterator.getOccupant() == maxPlayerID )
                        {
                            ownStonesFound++;
                        }
                        else if ( bfsIterator.isOccupied() && !bfsIterator.isOccupiedByExpansionStone() )
                        {
                            enemyStonesFound++;
                        }
                    }

                    double localDensity = ( (double) ownStonesFound ) / ( (double) tilesFound );
                    double localEnemyDensity = ( (double) enemyStonesFound ) / ( (double) tilesFound );

                    accDensity += localDensity * localDensity;
                    accEnemyDensity += localEnemyDensity * localEnemyDensity;
                }
            }
        }

        double averageDensity = accDensity / bombableTiles;
        double averageEnemyDensity = accEnemyDensity / bombableTiles;

        int totalBombs = 0;

        for ( Player p : state.getPlayers() )
        {
            totalBombs += p.getNBombs();
        }

        double relativeRank = ( (double) playerStones ) / ( (double) totalStones );
        double relativeOpportunities = ( (double) opportunities ) / ( (double) totalStones );
        // TODO: Use player stones to normalize?
        double relativeSafeStones = ( (double) ( playerStones - threatened ) ) / ( (double) totalStones );
        double relativeBombPower = ( (double) maxPlayer.getNBombs() ) / ( (double) totalBombs );
        double relativeBombDamage = ( averageEnemyDensity - averageDensity ) * relativeBombPower;

        return p1 * relativeRank + p2 * relativeOpportunities + p3 * relativeSafeStones + p4 * relativeBombDamage;
    }
}
