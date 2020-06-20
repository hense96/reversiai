package de.rwth.reversiai.net;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Representation of packets the server uses to request a move from a player.
 *
 * @author Roman Karwacik
 */
public class MoveRequestPacket extends Packet
{
    /**
     * Construct a MOVEANNOUNCE Packet from payload data
     *
     * @param data The payload data.
     */
    public MoveRequestPacket( byte[] data )
    {
        super( PacketType.MOVEREQUEST, data );
    }

    /**
     * Time the client has to perform a move
     *
     * @return The time limit in milliseconds or 0 if no time limit applies.
     */
    public int getTimeLimit()
    {
        return ByteBuffer.wrap( Arrays.copyOfRange( this.data, 0, 4 ) ).getInt();
    }

    /**
     * Maximum depth at which the client is allowed to search
     *
     * @return The maximum search depth or 0 if no depth limit applies.
     */
    public int getMaxDepth()
    {
        return (int) data[ 4 ];
    }

    /**
     * Returns a {@link String} representation of the move request made by the server.
     *
     * @return A {@link String} representation of the move request made by the server.
     */
    public String toString()
    {
        return "You have to make a move"
               + ( this.getTimeLimit() > 0 ? " in " + this.getTimeLimit() + " milliseconds" : "" )
               + ( this.getMaxDepth() > 0 ? "with a maximum search depth of" + this.getMaxDepth() : "" ) + ".";
    }
}
