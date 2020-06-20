package de.rwth.reversiai.net;

import java.io.IOException;

/**
 * All packet types needed for communicating with the server
 *
 * @author Roman Karwacik
 */
public enum PacketType
{
    /**
     * Packet used to send the group number to the server
     */
    GROUPNUMBER( (byte) 0x01 ),

    /**
     * Packet containing the map specification as a string
     */
    MAP( (byte) 0x02 ),

    /**
     * Packet containing the player number of the client
     */
    PLAYERNUMBER( (byte) 0x03 ),

    /**
     * Packet requesting a move from a player
     */
    MOVEREQUEST( (byte) 0x04 ),

    /**
     * Packet used to respond with a move
     */
    MOVERESPOND( (byte) 0x05 ),

    /**
     * Packet used to announce a made move to all players
     */
    MOVEANNOUNCE( (byte) 0x06 ),

    /**
     * Packet to announce the disqualification of a player to all players
     */
    DISQUALIFICATION( (byte) 0x07 ),

    /**
     * Packet announcing the end of the first phase
     */
    FIRSTPHASEEND( (byte) 0x08 ),

    /**
     * Packet announcing the end of the game
     */
    ENDOFGAME( (byte) 0x09 ),

    /**
     * Undefied packet type
     */
    UNDEFINED( (byte) 0xFF );

    private byte type;

    /**
     * Constructor used to instantiate the enum constants
     *
     * @param type The 8-bit unsigned integer encoding of the packet types
     */
    PacketType( byte type )
    {
        this.type = type;
    }

    /**
     * Encodes a packet type to the corresponding {@code byte} value
     *
     * @return The encoding of the packet type
     */
    public byte encode()
    {
        return this.type;
    }

    /**
     * Decodes a {@code byte} value to the corresponding PacketType
     *
     * @param type The byte to be decoded
     * @return The corresponding {@link PacketType}
     * @throws IOException If an error occurs during decoding
     */
    public static PacketType decode( byte type ) throws IOException
    {
        switch ( type )
        {
            case 0x01:
                return GROUPNUMBER;
            case 0x02:
                return MAP;
            case 0x03:
                return PLAYERNUMBER;
            case 0x04:
                return MOVEREQUEST;
            case 0x05:
                return MOVERESPOND;
            case 0x06:
                return MOVEANNOUNCE;
            case 0x07:
                return DISQUALIFICATION;
            case 0x08:
                return FIRSTPHASEEND;
            case 0x09:
                return ENDOFGAME;
            case (byte) 0xFF:
                throw new IOException( "Server closed connection" );
            default:
                throw new IOException( String.format( "Wrong packet type: 0x%H", type ) );
        }
    }
}