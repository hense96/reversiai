package de.rwth.reversiai.board.iterator;

import de.rwth.reversiai.board.Board;
import de.rwth.reversiai.board.BoardTensor;
import de.rwth.reversiai.board.Direction;

/**
 * Iterator class to perform absolute (global) or relative (stepwise) board traversal.
 *
 * @author Marvin Pf&ouml;rtner
 * @see AbstractBoardIterator
 * @see Board
 * @since Assignment 1
 */
public class BoardIterator extends AbstractBoardIterator
{
    /**
     * Constructs a new {@link BoardIterator} which initially points to the position (0, 0).
     *
     * @param board The {@link Board} on which the iterator will be working.
     */
    public BoardIterator( Board board )
    {
        super( board, (short) 0 );
    }

    /**
     * Constructs a new {@link BoardIterator} which initially points to the given position.
     *
     * @param board The {@link Board} on which the iterator is working.
     * @param x,y   The position the iterator initially points to.
     */
    public BoardIterator( Board board, int x, int y )
    {
        super( board );

        this.moveTo( x, y );
    }

    /**
     * Constructs a new {@link BoardIterator} which initially points to the given position.
     *
     * @param boardTensor The underlying data structure on which the iterator is working.
     * @param position    The position the iterator initially points to.
     */
    public BoardIterator( BoardTensor boardTensor, short position )
    {
        super( boardTensor, position );
    }

    /**
     * Clones the iterator.
     *
     * @return A clone of the iterator.
     */
    public BoardIterator clone()
    {
        return new BoardIterator( this.boardTensor, this.position );
    }

    /**
     * Checks if there is a neighboring tile in the given direction.
     *
     * @param direction The direction in which to check for an existing tile.
     * @return {@code true} if there is such a tile, {@code false} otherwise.
     */
    public boolean hasNext( Direction direction )
    {
        return this.hasNeighbor( direction );
    }

    /**
     * Moves the iterator taking the transition in the given direction.
     * <strong>WARNING:</strong> For the sake of performance, there is no boundary checking. A move to should thus only
     * take place if there is a tile assigned to the given position.
     *
     * @param direction The direction in which to move.
     */
    public void next( Direction direction )
    {
        this.position = this.boardTensor.getNeighborPosition( this.position, direction );

        assert this.position != -1;
    }

    /**
     * Moves the iterator to the given position.
     * <strong>Warning:</strong> For the sake of performance, there is no boundary checking. A move to should thus only
     * take place if there is a tile assigned to the given position.
     *
     * @param x,y The position to move to.
     */
    public void moveTo( int x, int y )
    {
        this.position = this.makePositionFromCoordinates( x, y );
    }

    /**
     * Moves the iterator to the given position.
     * <strong>Warning:</strong> For the sake of performance, there is no boundary checking. A move to should thus only
     * take place if there is a tile assigned to the given position.
     *
     * @param position The encoding of the position to move to.
     */
    public void moveTo( short position )
    {
        this.position = position;
    }
}