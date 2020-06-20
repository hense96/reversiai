package de.rwth.reversiai.board.iterator;

import de.rwth.reversiai.board.Board;
import de.rwth.reversiai.board.BoardTensor;

/**
 * This iterator implements a replacement for the common nested for loops iterating over all the positions on the
 * current board.
 *
 * @author Marvin Pf&ouml;rtner
 * @since Assignment 3
 */
public class SequentialBoardIterator extends AbstractBoardIterator
{
    /**
     * The upper bound for the encoded position in the current board.
     */
    private final short indexUpperBound;

    /**
     * Constructs a new {@link SequentialBoardIterator} that will step on position (0, 0) with the first call of
     * {@code next()}.
     *
     * @param board The {@link Board} data structure on which the iterator will operate.
     */
    public SequentialBoardIterator( Board board )
    {
        super( board, (short) -1 );

        this.indexUpperBound = (short) ( this.boardTensor.getHeight() * this.boardTensor.getWidth() );
    }

    /**
     * Internal constructor used to clone the iterator.
     *
     * @param boardTensor The {@link BoardTensor} data structure on which the iterator operates.
     * @param position    The current position of the iterator.
     */
    protected SequentialBoardIterator( BoardTensor boardTensor, short position )
    {
        super( boardTensor, position );

        this.indexUpperBound = (short) ( this.boardTensor.getHeight() * this.boardTensor.getWidth() );
    }

    /**
     * Clones the iterator.
     *
     * @return A clone of the iterator.
     */
    public SequentialBoardIterator clone()
    {
        return new SequentialBoardIterator( this.boardTensor, this.position );
    }

    /**
     * Checks whether there is an unvisited position on the board.
     *
     * @return {@code true} if there is an unvisited position on the board, {@code false} otherwise.
     */
    public boolean hasNext()
    {
        return this.position < this.indexUpperBound;
    }

    /**
     * Steps on the next unvisited position.
     * <strong>WARNING:</strong> For the sake of performance, there is no boundary checking. A call to {@code next()}
     * should thus only take place if {@code hasNext()} would return {@code true}.
     */
    public void next()
    {
        assert this.hasNext();

        this.position++;
    }
}
