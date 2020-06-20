package de.rwth.reversiai.board.iterator;

import de.rwth.reversiai.board.Board;
import de.rwth.reversiai.board.BoardTensor;
import de.rwth.reversiai.board.Direction;

/**
 * An iterator implementing the "ray-casting" traversal strategy that embodies the canonic Reversi board traversal.
 * It is used whenever a stones capturing behavior shall be mimicked. The iterator will only move in the
 * {@code rayDirection} which may change due to transitions (we call this phenomenon "reflection"). The {@code reset()}
 * method resets the current position to the origin of the ray and changes direction and the {@code moveTo()} method
 * relocates ray origins.
 */
public class RayIterator extends AbstractBoardIterator
{
    /**
     * The origin position of the current ray.
     */
    private short initialPosition;

    /**
     * The direction in which the iterator casts its ray.
     */
    private Direction rayDirection;

    /**
     * Constructs an uninitialized {@link RayIterator} on the given {@link Board} data structure.
     *
     * @param board The {@link Board} data structure on which the iterator operates.
     */
    public RayIterator( Board board )
    {
        super( board );

        this.initialPosition = 0;
        this.position = 0;
        this.rayDirection = Direction.NORTH;
    }

    /**
     * Constructs a {@link RayIterator} that will cast a ray north from the given position on.
     *
     * @param board The {@link Board} data structure on which the iterator operates.
     * @param x,y   The position from which the iterator will start casting the ray.
     */
    public RayIterator( Board board, int x, int y )
    {
        super( board );

        this.moveTo( x, y );
    }

    /**
     * Internal constructor used for cloning a {@link RayIterator}.
     *
     * @param boardTensor     The {@link BoardTensor} data structure on which the iterator operates.
     * @param position        The current position of the iterator.
     * @param initialPosition The origin position of the ray.
     * @param rayDirection    The current direction of the ray.
     */
    protected RayIterator( BoardTensor boardTensor, short position, short initialPosition, Direction rayDirection )
    {
        super( boardTensor, position );

        this.initialPosition = initialPosition;
        this.rayDirection = rayDirection;
    }

    /**
     * Clones the iterator.
     *
     * @return A clone of the iterator.
     */
    public RayIterator clone()
    {
        return new RayIterator( this.boardTensor, this.position, this.initialPosition, this.rayDirection );
    }

    /**
     * Resets the iterator's position to the ray's origin and restarts casting a ray in the given direction.
     *
     * @param rayDirection The direction in which the iterator will cast the new ray.
     */
    public void reset( Direction rayDirection )
    {
        this.position = this.initialPosition;
        this.rayDirection = rayDirection;
    }

    /**
     * Relocates the origin of the ray casted by the iterator and begins to cast a new ray north.
     *
     * @param x,y The new origin position of the iterator.
     */
    public void moveTo( int x, int y )
    {
        this.initialPosition = this.makePositionFromCoordinates( x, y );
        this.position = this.initialPosition;
        this.rayDirection = Direction.NORTH;
    }

    /**
     * Checks whether there is another tile on the current ray.
     *
     * @return {@code true} if there is another tile on the current ray, {@code false} otherwise.
     */
    public boolean hasNext()
    {
        return this.hasNeighbor( this.rayDirection );
    }

    /**
     * Enters the next tile on the current ray.
     * <strong>WARNING:</strong> For the sake of performance, there is no boundary checking. A call to {@code next()}
     * should thus only take place if {@code hasNext()} would return {@code true}.
     */
    public void next()
    {
        Direction reflectedRayDirection = this.peekNeighborIncomingDirection( this.rayDirection ).invert();

        this.position = this.boardTensor.getNeighborPosition( this.position, this.rayDirection );

        assert this.position != -1;

        this.rayDirection = reflectedRayDirection;
    }
}
