package de.rwth.reversiai.move;

import de.rwth.reversiai.board.Board;
import de.rwth.reversiai.board.Direction;
import de.rwth.reversiai.board.iterator.RayIterator;
import de.rwth.reversiai.game.State;

/**
 * Abstract representation of a stone move, i.e. placing a stone on a tile.
 *
 * @author Julius Hense
 */
public abstract class StoneMove extends Move
{
    /**
     * Array to cache positions which hold a stone of the player performing the move which allows capturing.
     * {@code capturingPositions[i] == pos} means that if you along a ray in the direction dir with
     * {@code dir.encode == i} you can capture all stones on that ray until you reach pos.
     */
    public short[] capturingPositions;

    /**
     * Player executing the move is automatically set to player which is about to move.
     * Player who is about to move will be updated in execute() method.
     *
     * @param state a state containing a board and player data (not null and not empty)
     * @param x,y   a valid position on this board
     */
    public StoneMove( State state, int x, int y )
    {
        super( state, x, y );
        capturingPositions = null;
    }

    /**
     * This method calculates the directions where there may be a capturing process
     * if the move is executed. The information is returned in a short array indicating
     * the positions there may be such a capturing process for each direction index.
     * <p>
     * The method does not consider tile or stone information of the move's actual position!
     *
     * @return short array with capturing positions for each position
     */
    protected short[] getCapturingPositions()
    {
        if ( this.capturingPositions != null )
            return this.capturingPositions;

        RayIterator iterator = this.state.getBoard().getRayIterator( this.x, this.y );
        this.capturingPositions = new short[ 8 ];

        /* iterate over all possible directions */
        for ( Direction initialDir : Direction.values() )
        {
            iterator.reset( initialDir );

            /* firstly, set position of this direction to -1 if there is no opponent stone on the neighbor tile */
            if ( iterator.hasNext() )
            {
                iterator.next();
                if ( !iterator.isOccupied() || iterator.getOccupant() == this.player.getID()
                     || ( iterator.getX() == this.x && iterator.getY() == this.y ) )
                {
                    capturingPositions[ initialDir.encode() ] = -1;
                }
            }

            /* secondly, check whether there is a capturable sequence of stones and expansion tiles */
            if ( capturingPositions[ initialDir.encode() ] != -1 )
            {
                capturingPositions[ initialDir.encode() ] = -1;
                while ( iterator.hasNext() )
                {
                    iterator.next();
                    if ( !iterator.isOccupied() || ( iterator.getX() == this.x && iterator.getY() == this.y ) )
                    {
                        break;
                    }
                    else if ( iterator.getOccupant() == this.player.getID() )
                    {
                        capturingPositions[ initialDir.encode() ] = iterator.getPosition();

                        break;
                    }
                }
            }
        }

        /* corner case check (direct neighbor does not capture):
         * if a sequence consisting of several transitions is found that finally leads to the neighbor
         * tile in the according start direction, placing a stone there would not capture anything */
        for ( Direction dir : Direction.values() )
        {
            if ( capturingPositions[ dir.encode() ] != -1 )
            {
                iterator.reset( dir );
                iterator.next();
                if ( capturingPositions[ dir.encode() ] == iterator.getPosition() )
                {
                    capturingPositions[ dir.encode() ] = -1;
                }
            }
        }

        return capturingPositions;
    }

    /**
     * This method decides whether there is at least one direction in which a capturing
     * process is possible.
     * <p>
     * The method does not consider tile or stone information of the move's actual position!
     *
     * @return true if there is a capturing direction
     */
    protected boolean hasCapturingDirection()
    {
        this.getCapturingPositions();
        for ( short capturingPos : this.capturingPositions )
        {
            if ( capturingPos != -1 )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * This method executes the recoloring if a StoneMove is executed.
     *
     * @param resultBoard the board the move should be executed on
     */
    protected void capture( Board resultBoard )
    {
        assert resultBoard != null;

        this.getCapturingPositions();

        resultBoard.getBoardIterator( this.x, this.y ).setOccupant( this.player.getID() );
        RayIterator iterator = resultBoard.getRayIterator( this.x, this.y );
        Direction curDir;

        /* for all capturing positions, capture along the respective ray until this position is reached */
        for ( int i = 0; i < this.capturingPositions.length; ++i )
        {
            if ( this.capturingPositions[ i ] != -1 )
            {
                curDir = Direction.decode( (byte) i );
                iterator.reset( curDir );
                iterator.next();

                while ( iterator.getPosition() != this.capturingPositions[ i ] )
                {
                    iterator.setOccupant( this.player.getID() );
                    iterator.next();
                }
            }
        }
    }
}