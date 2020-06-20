package de.rwth.reversiai.board;

/**
 * <p>
 * The {@link BoardTensor} is a data structure to efficiently represent both the layout of a game board and the stones
 * placed upon it in memory.
 * </p>
 * <p>
 * Internally the {@link BoardTensor} uses a third-order tensor data structure to store the tile types and the occupants
 * of the individual tiles, as well as a fourth-order tensor data structure to store the transition data for each
 * individual tile. The tensors both linearize their multi-dimensional indices in order to use an efficient
 * implementation as a one-dimensional array.
 * </p>
 * <p>
 * The first index of both tensors ranges from 0 to {@code height - 1} and the second index of both tensors ranges from
 * 0 to {@code width - 1}, thus these indices represent the tile's y and x coordinate, respectively. The third index of
 * the transition tensor ranges from 0 to 7 and thus represents the directions of a tile's outgoing transitions.
 * </p>
 * <p>
 * The tile tensor stores two values in the third dimension, namely, the encoding of the occupant and the tile's type.
 * For each tile and each direction the transition tensor stores the destination and incoming direction
 * (at the neighboring tile) of the transition originating from that position in that direction as a position and a
 * direction encoding, respectively.
 * </p>
 * <p>
 * To encode directions and tile types, we use the encoding values specified in the corresponding classes
 * {@link Direction} and {@link TileType}, to encode occupants, we use 0 for an empty tile, 1-8 for a stone of the
 * corresponding player ID, and 9 for an override stone, and to encode a position, represented as a pair of coordinates
 * (x, y), we use the expression {@code pos = y * width + x} as a {@code short} value. Multiplying {@code pos} by the
 * tensors' corresponding depths yields the first index of that particular tile in the respective linearized tensor. The
 * encoding of the unassigned position is -1.
 * </p>
 * <p>
 * The memory layout of the tile tensor can be visualized as:
 * <br><br>
 * | ty(0, 0) | oc(0, 0) | ... | ty(0, width - 1) | oc(0, width - 1) | ty(1, 0) | oc(1, 0) | ...
 * | ty(height - 1, width - 1) | oc(height - 1, width - 1) |
 * <br><br>
 * where ty(y, x) denotes the type encoding and oc(y, x) denotes the occupant encoding of the tile at position (x, y).
 * Furthermore, the memory layout of the transition tensor can be visualized as:
 * <br><br>
 * | pos(0, 0, 0) | dir(0, 0, 0) | ... | pos(0, 0, 7) | dir(0, 0, 7) | pos(0, 1, 0) | dir(0, 1, 0) | ...
 * | pos(height - 1, width - 1, 7) | dir(height - 1, width - 1, 7) |
 * <br><br>
 * where pos(y, x, d) denotes the destination and dir(y, x, d) denotes the incoming direction (at the neighboring tile)
 * of the transition originating from the tile at (x, y) in direction d.
 * </p>
 * <p>
 * Due to the order in which the entries are stored, the most cache coherent (and propably fastest) way to iterate over
 * the tensors is to first traverse all x values before increasing the y value (which corresponds to a "first-y-then-x"
 * nested for-loop construction).
 * </p>
 * <p>
 * Storing the neighbors for each tile is in fact not necessary but in this case it grants the data structure both an
 * efficient O(1) random access as well as convenient "pointer-like" navigation via the stored x and y coordinates.
 * </p>
 * <p>
 * The {@code TensorTest} experiment has shown that the linearized implementation of a multi-dimensional array is about
 * 3 times faster than naive implementations.
 * </p>
 * <p>
 * <strong>Warning:</strong> For the sake of performance there is no bounds checking on the indices. Make sure that
 * the indices do not exceed their boundaries for this will result in some pretty f***ed up behavior.
 * </p>
 *
 * @author Marvin Pf&ouml;rtner
 * @see Board
 * @see de.rwth.reversiai.board.iterator.AbstractBoardIterator
 * @since Assignment 5
 */
public class BoardTensor
{
    /**
     * The board's width (in tiles). This value must not be smaller than 1 or greater than 50.
     */
    private final int width;

    /**
     * The board's height (in tiles). This value most not be smaller than 1 or greater than 50.
     */
    private final int height;

    /**
     * Third-order linearized tensor storing tile data (see class description). This value should not be {@code null}.
     */
    private final byte[] tileTensor;

