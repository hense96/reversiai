package de.rwth.reversiai.util;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

/**
 * This enum class offers a standardized logging interface. By choosing one of the given log topics, one can
 * categorize the aspects one wants to log. The topics themselves appear in the log stream in a standardized format.
 * The class maintains logging streams one can write to.
 */
public enum LogTopic
{
    error( "ERROR" ),
    warning( "WARNING" ),
    info( "INFO" ),
    configuration( "CONFIGURATION" ),
    network( "NETWORK" ),
    deepening( "DEEPENING" ),
    settings( "SETTINGS" ),
    statistics( "STATISTICS" ),
    noprefix;

    static
    {
        // Find the longest name of a LogTopic in order to align all prefixes
        int maxPrefixLength = 0;

        for ( LogTopic logTopic : LogTopic.values() )
        {
            if ( logTopic.prefix != null && logTopic.prefix.length() > maxPrefixLength )
            {
                maxPrefixLength = logTopic.prefix.length();
            }
        }

        // We add two brackets around the LogTopic's name and an extra space add the end of the prefix
        maxPrefixLength += 3;

        for ( LogTopic logTopic : LogTopic.values() )
        {
            if ( logTopic.prefix != null )
            {
                StringBuilder buffer = new StringBuilder( maxPrefixLength );

                // Add brackets around the LogTopic name
                buffer.append( "[" );
                buffer.append( logTopic.prefix );
                buffer.append( "]" );

                // Add spacing to align log messages
                for ( int i = logTopic.prefix.length() + 2; i <= maxPrefixLength; i++ )
                {
                    buffer.append( " " );
                }

                logTopic.prefix = buffer.toString();
            }
            else
            {
                logTopic.prefix = "";
            }
        }
    }

    /**
     * Holds the prefix of a log topic.
     */
    private String prefix;

    /**
     * List of info log streams.
     */
    public final List< PrintStream > infoStreams = new LinkedList<>();

    /**
     * List of error log streams.
     */
    public final List< PrintStream > errorStreams = new LinkedList<>();

    /**
     * Constructor log topics without prefix.
     */
    LogTopic()
    {

    }

    /**
     * @param prefix The prefix of the log topic.
     */
    LogTopic( String prefix )
    {
        this.prefix = prefix;
    }

    /**
     * Writes a logging message with the log topic to the info log streams.
     *
     * @param message A logging message.
     */
    public void log( String message )
    {
        for ( PrintStream stream : infoStreams )
        {
            stream.println( this.prefix + message );
        }
    }

    /**
     * Writes a multiline logging message with the log topic to the info log streams.
     *
     * @param message A multiline logging message.
     */
    public void logMultiline( String message )
    {
        this.log( this.transformMultilineMessage( message ) );
    }

    /**
     * Writes a logging message with the log topic to the info log streams.
     *
     * @param format A format string.
     * @param args   Arguments of the format string.
     */
    public void log( String format, Object... args )
    {
        for ( PrintStream stream : infoStreams )
        {
            stream.printf( this.prefix + format + "\n", args );
        }
    }

    /**
     * Writes a multiline logging message with the log topic to the info log streams.
     *
     * @param format A format string.
     * @param args   Arguments of the format string.
     */
    public void logMultiline( String format, Object... args )
    {
        this.log( this.transformMultilineMessage( format ), args );
    }

    /**
     * Writes a logging message with the log topic to the error log streams.
     *
     * @param message A logging message.
     */
    public void error( String message )
    {
        for ( PrintStream stream : errorStreams )
        {
            stream.println( this.prefix + message );
        }
    }

    /**
     * Writes a multiline logging message with the log topic to the error log streams.
     *
     * @param message A multiline logging message.
     */
    public void errorMultiline( String message )
    {
        this.error( this.transformMultilineMessage( message ) );
    }

    /**
     * Writes a logging message with the log topic to the error log streams.
     *
     * @param format A format string.
     * @param args   Arguments of the format string.
     */
    public void error( String format, Object... args )
    {
        for ( PrintStream stream : errorStreams )
        {
            stream.printf( this.prefix + format + "\n", args );
        }
    }

    /**
     * Writes a multiline logging message with the log topic to the error log streams.
     *
     * @param format A format string.
     * @param args   Arguments of the format string.
     */
    public void errorMultiline( String format, Object... args )
    {
        this.error( this.transformMultilineMessage( format ), args );
    }

    /**
     * Writes multiline logging messages with the log topic to the info log streams.
     *
     * @param format A format string array.
     * @param args   Arguments of the format string array.
     */
    public void errorMultiline( String[] format, Object... args )
    {
        for ( String s : format )
        {
            this.error( s, args );
        }
    }

    /**
     * Writes {@code StackTrace} of an exception messages to error stream.
     *
     * @param e The exception.
     */
    public void errorStackTrace( Exception e )
    {
        this.error( e.toString() );
        for ( StackTraceElement s : e.getStackTrace() )
        {
            this.error( s.toString() );
        }
    }

    /**
     * Transforms a multiline message to a one line message.
     *
     * @param message The multiline message.
     * @return the one line message.
     */
    private String transformMultilineMessage( String message )
    {
        return message.replaceAll( "\n", "\n" + this.prefix );
    }
}
