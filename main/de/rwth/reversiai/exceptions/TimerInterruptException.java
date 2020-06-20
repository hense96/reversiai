package de.rwth.reversiai.exceptions;

/**
 * Exception for a timer interrupt. This is particularly used for interrupting the AI's calculation if it is
 * about to run into the given time limit for returning a move.
 */
public class TimerInterruptException extends Exception
{
    public TimerInterruptException()
    {
        // The last two parameters suppress the creation of a stack trace
        super( "Timer Interrupt Interrupted Computation", null, true, false );
    }

    @Override
    public String toString()
    {
        return this.getLocalizedMessage();
    }
}
