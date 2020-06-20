package de.rwth.reversiai.net;

import java.nio.ByteBuffer;

/**
 * Abstract class with the basic functionality for operating with Packets
 *
 * @author Roman Karwacik
 */
public class Packet
{
    /**
     * The type of the packet as specified in the network specification document
     */
    protected PacketType type;

    /**
     * The length of the packet in bytes.
     */
    protected int length;

    /**
     * The raw data of the packet.
     */
    protected byte[] data;

    /**
     * @param type Type of the packet
     * @param data bytes to be sent/received
     */
    public Packet( PacketType type, byte[] data )
    {
        this.type = type;
        this.length = data.length;
        this.data = data;
    }

    /**
     * Used for single byte Packets such as "PLAYERNUMBER"
     *
     * @param type Type of the packet
     * @param data single byte to be sent/received
     */
    public Packet( PacketType type, byte data )
    {
        this.type = type;
        this.length = 1;
        this.data = new byte[] { data };
    }

    protected Packet() {}

    /**
     * Takes all information (type,length,data) and packs them into a single byte array
     *
     * @return the whole packet as an byte array
     */
    public byte[] encode()
    {
        byte[] buffer = new byte[ 1 + 4 + this.length ];

        buffer[ 0 ] = this.type.encode();

        System.arraycopy(
                ByteBuffer.allocate( 4 ).putInt( this.length ).array(),
                0,
                buffer,
                1,
                4
        );

        System.arraycopy(
                this.data,
                0,
                buffer,
                5,
                this.length
        );

        return buffer;
    }

    /**
     * @return The type of the packet.
     */
    public PacketType getType()
    {
        return type;
    }

    /**
     * @return The length of the packet.
     */
    public int getLength()
    {
        return length;
    }

    /**
     * @return The raw data of the packet.
     */
    public byte[] getData()
    {
        return data;
    }

    /**
     * Interprets the byte data as an ASCII-String, e.g. for Map
     *
     * @return data interpreted as String
     */
    public String dataToString()
    {
        return new String( data );
    }

    /**
     * Interprets the whole packet as hex data. Used for debugging.
     *
     * @return Hex string representing the packet.
     */
    public String toHexString()
    {
        StringBuilder builder = new StringBuilder( 1 + 4 + this.length );

        for ( byte b : this.encode() )
        {
            builder.append( Integer.toHexString( b ) );
        }

        return builder.toString();
    }
}
