package de.rwth.reversiai.board;

/**
 * Static helper class for methods operating on {@code short} position encodings.
 *
 * @author Marvin Pf&ouml;rtner
 * @since Assignment 5
 */
public class Position
{
    /**
     * Returns the position of the next tile in the given direction <b>without taking transitions into account</b>.
     * Will return the unassigned position (-1) if there is no such position on the board.
     *
     * @param x,y         The position from which the next position should be evaluated.
     * @param direction   The direction for which to return the next position.
     * @param boardWidth  The width of the board.
     * @param boardHeight The height of the board.
     * @return The next position in the given direction or 0 if there is no such position.
     */
    public static short next( int x, int y, Direction direction, int boardWidth, int boardHeight )
    {
        if ( x < 0 || y < 0 || x >= boardWidth || y >= boardHeight )
        {
            return -1;
        }

        int maxX = boardWidth - 1;
        int maxY = boardHeight - 1;

        switch ( direction )
        {
            case NORTH:
                return ( y > 0 ) ? (short) ( x + boardWidth * ( y - 1 ) ) : -1;

            case NORTHEAST:
                return ( x < maxX && y > 0 ) ? (short) ( ( x + 1 ) + boardWidth * ( y - 1 ) ) : -1;

            case EAST:
                return ( x < maxX ) ? (short) ( ( x + 1 ) + boardWidth * y ) : -1;

            case SOUTHEAST:
                return ( x < maxX && y < maxY ) ? (short) ( ( x + 1 ) + boardWidth * ( y + 1 ) ) : -1;

            case SOUTH:
                return ( y < maxY ) ? (short) ( x + boardWidth * ( y + 1 ) ) : -1;

            case SOUTHWEST:
                return ( x > 0 && y < maxY ) ? (short) ( ( x - 1 ) + boardWidth * ( y + 1 ) ) : -1;

            case WEST:
                return ( x > 0 ) ? (short) ( ( x - 1 ) + boardWidth * y ) : -1;

            case NORTHWEST:
                return ( x > 0 && y > 0 ) ? (short) ( ( x - 1 ) + boardWidth * ( y - 1 ) ) : -1;

            default:
                throw new RuntimeException( "This should not happen!" );
        }
    }
}