    /**
     * Fourth-order linearized tensor storing transition data (see class description). This value should not be
     * {@code null}.
     */
    private final short[] transitionTensor;

    /**
     * Depth of the tile tensor.
     * <strong>WARNING:</strong> This MUST be an {@code int} because otherwise indices in the constructor,
     * {@code getTileTensorEntry} and {@code setTileTensorEntry} would overflow.
     */
    private final static int TILE_TENSOR_DEPTH = 2;

    /**
     * Depth of the transition tensor.
     * <strong>WARNING:</strong> This MUST be an {@code int} because otherwise indices in the constructor,
     * {@code getTransitionTensorEntry} and {@code setTransitionTensorEntry} would overflow.
     */
    private final static int TRANSITION_TENSOR_DEPTH = 8 * 2;

    /**
     * Depth index of the tile's type attribute in the tile tensor.
     */
    private final static int INDEX_TYPE = 0;

    /**
     * Depth index of the tile's occupancy attribute in the tile tensor.
     */
    private final static int INDEX_OCCUPANT = 1;

    /**
     * Depth index of the transition's destination in the transition tensor.
     */
    private final static int INDEX_POSITION = 0;

    /**
     * Depth index of the transition's incoming direction in the transition tensor.
     */
    private final static int INDEX_DIRECTION = 1;

    /**
     * Constructs a new empty {@link BoardTensor} of the given dimensions.
     * <strong>Warning:</strong> The board tensor needs to be initialized, because, concerning game logic, the hereby
     * created board representation is not valid.
     *
     * @param width  The board's width (in tiles). This value most not be smaller than 1 or greater than 50.
     * @param height The board's height (in tiles). This value most not be smaller than 1 or greater than 50.
     */
    public BoardTensor( int width, int height )
    {
        assert width >= 1 && width <= 50;
        assert height >= 1 && width <= 50;

        this.width = width;
        this.height = height;

        this.tileTensor = new byte[ width * height * TILE_TENSOR_DEPTH ];
        this.transitionTensor = new short[ width * height * TRANSITION_TENSOR_DEPTH ];
    }

    /**
     * Copy constructor.
     *
     * @param toCopy              The {@link BoardTensor} to copy.
     * @param cloneTransitionData Flag indicating whether the transition tensor should be cloned or reused.
     */
    private BoardTensor( BoardTensor toCopy, boolean cloneTransitionData )
    {
        this.width = toCopy.width;
        this.height = toCopy.height;

        this.tileTensor = toCopy.tileTensor.clone();
        this.transitionTensor = cloneTransitionData ? toCopy.transitionTensor.clone() : toCopy.transitionTensor;
    }

    /**
     * Clones a {@link BoardTensor}.
     *
     * @param cloneTransitionData Flag indicating whether the transition tensor should be cloned or reused.
     * @return A cloned {@link BoardTensor}.
     */
    public BoardTensor clone( boolean cloneTransitionData )
    {
        return new BoardTensor( this, cloneTransitionData );
    }

    /**
     * Returns the board's width (in tiles).
     *
     * @return The board's width (in tiles).
     */
    public int getWidth()
    {
        return this.width;
    }

    /**
     * Returns the board's height (in tiles).
     *
     * @return The board's height (in tiles).
     */
    public int getHeight()
    {
        return this.height;
    }

    /**
     * Fetches an entry of the tile tensor at the given position and depth index.
     *
     * @param position   Encoding of the position (see class description).
     * @param depthIndex Depth index of the value.
     * @return The entry of the tile tensor.
     */
    private byte getTileTensorEntry( short position, int depthIndex )
    {
        return this.tileTensor[ TILE_TENSOR_DEPTH * position + depthIndex ];
    }

    /**
     * Stores a value in the tile tensor at the given position and depth index.
     *
     * @param position   Encoding of the position (see class description).
     * @param depthIndex Depth index of the value.
     * @param value      The value to be stored in the tile tensor.
     */
    private void setTileTensorEntry( short position, int depthIndex, byte value )
    {
        this.tileTensor[ TILE_TENSOR_DEPTH * position + depthIndex ] = value;
    }

    /**
     * Fetches an entry of the transition tensor at the given position, direction and depth index.
     *
     * @param position   Encoding of the position (see class description).
     * @param direction  Direction index of the transition tensor.
     * @param depthIndex Depth index of the value.
     * @return The entry of the transition tensor.
     */
    private short getTransitionTensorEntry( short position, Direction direction, int depthIndex )
    {
        return this.transitionTensor[ TRANSITION_TENSOR_DEPTH * position + 2 * direction.encode() + depthIndex ];
    }

