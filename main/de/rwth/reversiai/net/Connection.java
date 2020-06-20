package de.rwth.reversiai.net;

import de.rwth.reversiai.util.LogTopic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * This class is used to represent the connection to a Reversi server for the {@link de.rwth.reversiai.clients.TCPClient}.
 * It is meant as a "static class" because we only support one server connection per running client.
 *
 * @author Marvin Pf&ouml;rtner
 */
public class Connection
{
    /**
     * The TCP socket that is used in order to communicate with the server.
     */
    private static Socket socket = null;

    /**
     * The {@link OutputStream} of the TCP socket.
     */
    private static OutputStream uplink = null;

    /**
     * The {@link InputStream} of the TCP socket.
     */
    private static InputStream downlink = null;

    /**
     * Connects to a server via the given connection information.
     *
     * @param host The IP address or hostname of the server to connect to.
     * @param port The port at which the Reversi server is running.
     */
    public static void connect( String host, int port )
    {
        LogTopic.network.log( "Connecting to %s:%d...", host, port );

        try
        {
            Connection.socket = new Socket( host, port );

            LogTopic.network.log( "Connection established!" );
        }
        catch ( UnknownHostException e )
        {
            LogTopic.network.error( "The IP address of the host %s could not be determined!", host );

            System.exit( -1 );
        }
        catch ( IOException e )
        {
            LogTopic.network.error( "An error occurred while connecting to the server: %s!", e.getMessage() );

            System.exit( -1 );
        }
        catch ( IllegalArgumentException e )
        {
            LogTopic.network.error( "The port number (%d) is outside the specified range of valid port values!", port );

            System.exit( -1 );
        }

        try
        {
            Connection.uplink = socket.getOutputStream();
            Connection.downlink = socket.getInputStream();
        }
        catch ( IOException e )
        {
            LogTopic.network.error( "Error while opening network streams: %s!", e.getMessage() );

            System.exit( -1 );
        }
    }

    /**
     * Closes the connection with the server.
     */
    public static void close()
    {
        Connection.uplink = null;
        Connection.downlink = null;

        try
        {
            Connection.socket.close();
        }
        catch ( IOException e )
        {
            LogTopic.network.error( "Error while closing the network connection: %s!", e.getMessage() );

            System.exit( -1 );
        }
    }

    /**
     * Sends a {@link Packet} to the server.
     *
     * @param packet The packet to send to the server.
     * @throws IOException If an error occurs while sending the {@link Packet} to the server.
     */
    public static void sendPacket( Packet packet ) throws IOException
    {
        Connection.uplink.write( packet.encode() );
    }

    /**
     * Receives a {@link Packet} from the server
     *
     * @return the Packet subclass with the right type for the received packet
     * @throws IOException If an error occurs while receiving the {@link Packet} from the server.
     */
    public static Packet receivePacket() throws IOException
    {
        PacketType type = PacketType.decode( (byte) downlink.read() );

        byte[] lengthBuffer = new byte[ 4 ];

        if ( downlink.read( lengthBuffer, 0, 4 ) != 4 )
        {
            throw new IOException( "Didn't receive 4 length bytes" );
        }

        int dataLength = ByteBuffer.wrap( lengthBuffer ).getInt();

        byte[] data = new byte[ dataLength ];

        if ( downlink.read( data, 0, dataLength ) != dataLength )
        {
            throw new IOException( "Didn't receive all " + dataLength + " data bytes!" );
        }

        switch ( type )
        {
            case MOVEANNOUNCE:
                return new MoveAnnouncePacket( data );

            case MOVEREQUEST:
                return new MoveRequestPacket( data );

            default:
                return new Packet( type, data );
        }
    }
}
