package de.rwth.reversiai.exceptions;

/**
 * Exception for an error due to an unexpected command line input.
 */
public class CommandLineArgumentException extends Exception
{
    public CommandLineArgumentException( String message )
    {
        super( message );
    }
}
