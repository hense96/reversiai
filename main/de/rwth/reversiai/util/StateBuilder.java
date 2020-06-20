package de.rwth.reversiai.util;

import de.rwth.reversiai.board.*;
import de.rwth.reversiai.board.iterator.BoardIterator;
import de.rwth.reversiai.exceptions.BoardSyntaxException;
import de.rwth.reversiai.exceptions.InvalidTransitionException;
import de.rwth.reversiai.game.Player;
import de.rwth.reversiai.game.PlayerPool;
import de.rwth.reversiai.game.State;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builder class to construct {@link State} objects from either a ".board" file or from a string containing an encoded
 * board. The board encoding is specified in the "courseRules.pdf" document.
 *
 * @author Marvin Pf&ouml;rtner
 * @see State
 * @see Board
 * @see PlayerPool
 * @since Assignment 1
 */
public class StateBuilder
{
    /**
     * Cache for the number of players that initially play on the board.
     */
    private int players;

    /**
     * Cache for the number of override stones each player holds initially.
     */
    private int overrideStones;

    /**
     * Cache for the number of bombs each player holds initially.
     */
    private int bombs;

    /**
     * Cache for the destructive radius of a bomb.
     */
    private int bombRadius;

    /**
     * Cache for the board's height (in tiles).
     */
    private int height;

    /**
     * Cache for the board's width (in tiles).
     */
    private int width;

    /**
     * Cache for parsed tile data.
     */
    private char[][] tileData;

    /**
     * Cache for parsed transition data.
     */
    private List< int[] > transitionData = new LinkedList<>();

    /**
     * Flag that will be set, once the internal cache is successfully filled with valid data.
     */
    private boolean cacheFilled = false;

    /**
     * Loads an encoded board from the given file and parses the data into the internal cache.
     *
     * @param path The path of the file to load.
     * @return The {@code this} reference to allow chaining.
     * @throws IOException          If the file can't be read
     * @throws BoardSyntaxException If the board is invalid
     */
    public StateBuilder parseFile( String path ) throws IOException, BoardSyntaxException
    {
        this.parseBoard( new BufferedReader( new InputStreamReader( new FileInputStream( path ) ) ) );

        return this;
    }

    /**
     * Parses data from a given board encoding string into the internal cache.
     *
     * @param mapString A string containing a board encoding.
     * @return The {@code this} reference to allow chaining.
     * @throws IOException          If the file can't be read
     * @throws BoardSyntaxException If the board is invalid
     */
    public StateBuilder parseString( String mapString ) throws IOException, BoardSyntaxException
    {
        this.parseBoard( new BufferedReader( new StringReader( mapString ) ) );

        return this;
    }

    /**
     * Parses data from a board encoding string obtained through the given {@link BufferedReader} into the internal
     * cache.
     *
     * @param reader Reader of the ".board" file
     * @throws IOException          If the file can't be read
     * @throws BoardSyntaxException If the board is invalid
     */
    private void parseBoard( BufferedReader reader ) throws IOException, BoardSyntaxException
    {
        String buffer;
        String[] bufferParts;

        // The line number of the given board encoding. Used for error logs.
        int lineNumber = 1;

        // Parse number of players
        buffer = reader.readLine();

        if ( !buffer.matches( "^[2-8]( )*$" ) )
        {
            throw new BoardSyntaxException( lineNumber );
        }

        this.players = Integer.parseUnsignedInt( buffer );

        lineNumber++;

        // Parse number of override stones
        buffer = reader.readLine();

        if ( !buffer.matches( "^([0-9]|[1-9]\\d*)( )*$" ) )
        {
            throw new BoardSyntaxException( lineNumber );
        }

        this.overrideStones = Integer.parseUnsignedInt( buffer );

        lineNumber++;

        // Parse number of bombs and bomb radius
        buffer = reader.readLine();

        if ( !buffer.matches( "^([0-9]|[1-9]\\d*) ([0-9]|[1-9]\\d*)( )*$" ) )
        {
            throw new BoardSyntaxException( lineNumber );
        }

        bufferParts = buffer.split( " " );

        this.bombs = Integer.parseUnsignedInt( bufferParts[ 0 ] );
        this.bombRadius = Integer.parseUnsignedInt( bufferParts[ 1 ] );

        lineNumber++;

        // Parse board dimensions
        buffer = reader.readLine();

        if ( !buffer.matches( "^([0-9]|[1-4][0-9]|50) ([0-9]|[1-4][0-9]|50)( )*$" ) )
        {
            throw new BoardSyntaxException( lineNumber );
        }

        bufferParts = buffer.split( " " );

        this.height = Integer.parseUnsignedInt( bufferParts[ 0 ] );
        this.width = Integer.parseUnsignedInt( bufferParts[ 1 ] );

        lineNumber++;

        // Parse board
        this.tileData = new char[ this.width ][ this.height ];

        for ( int y = 0; y < this.height; y++ )
        {
            buffer = reader.readLine();

            if ( !buffer.matches( "^[0-8cibx\\-]( [0-8cibx\\-])*( )*$" ) )
            {
                throw new BoardSyntaxException( lineNumber );
            }

            bufferParts = buffer.split( " " );

            if ( bufferParts.length != this.width )
            {
                throw new BoardSyntaxException( 5 + y );
            }

            for ( int x = 0; x < this.width; x++ )
            {
                this.tileData[ x ][ y ] = bufferParts[ x ].charAt( 0 );
            }

            lineNumber++;
        }

        // Parse transitions (if present)
        Pattern transitionPattern = Pattern.compile(
                "^([0-9]|[1-4][0-9]) ([0-9]|[1-4][0-9]) ([0-7]) <-> ([0-9]|[1-4][0-9]) ([0-9]|[1-4][0-9]) ([0-7])( )*$"
        );

        while ( ( buffer = reader.readLine() ) != null )
        {
            Matcher transitionMatcher = transitionPattern.matcher( buffer );

            if ( !transitionMatcher.matches() )
            {
                throw new BoardSyntaxException( lineNumber );
            }

            transitionData.add( new int[] {
                    Integer.parseUnsignedInt( transitionMatcher.group( 1 ) ),
                    Integer.parseUnsignedInt( transitionMatcher.group( 2 ) ),
                    Integer.parseUnsignedInt( transitionMatcher.group( 3 ) ),
                    Integer.parseUnsignedInt( transitionMatcher.group( 4 ) ),
                    Integer.parseUnsignedInt( transitionMatcher.group( 5 ) ),
                    Integer.parseUnsignedInt( transitionMatcher.group( 6 ) )
            } );

            lineNumber++;
        }

        reader.close();

        this.cacheFilled = true;
    }

