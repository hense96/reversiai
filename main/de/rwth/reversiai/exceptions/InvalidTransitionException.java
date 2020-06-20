package de.rwth.reversiai.exceptions;

/**
 * Exception for a read in board transition that is actually not allowed according to the game specification.
 */
public class InvalidTransitionException extends RuntimeException
{
    public InvalidTransitionException( String message )
    {
        super( message );
    }
}
