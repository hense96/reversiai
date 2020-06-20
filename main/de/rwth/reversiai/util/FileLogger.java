package de.rwth.reversiai.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Supporting class to write logging data into different files in a given directory.
 *
 * @author Marvin Pf&ouml;rtner
 */
public class FileLogger
{
    /**
     * A file path where the files to write the log stream to are placed.
     */
    private static String logPath = null;

    /**
     * The open files mapped to the respective writer object.
     */
    private static Map< String, BufferedWriter > openFiles = new HashMap<>();

    /**
     * Writes logging data into the given file.
     *
     * @param filename The name of the file to write to.
     * @param data     The logging data string.
     */
    public static void writeToFile( String filename, String data )
    {
        FileLogger.writeToFile( filename, data, false );
    }

    /**
     * Writes logging data into the given file.
     *
     * @param filename The name of the file to write to.
     * @param data     The logging data string.
     * @param keepOpen Pass {@code true} if the given file should stay open after writing.
     */
    public static void writeToFile( String filename, String data, boolean keepOpen )
    {
        // If the logPath is null, file logging is disabled
        if ( logPath == null )
        {
            return;
        }

        BufferedWriter writer;

        try
        {
            if ( openFiles.containsKey( filename ) )
            {
                writer = openFiles.get( filename );
            }
            else
            {
                writer = new BufferedWriter( new FileWriter( new File( logPath, filename ) ) );

                openFiles.put( filename, writer );
            }

            writer.write( data );

            if ( !keepOpen )
            {
                writer.close();
                openFiles.remove( filename );
            }
        }
        catch ( IOException e )
        {
            LogTopic.warning.error( "Error while writing to log file %s: %s!", filename, e.getMessage() );
        }
    }

    /**
     * Do not set new log paths once you have set one.
     *
     * @param path A path to a log file.
     */
    public static void setLogPath( String path )
    {
        if ( logPath == null )
        {
            logPath = path;
        }
        else
        {
            throw new UnsupportedOperationException( "Log path must not be modified once it's set!" );
        }
    }
}
