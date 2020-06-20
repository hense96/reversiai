package de.rwth.reversiai.net;

import de.rwth.reversiai.move.StandardStoneMove;

/**
 * Representation of packets used to send a move to the server.
 *
 * @author Roman Karwacik
 */
public class MoveRespondPacket extends Packet
{
    /**
     * Construct a {@code MOVERESPOND} packet representing a {@code StandardStoneMove} that does not place onto a bonus
     * or choice tile
     *
     * @param x,y Position where the stone should be placed
     */
    public MoveRespondPacket( int x, int y )
    {
        super( PacketType.MOVERESPOND, new byte[ 5 ] );

        this.data[ 0 ] = (byte) ( x >> 8 );
        this.data[ 1 ] = (byte) ( x );
        this.data[ 2 ] = (byte) ( y >> 8 );
        this.data[ 3 ] = (byte) ( y );
        this.data[ 4 ] = 0;
    }

    /**
     * Construct a {@code MOVERESPOND} packet representing a {@code StandardStoneMove} onto a choice tile
     *
     * @param x,y          Position where the stone should be placed
     * @param choicePlayer ID of the player to switch stones with (0x1-0x8)
     */
    public MoveRespondPacket( int x, int y, byte choicePlayer )
    {
        this( x, y );

        this.data[ 4 ] = choicePlayer;
    }

    /**
     * Construct a {@code MOVERESPOND} packet representing a {@code StandardStoneMove} onto a bonus tile
     *
     * @param x,y   Position where the stone should be placed
     * @param bonus The bonus preference of the move
     */
    public MoveRespondPacket( int x, int y, StandardStoneMove.BonusPref bonus )
    {
        this( x, y );

        this.data[ 4 ] = bonus.getType();
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
     * @return The bonus tile preference encoded by the 5th byte of the packet
     */
    public StandardStoneMove.BonusPref getBonusPref()
    {
        return StandardStoneMove.BonusPref.createBonusPref( data[ 4 ] );
    }
}
