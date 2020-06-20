package de.rwth.reversiai.board;

/**
 * Logical representation of a tile's type used for data mapping.
 *
 * @author Marvin Pf&ouml;rtner
 */
public enum TileType
{
    /**
     * An absent tile.
     */
    ABSENT( TileType.Encoding.ABSENT ),

    /**
     * A standard tile.
     */
    STANDARD( TileType.Encoding.STANDARD ),

    /**
     * An unoccupied choice tile.
     */
    CHOICE( TileType.Encoding.CHOICE ),

    /**
     * An unoccupied inversion tile.
     */
    INVERSION( TileType.Encoding.INVERSION ),

    /**
     * An unoccupied bonus tile.
     */
    BONUS( TileType.Encoding.BONUS );

    /**
     * The data representation of a tile type. This should be a value from the inner class {@link TileType.Encoding}.
     */
    private final byte encoding;

    /**
     * Constructs a logical representation of a tile type.
     *
     * @param encoding The corresponding data representation of the tile type. This should be a byte value from the
     *                 internal class {@link TileType.Encoding}.
     */
    TileType( byte encoding )
    {
        this.encoding = encoding;
    }

    /**
     * Encodes a logical representation of a tile type to its corresponding data representation.
     *
     * @return The corresponding data representation.
     */
    public byte encode()
    {
        return this.encoding;
    }

    /**
     * Decodes a data representation of a tile type to its corresponding logical representation.
     *
     * @param encoding The data representation.
     * @return The corresponding logical representation.
     */
    public static TileType decode( byte encoding )
    {
        switch ( encoding )
        {
            case TileType.Encoding.ABSENT:
                return TileType.ABSENT;

            case TileType.Encoding.STANDARD:
                return TileType.STANDARD;

            case TileType.Encoding.CHOICE:
                return TileType.CHOICE;

            case TileType.Encoding.INVERSION:
                return TileType.INVERSION;

            case TileType.Encoding.BONUS:
                return TileType.BONUS;

            default:
                throw new IllegalArgumentException( "Invalid tile encoding: " + encoding );
        }
    }

    /**
     * Encoding information for data representation.
     */
    private class Encoding
    {
        private final static byte ABSENT = -1;
        private final static byte STANDARD = 0;
        private final static byte CHOICE = 1;
        private final static byte INVERSION = 2;
        private final static byte BONUS = 3;
    }
}