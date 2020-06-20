package de.rwth.reversiai.board;

import de.rwth.reversiai.board.iterator.*;

/**
 * Representation of a game board in terms of game logic that serves as an abstraction layer to the underlying
 * {@link BoardTensor} data structure.
 *
 * @author Marvin Pf&ouml;rtner
 * @see AbstractBoardIterator
 * @see BoardIterator
 * @see SequentialBoardIterator
 * @see BFSBoardIterator
 * @see RayIterator
 * @since Assignment 1
 */
public class Board
{
    /**
     * The data structure that describes the layout of the board and the stones placed on it. This reference must not be
     * {@code null}.
     */
    private final BoardTensor boardTensor;

    /**
     * The number of players that initially play on the board. This value must not be smaller than 2 or greater than 8.
     */
    private final int players;

    /**
     * The destructive radius of a bomb. This value must not be negative.
     */
    private final int bombRadius;

    /**
     * Constructs a {@link Board} object with the given parameters.
     *
     * @param boardTensor The data structure that describes the layout of the board. This reference must not be
     *                    {@code null}.
     * @param players     The number of players that initially play on the board. This value must not be smaller than 2
     *                    or greater than 8.
     * @param bombRadius  The destructive radius of a bomb. This value must not be negative.
     */
    public Board( BoardTensor boardTensor, int players, int bombRadius )
    {
        assert players >= 2 && players <= 8;
        assert bombRadius >= 0;
        assert boardTensor != null;

        this.players = players;
        this.bombRadius = bombRadius;

        this.boardTensor = boardTensor;
    }

    /**
     * Creates an exact copy of a given {@link Board} object cloning all attributes except the board's transition
     * structure (by default). In this case both the copy and the original object reference the same array tensor
     * representing the board's transition data. Thus, changes to the transition data have an impact on all copies!
     * Optionally, transition data can be cloned by setting {@code cloneTransitionData} to {@code true}.
     *
     * @param toCopy              The {@link Board} object to be copied. This reference must not be {@code null}.
     * @param cloneTransitionData {@code true} if the copy should have an own instance of the board's transition data,
     *                            {@code false} if the copy and the original object should share the same transition
     *                            data.
     */
    public Board( Board toCopy, boolean cloneTransitionData )
    {
        assert toCopy != null;

        this.boardTensor = toCopy.boardTensor.clone( cloneTransitionData );
        this.players = toCopy.players;
        this.bombRadius = toCopy.bombRadius;
    }

    /**
     * Returns the board's width (in tiles).
     *
     * @return The board's width (in tiles).
     */
    public int getWidth()
    {
        return this.boardTensor.getWidth();
    }

    /**
     * Returns the board's height (in tiles).
     *
     * @return The board's height (in tiles).
     */
    public int getHeight()
    {
        return this.boardTensor.getHeight();
    }

    /**
     * Returns the number of players that initially play on the board.
     *
     * @return The number of players that initially play on the board.
     */
    public int getPlayers()
    {
        return this.players;
    }

    /**
     * Returns the destructive radius of a bomb.
     *
     * @return The destructive radius of a bomb.
     */
    public int getBombRadius()
    {
        return this.bombRadius;
    }

    /**
     * Returns the data structure that describes the current layout of the board.
     *
     * @return The data structure that describes the current layout of the board.
     */
    public BoardTensor getBoardTensor()
    {
        return this.boardTensor;
    }

    /**
     * Returns a new {@link BoardIterator} initially pointing to position (0, 0) of the board.
     *
     * @return A new {@link BoardIterator} pointing to position (0, 0).
     */
    public BoardIterator getBoardIterator()
    {
        return new BoardIterator( this );
    }

    /**
     * Returns a new {@link BoardIterator} initially pointing to the given position of the board.
     *
     * @param x,y The coordinates of the iterator's initial position.
     * @return A new {@link BoardIterator} pointing to position (x, y).
     */
    public BoardIterator getBoardIterator( int x, int y )
    {
        assert this.hasPosition( x, y );

        return new BoardIterator( this, x, y );
    }

    /**
     * Returns a new {@link SequentialBoardIterator} that is ready to iterate over the whole board.
     *
     * @return A new {@link SequentialBoardIterator} entering position (0, 0) on the call of {@code next()}.
     */
    public SequentialBoardIterator getSequentialBoardIterator()
    {
        return new SequentialBoardIterator( this );
    }

    /**
     * Returns a new {@link BFSBoardIterator} that will start a breadth first search with the given depth limit
     * starting from position (0, 0).
     *
     * @param depthLimit The depth limit to apply in the search process.
     * @return A new {@link BFSBoardIterator} with search origin (0, 0) and with depth limit defined by
     * {@code depthLimit}.
     */
    public BFSBoardIterator getBFSBoardIterator( int depthLimit )
    {
        assert depthLimit >= 0;

        return new BFSBoardIterator( this, 0, 0, depthLimit );
    }

