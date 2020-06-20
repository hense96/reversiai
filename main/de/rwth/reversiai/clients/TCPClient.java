package de.rwth.reversiai.clients;

import de.rwth.reversiai.AI;
import de.rwth.reversiai.configuration.AIConfiguration;
import de.rwth.reversiai.exceptions.BoardSyntaxException;
import de.rwth.reversiai.game.State;
import de.rwth.reversiai.move.Move;
import de.rwth.reversiai.net.*;
import de.rwth.reversiai.util.FileLogger;
import de.rwth.reversiai.util.LogTopic;
import de.rwth.reversiai.util.StateBuilder;
import de.rwth.reversiai.visualization.StateVisualization;

import java.io.IOException;
import java.util.Arrays;

/**
 * The {@code TCPClient} manages a game that is hosted by a server. It parses the announcements of the server
 * and answers requests.
 * Gameplay options:
 * <ul>
 * <li>enable a visualization of the game,</li>
 * <li>choose the configuration of the AI that decides on the moves to execute when the server sends a move request.</li>
 * </ul>
 *
 * @see de.rwth.reversiai.net
 * @see de.rwth.reversiai.visualization
 */
public class TCPClient extends Client
{
    /**
     * Our group number.
     */
    public static final byte groupNumber = 7;

    /**
     * The game visualization.
     */
    private StateVisualization stateVisualization = null;

    /**
     * The AI this client makes use of.
     */
    private AI ai;

    /**
     * @param host            The IP address of the server hosting the game.
     * @param port            The used port.
     * @param gui             {@code true} if the game should be visualized.
     * @param aiConfiguration The configuration of the AI this client should use.
     */
    public TCPClient( String host, int port, boolean gui, AIConfiguration aiConfiguration )
    {
        LogTopic.info.log( "Starting Group 7 Reversi Client..." );

        Connection.connect( host, port );

        try
        {
            LogTopic.network.log( "Announcing group number to server..." );

            Connection.sendPacket( new Packet( PacketType.GROUPNUMBER, TCPClient.groupNumber ) );
        }
        catch ( IOException e )
        {
            LogTopic.network.error( "Error while announcing group number to server: %s!", e.getMessage() );

            System.exit( -1 );
        }

        State state = null;

        try
        {
            LogTopic.network.log( "Waiting to receive map from server..." );

            Packet mapPacket = Connection.receivePacket();

            FileLogger.writeToFile( "server.board", mapPacket.dataToString() );

            LogTopic.network.log( "Parsing received map..." );

            state = new StateBuilder().parseString( mapPacket.dataToString() ).buildState();
        }
        catch ( BoardSyntaxException e )
        {
            LogTopic.error.error( e.getMessage() );

            System.exit( -1 );
        }
        catch ( IOException e )
        {
            LogTopic.network.error( "Error while receiving map from server: %s!", e.getMessage() );

            System.exit( -1 );
        }

        byte playerID = 0;

        try
        {
            LogTopic.network.log( "Receiving player number from server..." );

            playerID = Connection.receivePacket().getData()[ 0 ];

            LogTopic.network.log( "We are Player %d", playerID );
        }
        catch ( Exception e )
        {
            LogTopic.network.error( "Error while receiving player number from server: %s!", e.getMessage() );

            System.exit( -1 );
        }

        LogTopic.info.log( "Starting AI with the following parameters..." );
        LogTopic.info.logMultiline( aiConfiguration.toString() );

        this.ai = new AI( state, playerID, aiConfiguration );

        if ( gui )
        {
            LogTopic.info.log( "Starting GUI..." );

            this.stateVisualization = new StateVisualization( state );
        }
    }

    /**
     * Receives and parses packets from the server and makes subsequent method calls until the game ends.
     */
    @Override
    public void run()
    {
        Packet packet;

        main_loop:
        while ( true )
        {
            try
            {
                packet = Connection.receivePacket();
            }
            catch ( IOException e )
            {
                LogTopic.error.error( "Failed to receive Packet!" );
                LogTopic.error.errorStackTrace( e );

                break main_loop;
            }

            switch ( packet.getType() )
            {
                case DISQUALIFICATION:
                {
                    byte disqualifiedPlayerID = packet.getData()[ 0 ];

                    LogTopic.network.log( "Server disqualified Player %d", disqualifiedPlayerID );

                    if ( disqualifiedPlayerID == this.ai.getPlayerID() )
                    {
                        LogTopic.network.error( "Damn, that's us! Aborting Mission!" );
                        LogTopic.error.errorMultiline( this.ai.getState().toString() );

                        break main_loop;
                    }

                    this.ai.getState().disqualify( disqualifiedPlayerID );

                    break;
                }

                case MOVEREQUEST:
                {
                    LogTopic.network.log( packet.toString() );

                    Move move;

                    try
                    {
                        move = ai.computeBestMove(
                                ( (MoveRequestPacket) packet ).getTimeLimit() * 1000000L,
                                ( (MoveRequestPacket) packet ).getMaxDepth(),
                                0.8
                        );
                    }
                    catch ( Exception e )
                    {
                        LogTopic.error.error( "Failed to calculate move" );
                        LogTopic.error.errorMultiline( "Last BoardState\n:" + ai.getState().getBoard().toString() );
                        LogTopic.error.errorStackTrace( e );

                        break main_loop;
                    }

                    try
                    {
                        LogTopic.network.log( "Sending Move: %s", move.toString() );
                        Connection.sendPacket( move.toPacket() );
                    }
                    catch ( IOException e )
                    {
                        LogTopic.network.error( "Error while sending move to server: %s!", e.getMessage() );

                        break main_loop;
                    }
                    break;
                }

                case MOVEANNOUNCE:
                {
                    LogTopic.network.log( packet.toString() );

                    LogTopic.info.logMultiline( ai.getState().getPlayers().toString() );

                    Move move;

                    try
                    {
                        move = ( (MoveAnnouncePacket) packet ).parseMove( ai.getState() );
                    }
                    catch ( Exception e )
                    {
                        LogTopic.error.error( "Failed to parse " + packet.toString() );
                        LogTopic.error.errorMultiline( "Last BoardState:" + ai.getState().getBoard().toString() );
                        LogTopic.error.errorMultiline( Arrays.toString( e.getStackTrace() ) );

                        break main_loop;
                    }
                    ai.setState( move.execute() );

                    if ( this.stateVisualization != null )
                    {
                        this.stateVisualization.updateState( ai.getState() );
                    }

                    break;
                }

                case FIRSTPHASEEND:
                {
                    LogTopic.network.log( "Server announced end of first phase!" );
                    assert ai.getState().isBombingPhase() == true;
                    break;
                }

                case ENDOFGAME:
                {
                    LogTopic.network.log( "Server announced end of game!" );
                    assert ai.getState().isOver() == true;

                    break main_loop;
                }
            }
        }

        Connection.close();
    }
}
