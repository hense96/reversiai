package de.rwth.reversiai.board;

/**
 * Logical representation of a transition's direction used for data mapping.
 *
 * @author Marvin Pf&ouml;rtner
 * @since Assignment 1
 */
public enum Direction
{
    /**
     * Top
     */
    NORTH( Direction.Encoding.NORTH ),

    /**
     * Top right
     */
    NORTHEAST( Direction.Encoding.NORTHEAST ),

    /**
     * Right
     */
    EAST( Direction.Encoding.EAST ),

    /**
     * Bottom right
     */
    SOUTHEAST( Direction.Encoding.SOUTHEAST ),

    /**
     * Bottom
     */
    SOUTH( Direction.Encoding.SOUTH ),

    /**
     * Bottom left
     */
    SOUTHWEST( Direction.Encoding.SOUTHWEST ),

    /**
     * Left
     */
    WEST( Direction.Encoding.WEST ),

    /**
     * Top left
     */
    NORTHWEST( Direction.Encoding.NORTHWEST );

    /**
     * The data representation of the direction. This should be a byte value from the inner
     * {@link Direction.Encoding} class.
     */
    private final byte encoding;

    /**
     * Constructs a logical representation of a direction.
     *
     * @param encoding The corresponding data representation of the direction. This should be a byte value from the
     *                 internal {@link Direction.Encoding} class.
     */
    Direction( byte encoding )
    {
        this.encoding = encoding;
    }

    /**
     * Encodes a logical representation of a direction to its corresponding data representation.
     *
     * @return The corresponding data representation.
     */
    public byte encode()
    {
        return this.encoding;
    }

    /**
     * Decodes a data representation of a direction to its corresponding logical representation.
     *
     * @param encoding The data representation.
     * @return The corresponding logical representation.
     */
    public static Direction decode( byte encoding )
    {
        switch ( encoding )
        {
            case Encoding.NORTH:
                return Direction.NORTH;

            case Encoding.NORTHEAST:
                return Direction.NORTHEAST;

            case Encoding.EAST:
                return Direction.EAST;

            case Encoding.SOUTHEAST:
                return Direction.SOUTHEAST;

            case Encoding.SOUTH:
                return Direction.SOUTH;

            case Encoding.SOUTHWEST:
                return Direction.SOUTHWEST;

            case Encoding.WEST:
                return Direction.WEST;

            case Encoding.NORTHWEST:
                return Direction.NORTHWEST;

            default:
                throw new IllegalArgumentException( "Invalid direction encoding: " + encoding );
        }
    }

    /**
     * Returns the direction that corresponds to a 180 degree turn from the current direction.
     *
     * @return The inverted direction.
     */
    public Direction invert()
    {
        return Direction.decode( (byte) ( ( this.encoding + 4 ) % 8 ) );
    }

    /**
     * Encoding information for data representation.
     */
    private class Encoding
    {
        private final static byte NORTH = 0;
        private final static byte NORTHEAST = 1;
        private final static byte EAST = 2;
        private final static byte SOUTHEAST = 3;
        private final static byte SOUTH = 4;
        private final static byte SOUTHWEST = 5;
        private final static byte WEST = 6;
        private final static byte NORTHWEST = 7;
    }
}