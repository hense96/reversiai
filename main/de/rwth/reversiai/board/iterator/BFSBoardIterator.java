package de.rwth.reversiai.board.iterator;

import de.rwth.reversiai.board.Board;
import de.rwth.reversiai.board.Direction;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * This iterator traverses the board via breadth first search (optionally with a depth limit).
 *
 * @author Marvin Pf&ouml;rtner
 * @see AbstractBoardIterator
 * @since Assignment 3
 */
public class BFSBoardIterator extends AbstractBoardIterator
{
    /**
     * The optional depth limit at which the breadth first search stops traversing the board. A value of -1 means that
     * no depth limit applies and any other non-negative value specifies a depth limit.
     */
    private final int depthLimit;

    /**
     * The queue of pending positions used to perform depth first search.
     */
    private Queue< Short > pending = new LinkedList<>();

    /**
     * The depth from the search origin assigned to a position by the breadth first search.
     */
    private Map< Short, Integer > depthMap = new HashMap<>();

    /**
     * The distance from the search origin to the tile at the current position of the iterator.
     */
    private int depth;

    /**
     * Constructs a new {@link BFSBoardIterator} starting a breadth first search without depth limit starting from
     * the given search origin position on the given board.
     *
     * @param board The board to search on.
     * @param x,y   The origin position at which the search will start.
     */
    public BFSBoardIterator( Board board, int x, int y )
    {
        this( board, x, y, -1 );
    }

    /**
     * Constructs a new {@link BFSBoardIterator} starting a breadth first search with the given depth limit starting
     * from the given origin position on the given board tensor.
     *
     * @param board      The board to search on.
     * @param x,y        The origin position at which the search will start.
     * @param depthLimit The depth limit at which the search will stop.
     */
    public BFSBoardIterator( Board board, int x, int y, int depthLimit )
    {
        super( board );

        assert depthLimit >= -1;

        this.position = this.makePositionFromCoordinates( x, y );
        this.depthLimit = depthLimit;

        this.pending.add( this.makePositionFromCoordinates( x, y ) );
        this.depthMap.put( this.position, 0 );
    }

    /**
     * Cloning a {@link BFSBoardIterator} is currently not supported.
     *
     * @return Nothing.
     */
    public BFSBoardIterator clone()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the distance from the search origin to the tile at the current position of the iterator.
     *
     * @return the distance from the search origin to the tile at the current position of the iterator.
     */
    public int getDepth()
    {
        return this.depth;
    }

    /**
     * Checks whether the breadth first search has terminated.
     *
     * @return {@code true} if the breadth first search has <strong>NOT</strong> terminated, otherwise
     * {@code false}.
     */
    public boolean hasNext()
    {
        return !this.pending.isEmpty();
    }

    /**
     * Sets the current position of the iterator to the next position of the next tile in the search order of the breadth
     * first search. Should only be called if the breadth first search has not terminated.
     */
    public void next()
    {
        assert this.hasNext();

        this.moveTo( this.pending.remove() );
    }

    /**
     * Moves the iterator to the given position and, if the depth limit has not been reached, discovers all adjacent
     * tiles that have not yet been discovered.
     *
     * @param position The position to move to.
     */
    private void moveTo( short position )
    {
        this.position = position;
        this.depth = this.depthMap.get( position );

        if ( depthLimit == -1 || this.depth < this.depthLimit )
        {
            for ( Direction direction : Direction.values() )
            {
                if ( this.hasNeighbor( direction ) )
                {
                    short neighbor = this.boardTensor.getNeighborPosition( this.position, direction );

                    if ( !this.depthMap.containsKey( neighbor ) )
                    {
                        this.pending.add( neighbor );
                        this.depthMap.put( neighbor, this.depth + 1 );
                    }
                }
            }
        }
    }

    /**
     * Resets the internal state of the iterator and starts a new search from the given position.
     *
     * @param x,y The position at which to start the search.
     */
    public void reset( int x, int y )
    {
        this.position = this.makePositionFromCoordinates( x, y );

        this.pending = new LinkedList<>();
        this.depthMap = new HashMap<>();

        this.pending.add( this.position );
        this.depthMap.put( this.position, 0 );
    }
}
