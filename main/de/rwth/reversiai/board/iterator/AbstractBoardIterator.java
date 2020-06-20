package de.rwth.reversiai.board.iterator;

import de.rwth.reversiai.board.Board;
import de.rwth.reversiai.board.BoardTensor;
import de.rwth.reversiai.board.Direction;
import de.rwth.reversiai.board.TileType;

/**
 * The common base class for all iterator classes that traverse the game board. It provides methods to access the
 * corresponding board tensor data structure.
 *
 * @author Marvin Pf&ouml;rtner
 * @see Board
 * @see BoardIterator
 * @see BFSBoardIterator
 * @since Assignment 3
 */
public abstract class AbstractBoardIterator
{
    /**
     * The underlying data structure of the board on which the iterator is operating.
     */
    protected final BoardTensor boardTensor;

    /**
     * The current position of the iterator.
     */
    protected short position = 0;

    /**
     * Constructs a new iterator on the given {@link Board}. This constructor is intentionally left protected
     * because subclasses should be able to force a position to be passed via their constructor.
     *
     * @param board The {@link Board} on which the iterator operates. This reference must not be {@code null}.
     */
    protected AbstractBoardIterator( Board board )
    {
        assert board != null;

        this.boardTensor = board.getBoardTensor();
    }

    /**
     * Constructs a new iterator on the given {@link Board} with its initial position set to the given value.
     * This constructor is intentionally left protected because subclasses should be able to force a standard initial
     * position.
     *
     * @param board    The {@link Board} on which the iterator operates. This reference must not be {@code null}.
     * @param position The initial position of the iterator.
     */
    protected AbstractBoardIterator( Board board, short position )
    {
        assert board != null;

        this.boardTensor = board.getBoardTensor();
        this.position = position;
    }

    /**
     * Constructs a new iterator directly on the given {@link BoardTensor} with its initial position set to the given
     * value. This constructor is intentionally left protected because it will mainly be used for subclasses to
     * to implement the {@code clone()} method.
     *
     * @param boardTensor The {@link BoardTensor} on which the iterator operates. This reference must not be
     *                    {@code null}.
     * @param position    The initial position of the iterator.
     */
    protected AbstractBoardIterator( BoardTensor boardTensor, short position )
    {
        assert boardTensor != null;

        this.boardTensor = boardTensor;
        this.position = position;
    }

    /**
     * Clones the iterator. Both iterators should be able mimic each other's behavior afterwards.
     *
     * @return A cloned iterator.
     */
    public abstract AbstractBoardIterator clone();

    /**
     * Returns the x coordinate of the current position of the iterator.
     *
     * @return The x coordinate of the current position of the iterator.
     */
    public int getX()
    {
        return position % this.boardTensor.getWidth();
    }

    /**
     * Returns the y coordinate of the current position of the iterator.
     *
     * @return The y coordinate of the current position of the iterator.
     */
    public int getY()
    {
        return position / this.boardTensor.getWidth();
    }

    /**
     * Returns the encoding of the current position of the iterator (see class description of {@link BoardTensor}).
     *
     * @return The encoding of the current position of the iterator.
     */
    public short getPosition()
    {
        return this.position;
    }

    /**
     * Computes the encoding of the position induced by the given x and y coordinates (see class description of
     * {@link BoardTensor}).
     *
     * @param x,y The coordinates to transform into a position.
     * @return The encoding of the position induced by the given x and y coordinates.
     */
    protected final short makePositionFromCoordinates( int x, int y )
    {
        return (short) ( this.boardTensor.getWidth() * y + x );
    }

    /**
     * Returns the type of the tile at the current position of the iterator.
     *
     * @return The type of the tile at the current position of the iterator.
     */
    public TileType getTileType()
    {
        return this.boardTensor.getTileType( this.position );
    }

    /**
     * Returns the player ID of the player currently occupying the tile at the current position of the iterator, 0 if
     * the tile is not occupied or 9 if the tile is occupied by an expansion stone.
     *
     * @return The numeric representation of the occupant.
     */
    public byte getOccupant()
    {
        return this.boardTensor.getOccupant( this.position );
    }

    /**
     * Checks whether the tile at the current position of the iterator is occupied by either a player or by an expansion
     * stone.
     *
     * @return {@code true} if the current tile is occupied, otherwise {@code false}.
     */
    public boolean isOccupied()
    {
        return this.getOccupant() > 0;
    }

    /**
     * Checks whether the tile at the current position of the iterator is occupied by an expansion stone.
     *
     * @return {@code true} if the tile is occupied by an expansion stone, otherwise {@code false}.
     */
    public boolean isOccupiedByExpansionStone()
    {
        return this.getOccupant() == 9;
    }

    /**
     * Checks if the tile at the current position of the iterator has a neighboring tile in the given direction.
     *
     * @param direction The direction in which to check for an existing tile.
     * @return {@code true} if there is such a tile, {@code false} otherwise.
     */
    public boolean hasNeighbor( Direction direction )
    {
        return this.boardTensor.getNeighborPosition( this.position, direction ) != -1;
    }

    /**
     * Returns the incoming direction of the transition originating from the iterator's current position and the given
     * direction.
     *
     * @param direction The transition's origin direction.
     * @return The incoming direction of the transition originating from the given position and direction.
     */
    public Direction peekNeighborIncomingDirection( Direction direction )
    {
        return this.boardTensor.getNeighborIncomingDirection( this.position, direction );
    }

    /**
     * Changes the type of the tile at the current position of the iterator to the given tile type.
     *
     * @param type The tile type to assign to the tile.
     */
    public void setTileType( TileType type )
    {
        this.boardTensor.setTileType( this.position, type );
    }

    /**
     * Changes the occupant of the tile at the current position of the iterator.
     *
     * @param id The numeric occupant value to change to.
     */
    public void setOccupant( byte id )
    {
        this.boardTensor.setOccupant( this.position, id );
    }

    /**
     * Turns the tile at the current position of the iterator into an absent tile and removes all incoming and outgoing
     * transitions.
     */
    public void makeHole()
    {
        for ( Direction direction : Direction.values() )
        {
            short neighborPosition = this.boardTensor.getNeighborPosition( this.position, direction );

            if ( neighborPosition != -1 )
            {
                Direction neighborIncomingDirection = this.boardTensor.getNeighborIncomingDirection( this.position,
                                                                                                     direction );

                this.boardTensor.setNeighborPosition(
                        neighborPosition,
                        neighborIncomingDirection,
                        (short) -1 // unassigned
                );

                this.boardTensor.setNeighborIncomingDirection(
                        neighborPosition,
                        neighborIncomingDirection,
                        Direction.NORTH
                );

                this.boardTensor.setNeighborPosition(
                        this.position,
                        direction,
                        (short) -1 // unassigned
                );

                this.boardTensor.setNeighborIncomingDirection(
                        this.position,
                        direction,
                        Direction.NORTH
                );
            }
        }

        this.setOccupant( (byte) 0 );
        this.setTileType( TileType.ABSENT );
    }

    /**
     * Returns a string representation of the iterator's current position.
     *
     * @return A string representation of the iterator's current position.
     */
    public String toString()
    {
        return "(" + this.getX() + ", " + this.getY() + ")";
    }
}