    /**
     * Builds a {@link Board} object from the data in the internal cache. Should only be called if the internal
     * cache has been filled successfully.
     *
     * @return The board constructed from the cached data.
     * @throws InvalidTransitionException If a transition is invalid
     */
    private Board buildBoard() throws InvalidTransitionException
    {
        assert this.cacheFilled;

        BoardTensor boardTensor = new BoardTensor( this.width, this.height );

        Board board = new Board( boardTensor, this.players, this.bombRadius );

        BoardIterator iterator = board.getBoardIterator();

        // Initialize the board tensor data structure without taking transitions into account
        TileType type;
        byte occupant;

        for ( int y = 0; y < this.height; y++ )
        {
            for ( int x = 0; x < this.width; x++ )
            {
                iterator.moveTo( x, y );

                switch ( this.tileData[ x ][ y ] )
                {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                        type = TileType.STANDARD;
                        occupant = (byte) ( this.tileData[ x ][ y ] - '0' );
                        break;

                    case 'x':
                        type = TileType.STANDARD;
                        occupant = 9;
                        break;

                    case '-':
                        type = TileType.ABSENT;
                        occupant = 0;
                        break;

                    case 'c':
                        type = TileType.CHOICE;
                        occupant = 0;
                        break;

                    case 'i':
                        type = TileType.INVERSION;
                        occupant = 0;
                        break;

                    case 'b':
                        type = TileType.BONUS;
                        occupant = 0;
                        break;

                    default:
                        throw new RuntimeException( "This should not be reachable!" );
                }

                iterator.setTileType( type );
                iterator.setOccupant( occupant );

                // Connect each tile to its "coordinate neighbors"
                for ( Direction direction : Direction.values() )
                {
                    boardTensor.setNeighborPosition(
                            iterator.getPosition(),
                            direction,
                            Position.next( x, y, direction, this.width, this.height )
                    );

                    boardTensor.setNeighborIncomingDirection(
                            iterator.getPosition(),
                            direction,
                            direction.invert()
                    );
                }
            }
        }

        // Remove all connections involving absent tiles
        for ( int y = 0; y < this.height; y++ )
        {
            for ( int x = 0; x < this.width; x++ )
            {
                iterator.moveTo( x, y );

                if ( iterator.getTileType() == TileType.ABSENT )
                {
                    iterator.makeHole();
                }
            }
        }

        BoardIterator iterator1 = board.getBoardIterator();
        BoardIterator iterator2 = board.getBoardIterator();
        Direction direction1;
        Direction direction2;

        // Add additional transitions
        for ( int[] transition : this.transitionData )
        {
            iterator1.moveTo( transition[ 0 ], transition[ 1 ] );
            iterator2.moveTo( transition[ 3 ], transition[ 4 ] );

            direction1 = Direction.decode( (byte) transition[ 2 ] );
            direction2 = Direction.decode( (byte) transition[ 5 ] );

            if ( iterator1.hasNeighbor( direction1 )
                 || iterator2.hasNeighbor( direction2 ) )
            {
                throw new InvalidTransitionException(
                        "Transition " + transition[ 0 ] + " " + transition[ 1 ] + " " + transition[ 2 ] + " <-> "
                        + transition[ 3 ] + " " + transition[ 4 ] + " " + transition[ 5 ] + " is not valid!"
                );
            }

            boardTensor.setNeighborPosition(
                    iterator1.getPosition(),
                    direction1,
                    iterator2.getPosition()
            );

            boardTensor.setNeighborIncomingDirection(
                    iterator1.getPosition(),
                    direction1,
                    direction2
            );

            boardTensor.setNeighborPosition(
                    iterator2.getPosition(),
                    direction2,
                    iterator1.getPosition()
            );

            boardTensor.setNeighborIncomingDirection(
                    iterator2.getPosition(),
                    direction2,
                    direction1
            );
        }

        return board;
    }

    /**
     * Builds initial player data from the data in the internal cache and returns it as a {@link PlayerPool} object.
     * Should only be called if the internal cache has been filled successfully.
     *
     * @return The {@link PlayerPool} object containing the initial player data.
     */
    private PlayerPool buildPlayerPool()
    {
        assert this.cacheFilled;

        Player[] playerData = new Player[ this.players ];

        for ( int playerID = 1; playerID <= this.players; playerID++ )
        {
            playerData[ playerID - 1 ] = new Player( (byte) playerID, this.overrideStones, this.bombs );
        }

        return new PlayerPool( playerData );
    }

    /**
     * Builds a {@link State} object from the data in the internal cache. Should only be called if the internal cache
     * has been filled successfully.
     *
     * @return The {@link State} that was built from the parsed encoding data.
     */
    public State buildState()
    {
        return new State( this.buildBoard(), this.buildPlayerPool() );
    }
}
