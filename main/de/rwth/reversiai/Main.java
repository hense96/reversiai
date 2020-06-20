package de.rwth.reversiai;

import de.rwth.reversiai.clients.Client;
import de.rwth.reversiai.clients.StandaloneClient;
import de.rwth.reversiai.clients.TCPClient;
import de.rwth.reversiai.configuration.Configurator;
import de.rwth.reversiai.configuration.DefaultConfigurator;
import de.rwth.reversiai.exceptions.CommandLineArgumentException;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Command Line Interface
 *
 * @author Marvin Pf&ouml;rtner
 */
public class Main
{
    /**
     * The client that runs the software after reading in the command line switches.
     */
    private static Client client;

    /**
     * @param args Command line arguments.
     */
    public static void main( String args[] )
    {
        try
        {
            List< String > argumentsList = Arrays.asList( args );

            // Display help message if requested
            if ( argumentsList.contains( "-h" ) || argumentsList.contains( "--help" ) )
            {
                printHelpMessage();

                System.exit( 0 );
            }

            // Parse optional arguments

            Configurator configurator;

            if ( argumentsList.contains( "--configurator" ) )
            {
                int configClassIndex = argumentsList.indexOf( "--configurator" ) + 1;

                if ( argumentsList.size() <= configClassIndex
                     || argumentsList.get( configClassIndex ).startsWith( "-" ) )
                {
                    throw new CommandLineArgumentException( "Must specify a valid configurator class name!" );
                }

                try
                {
                    configurator = (Configurator) Class.forName(
                            "de.rwth.reversiai.configuration." + argumentsList.get( configClassIndex )
                    ).newInstance();
                }
                catch ( Exception e )
                {
                    throw new CommandLineArgumentException( "There is no such configurator class!" );
                }
            }
            else
            {
                // Use the Default
                configurator = new DefaultConfigurator();
            }

            if ( argumentsList.contains( "--log" ) )
            {
                int logPathIndex = argumentsList.indexOf( "--log" ) + 1;

                if ( argumentsList.size() <= logPathIndex
                     || argumentsList.get( logPathIndex ).startsWith( "-" ) )
                {
                    throw new CommandLineArgumentException( "Must specify a valid log directory path!" );
                }

                String logPath = argumentsList.get( logPathIndex );

                try
                {
                    new File( logPath ).mkdirs();
                }
                catch ( Exception e )
                {
                    throw new CommandLineArgumentException( "Could not create the specified log directory!" );
                }

                configurator.setLogPath( logPath );
            }

            // Perform static configuration
            configurator.configureSystem();

            // Parse mandatory arguments
            if ( argumentsList.contains( "--server" ) && argumentsList.contains( "--board" ) )
            {
                throw new CommandLineArgumentException( "Cannot set both server and board flag!" );
            }
            else if ( argumentsList.contains( "--server" ) )
            {
                int hostIndex = argumentsList.indexOf( "--server" ) + 1;
                int portIndex = hostIndex + 1;

                if ( argumentsList.size() <= portIndex
                     || argumentsList.get( hostIndex ).startsWith( "-" )
                     || argumentsList.get( portIndex ).startsWith( "-" ) )
                {
                    throw new CommandLineArgumentException( "Must specify a hostname and a valid port!" );
                }

                String host = argumentsList.get( hostIndex );
                int port;

                try
                {
                    port = Integer.parseUnsignedInt( argumentsList.get( portIndex ) );
                }
                catch ( NumberFormatException e )
                {
                    throw new CommandLineArgumentException( "That is not a valid port number!" );
                }

                boolean gui = argumentsList.contains( "--gui" );

                Main.client = new TCPClient( host, port, gui, configurator.buildAIConfiguration() );
            }
            else if ( argumentsList.contains( "--gui" ) )
            {
                throw new CommandLineArgumentException(
                        "The gui switch is irrelevant for standalone operation!"
                );
            }
            else if ( argumentsList.contains( "--board" ) )
            {
                int boardFileIndex = argumentsList.indexOf( "--board" ) + 1;

                if ( argumentsList.size() <= boardFileIndex
                     || argumentsList.get( boardFileIndex ).startsWith( "-" ) )
                {
                    throw new CommandLineArgumentException( "Must specify the path to a valid board file!" );
                }

                String boardFile = argumentsList.get( boardFileIndex );

                if ( !new File( boardFile ).isFile() )
                {
                    throw new CommandLineArgumentException( "There is no such board file!" );
                }

                int humanPlayers = 0;
                String replayFile = null;

                if ( argumentsList.contains( "--interactive" ) )
                {
                    int humanPlayersIndex = argumentsList.indexOf( "--interactive" ) + 1;

                    if ( argumentsList.size() <= humanPlayersIndex
                         || argumentsList.get( humanPlayersIndex ).startsWith( "-" ) )
                    {
                        throw new CommandLineArgumentException( "Must specify a valid number of human players!" );
                    }

                    try
                    {
                        humanPlayers = Integer.parseUnsignedInt( argumentsList.get( humanPlayersIndex ) );
                    }
                    catch ( NumberFormatException e )
                    {
                        throw new CommandLineArgumentException( "Number of human players is not a positive integer!" );
                    }
                }

                if ( argumentsList.contains( "--replay" ) )
                {
                    int replayFileIndex = argumentsList.indexOf( "--replay" ) + 1;

                    if ( argumentsList.size() <= replayFileIndex
                         || argumentsList.get( replayFileIndex ).startsWith( "-" ) )
                    {
                        throw new CommandLineArgumentException( "Must specify the path to a valid moves file!" );
                    }

                    replayFile = argumentsList.get( replayFileIndex );

                    if ( !new File( replayFile ).isFile() )
                    {
                        throw new CommandLineArgumentException( "There is no such moves file!" );
                    }
                }

                Main.client = new StandaloneClient( boardFile, humanPlayers, replayFile, configurator );
            }
            else if ( argumentsList.contains( "--interactive" ) || argumentsList.contains( "--replay" ) )
            {
                throw new CommandLineArgumentException(
                        "Interactive and replay mode are only allowed in standalone operation!"
                );
            }
            else
            {
                throw new CommandLineArgumentException(
                        "Must specify either server to connect to or board to play on!"
                );
            }
        }
        catch ( CommandLineArgumentException e )
        {
            System.err.println( e.getMessage() );

            printHelpMessage();

            System.exit( -1 );
        }

        Main.client.run();
    }

    /**
     * Displays a message describing all command line switches.
     */
    private static void printHelpMessage()
    {
        System.out.println(
                "Usage: java -jar Reversi.jar" +
                " ( --server host port [--gui] | --board boardFile [ --interactive players ] [ --replay movesFile ] )" +
                " [ --configurator configClass ]" +
                " [ --log logPath ]" +
                " [ -h | --help ]\n\n" +
                "Arguments:\n" +
                "  --server       Connect the client to the server specified by the given host and port\n" +
                "  --board        Launch a local instance of the Reversi game using the specified board file\n" +
                "  --interactive  Specify the number of human players that play against the local AI\n" +
                "  --replay       Replay the moves that have been written to the specified moves file\n" +
                "  --configurator Specify the configurator class to be used when configuring the AI (default: \"DefaultConfigurator\")\n" +
                "  --gui          Display the game states in a GUI\n" +
                "  --log          Specifies a log directory to write all logging info to (default: logging to file disabled)\n\n" +
                "Basic options:\n" +
                "  -h, --help    Show this help text"
        );
    }
}
