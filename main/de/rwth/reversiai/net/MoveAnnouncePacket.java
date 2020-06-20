package de.rwth.reversiai.net;

import de.rwth.reversiai.game.State;
import de.rwth.reversiai.move.Move;
import de.rwth.reversiai.move.StandardStoneMove;

/**
 * Representation of packets announcing a made move to all players.
 *
 * @author Roman Karwacik
 */
public class MoveAnnouncePacket extends Packet
{
    /**
     * Construct a MOVEANNOUNCE Packet from payload data
     *
     * @param data The payload data.
     */
    public MoveAnnouncePacket( byte[] data )
    {
        super( PacketType.MOVEANNOUNCE, data );
    }

    /**
     * @return The x coordinate of the tile to place on
     */
    public int getX()
    {
        return ( (int) this.data[ 0 ] << 8 ) | (int) this.data[ 1 ];
    }

    /**
     * @return The y coordinate of the tile to place on
     */
    public int getY()
    {
        return ( (int) this.data[ 2 ] << 8 ) | (int) this.data[ 3 ];
    }

    /**
     * @return The encoding of the move's preference
     */
    public byte getPreference()
    {
        return data[ 4 ];
    }

    /**
     * Parse the 5th byte as a bonus tile preference
     *
     * @return the picked Bonus preference
     */
    public StandardStoneMove.BonusPref getBonusPref()
    {
        return StandardStoneMove.BonusPref.createBonusPref( data[ 4 ] );
    }

    /**
     * @return The number of the player that does the move
     */
    public int getPlayer()
    {
        return data[ 5 ];
    }

    /**
     * Builds a Move according to the content of the Packet and the current State
     *
     * @param state State on which the Move should be evaluated
     * @return Move with content provided in the Packet
     */
    public Move parseMove( State state )
    {
        if ( state.getTurnPlayer().getID() != this.getPlayer() )
        {
            throw new RuntimeException( "Parsed a move packet containing a player who is not the turn player" );
        }

        return state.buildMove( this.getX(), this.getY(), this.getPreference() );
    }

    /**
     * Creates a {@link String} representation of the move that is encoded by the packet
     *
     * @return A {@link String} representation of the move that is encoded by the packet
     */
    public String toString()
    {
        if ( getPreference() != 0x0 )
        {
            if ( getPreference() == (byte) 20 )
            {
                return String.format(
                        "Player %d chose a bomb on (%d, %d)",
                        this.getPlayer(),
                        this.getX(),
                        this.getY()
                );
            }
            else if ( getPreference() == (byte) 21 )
            {
                return String.format(
                        "Player %d chose an override stone on (%d, %d)",
                        this.getPlayer(),
                        this.getX(),
                        this.getY()
                );
            }
            else
            {
                return String.format(
                        "Player %d swapped colors with Player %d on (%d, %d)",
                        this.getPlayer(),
                        this.getPreference(),
                        this.getX(),
                        this.getY()
                );
            }
        }
        else
        {
            return String.format( "Player %d placed on (%d, %d)", this.getPlayer(), this.getX(), this.getY() );
        }
    }
}