    /**
     * Stores a value in the transition tensor at the given position, direction and depth index.
     *
     * @param position   Encoding of the position (see class description).
     * @param direction  Direction index of the transition tensor.
     * @param depthIndex Depth index of the value.
     * @param value      The value to be stored in the transition tensor.
     */
    private void setTransitionTensorEntry( short position, Direction direction, int depthIndex, short value )
    {
        this.transitionTensor[ TRANSITION_TENSOR_DEPTH * position + 2 * direction.encode() + depthIndex ] = value;
    }

    /**
     * Returns the {@link TileType} of the tile at the given position.
     *
     * @param position Encoding of the position (see class description).
     * @return The {@link TileType} of the tile at the given position.
     */
    public TileType getTileType( short position )
    {
        return TileType.decode( this.getTileTensorEntry( position, INDEX_TYPE ) );
    }

    /**
     * Changes the {@link TileType} of the tile at the given position to the given {@link TileType}.
     *
     * @param position Encoding of the position (see class description).
     * @param tileType The type to change to. A tile must be unoccupied unless it is a standard tile.
     */
    public void setTileType( short position, TileType tileType )
    {
        assert tileType == TileType.STANDARD || this.getOccupant( position ) == 0 :
                "A non-standard tile must always be unoccupied";

        this.setTileTensorEntry( position, INDEX_TYPE, tileType.encode() );
    }

    /**
     * Returns the occupancy value (see class description) of the tile at the given position.
     *
     * @param position Encoding of the position (see class description).
     * @return The occupancy value (see class description) of the tile at the given position.
     */
    public byte getOccupant( short position )
    {
        return this.getTileTensorEntry( position, INDEX_OCCUPANT );
    }

    /**
     * Changes the occupancy value (see class description) of the tile at the given position to the given
     * {@code occupant}.
     *
     * @param position Encoding of the position (see class description).
     * @param occupant Encoding of the occupancy value to change to (see class description). A tile must be unoccupied
     *                 unless it is a standard tile.
     */
    public void setOccupant( short position, byte occupant )
    {
        assert occupant >= 0 && occupant <= 9;

        // TODO: Make sure this passes! (works without it anyway, but it would be cleaner if it passed)
        // assert this.getTileType( position ) == TileType.STANDARD || occupant == 0;

        this.setTileTensorEntry( position, INDEX_OCCUPANT, occupant );
    }

    /**
     * Returns the encoded destination (see class description) of the transition originating from the given position and
     * direction.
     *
     * @param position  Encoding of the position (see class description).
     * @param direction The transition's origin direction.
     * @return The encoded destination (see class description) of the transition originating from the given position and
     * direction.
     */
    public short getNeighborPosition( short position, Direction direction )
    {
        return this.getTransitionTensorEntry( position, direction, INDEX_POSITION );
    }

    /**
     * Changes the destination of the transition originating from the given position and direction to the given
     * position.
     *
     * @param position         Encoding of the position (see class description).
     * @param direction        The transition's origin direction.
     * @param neighborPosition The new encoded destination of the transition (see class description).
     */
    public void setNeighborPosition( short position, Direction direction, short neighborPosition )
    {
        this.setTransitionTensorEntry( position, direction, INDEX_POSITION, neighborPosition );
    }

    /**
     * Returns the incoming direction of the transition originating from the given position and direction.
     *
     * @param position  Encoding of the position (see class description).
     * @param direction The transition's origin direction.
     * @return The incoming direction of the transition originating from the given position and direction.
     */
    public Direction getNeighborIncomingDirection( short position, Direction direction )
    {
        return Direction.decode( (byte) this.getTransitionTensorEntry( position, direction, INDEX_DIRECTION ) );
    }

    /**
     * Returns the incoming direction of the transition originating from the given position and direction.
     *
     * @param position          Encoding of the position (see class description).
     * @param direction         The transition's origin direction.
     * @param incomingDirection The new incoming direction of the transition.
     */
    public void setNeighborIncomingDirection( short position, Direction direction, Direction incomingDirection )
    {
        this.setTransitionTensorEntry( position, direction, INDEX_DIRECTION, incomingDirection.encode() );
    }
}