    /**
     * Returns a new {@link BFSBoardIterator} that will start a breadth first search without depth limit starting from
     * the given position.
     *
     * @param x,y The coordinates of the search origin.
     * @return A new {@link BFSBoardIterator} with search origin (x, y) and without depth limit.
     */
    public BFSBoardIterator getBFSBoardIterator( int x, int y )
    {
        assert this.hasPosition( x, y );

        return new BFSBoardIterator( this, x, y );
    }

    /**
     * Returns a new {@link BFSBoardIterator} that will start a breadth first search with the given depth limit
     * starting from the given position.
     *
     * @param x,y        The coordinates of the search origin.
     * @param depthLimit The depth limit to apply in the search process.
     * @return A new {@link BFSBoardIterator} with search origin (x, y) and with depth limit defined by
     * {@code depthLimit}.
     */
    public BFSBoardIterator getBFSBoardIterator( int x, int y, int depthLimit )
    {
        assert this.hasPosition( x, y );
        assert depthLimit >= 0;

        return new BFSBoardIterator( this, x, y, depthLimit );
    }

    /**
     * Returns a new uninitialized {@link RayIterator} that should be initialized via the {@code moveTo()} method.
     *
     * @return A new uninitialized {@link RayIterator}.
     */
    public RayIterator getRayIterator()
    {
        return new RayIterator( this );
    }

    /**
     * Returns a new {@link RayIterator} with the given initial position casting its ray north.
     *
     * @param x,y The initial position of the iterator.
     * @return A new {@link RayIterator} with the given initial position casting its ray north.
     */
    public RayIterator getRayIterator( int x, int y )
    {
        assert this.hasPosition( x, y );

        return new RayIterator( this, x, y );
    }

    /**
     * Checks whether a given position exists on the board.
     *
     * @param x,y The position to check for.
     * @return {@code true} if (x, y) exists on the board, {@code false} otherwise.
     */
    public boolean hasPosition( int x, int y )
    {
        return ( 0 <= x && x < this.getWidth()
                 && 0 <= y && y < this.getHeight() );
    }

    /**
     * Constructs and returns a string that represents the board and the stones placed on it using the following
     * encoding:
     * <br><br>
     * <table>
     * <tr><td> -         </td><td> A hole in the board. </td></tr>
     * <tr><td> 0         </td><td> An unoccupied standard tile. </td></tr>
     * <tr><td> 1, ..., 8 </td><td> A standard tile occupied by player 1, ..., 8. </td></tr>
     * <tr><td> X         </td><td> A standard tile occupied by an expansion stone. </td></tr>
     * <tr><td> B         </td><td> A bonus tile. </td></tr>
     * <tr><td> C         </td><td> A choice tile. </td></tr>
     * <tr><td> I         </td><td> An inversion tile. </td></tr>
     * <caption><strong>Description of the encoding</strong></caption>
     * </table>
     *
     * @return A string representation of the board.
     */
    public String toString()
    {
        BoardIterator iterator = this.getBoardIterator();
        StringBuilder buffer = new StringBuilder();

        // Write out horizontal coordinate axis labels
        buffer.append( "      " );

        for ( int i = 0; i < this.getWidth(); i++ )
        {
            buffer.append( i );
            buffer.append( " " );

            // Align numbers with less than 2 decimal places
            if ( i < 10 )
                buffer.append( " " );
        }

        buffer.append( "\n" );

        // Write out horizontal coordinate axis
        buffer.append( "   /-" );

        for ( int i = 0; i < this.getWidth(); i++ )
        {
            buffer.append( "---" );
        }

        buffer.append( "-" );

        buffer.append( "\n" );

        // Write out board data
        for ( int y = 0; y < this.getHeight(); y++ )
        {
            for ( int x = -1; x < this.getWidth(); x++ )
            {
                iterator.moveTo( x, y );

                if ( x == -1 )
                {
                    // Align numbers with less than 10 decimal places
                    if ( y < 10 )
                        buffer.append( " " );

                    // Write out vertical axis labels
                    buffer.append( y );

                    // Write out vertical coordinate axis
                    buffer.append( " |" );
                }
                else if ( iterator.isOccupied() )
                {
                    if ( iterator.isOccupiedByExpansionStone() )
                    {
                        buffer.append( "X" );
                    }
                    else
                    {
                        buffer.append( iterator.getOccupant() );
                    }
                }
                else
                {
                    switch ( iterator.getTileType() )
                    {
                        case ABSENT:
                            buffer.append( "-" );
                            break;

                        case STANDARD:
                            buffer.append( "0" );
                            break;

                        case BONUS:
                            buffer.append( "B" );
                            break;

                        case CHOICE:
                            buffer.append( "C" );
                            break;

                        case INVERSION:
                            buffer.append( "I" );
                            break;
                    }
                }

                buffer.append( "  " );
            }

            buffer.append( "\n" );
        }

        return buffer.toString();
    }
}
