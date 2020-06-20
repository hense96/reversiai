package de.rwth.reversiai.visualization;

/**
 * Representation of a tile's position on the board as a coordinate pair of positive integers.
 *
 * @author Marvin P&ouml;rtner
 */
class Position
{
    /**
     * This is the global reference to the unassigned position.
     */
    public static final Position unassigned = new Position( -1, -1 );

    /**
     * The x coordinate of the tile.
     */
    public final int x;

    /**
     * The y coordinate of the tile.
     */
    public final int y;

    /**
     * Constructs a new tile position from given coordinates.
     *
     * @param x The x coordinate of the tile.
     * @param y The y coordinate of the tile.
     */
    public Position( int x, int y )
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Checks whether the tile's position is unassigned. A tile's position is said to be unassigned if it does not
     * represent a position that is valid/present on the current board.
     *
     * @return <code>true</code> if the tile's position is unassigned; <code>false</code> otherwise.
     */
    public boolean isUnassigned()
    {
        return ( x == -1 || y == -1 );
    }

    /**
     * Returns a string representation of the tile's position as a coordinate pair.
     *
     * @return The string representation
     */
    @Override
    public String toString()
    {
        return "(" + this.x + ", " + this.y + ")";
    }

    /**
     * Indicates whether another object is "equal to" this tile position.
     *
     * @param obj The reference object with which to compare.
     * @return <code>true</code> if the object argument contains a Position object that has the same coordinates as this
     * position; <code>false</code> otherwise.
     */
    @Override
    public boolean equals( Object obj )
    {
        if ( obj instanceof Position )
        {
            Position other = (Position) obj;

            return ( this.x == other.x && this.y == other.y );
        }

        return false;
    }
}
