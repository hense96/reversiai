package de.rwth.reversiai.exceptions;

/**
 * Exception for an error due to an unexpected data format during reading in a board.
 */
public class BoardSyntaxException extends Exception
{
    public BoardSyntaxException( int line )
    {
        super( "Board syntax error on line " + line + "!" );
    }
}
