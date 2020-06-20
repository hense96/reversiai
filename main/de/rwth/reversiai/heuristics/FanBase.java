package de.rwth.reversiai.heuristics;

import de.rwth.reversiai.board.Board;
import de.rwth.reversiai.board.Direction;
import de.rwth.reversiai.board.TileType;
import de.rwth.reversiai.board.iterator.BFSBoardIterator;
import de.rwth.reversiai.board.iterator.BoardIterator;
import de.rwth.reversiai.board.iterator.RayIterator;
import de.rwth.reversiai.game.Player;
import de.rwth.reversiai.game.PlayerEval;
import de.rwth.reversiai.game.PlayerPool;
import de.rwth.reversiai.game.State;

import java.util.*;

/**
 * This class provides static supporting functions that all heuristics may use often.
 * Such supporting functions may call each other, too.
 * Hints for implementing new functions: Outsource as many useful functions as possible into this supporting function class,
 * work as modular as possible, write precise comments and to return not normalized, well interpretable values.
 * If possible, give an estimate about the computational complexity of the implemented functions.
 *
 * @author Julius Hense
 */
public class FanBase
{
    /**
     * author Julius Hense
     * <p>
     * Returns a set of all corner tiles. A position is considered to be a corner tile if a stone on this
     * tile may never be captured without making use of an override stone.
     * <p>
     * Complexity: linear in board size
     *
     * @param board The board the game takes place on.
     * @return the set of corner tile positions as {@code Shorts}.
     */
    public static HashSet< Short > findCornerTiles( Board board )
    {
        BoardIterator it = board.getBoardIterator();
        HashSet< Short > stones = new HashSet<>();

        for ( int y = 0; y < board.getHeight(); ++y )
        {
            for ( int x = 0; x < board.getWidth(); ++x )
            {
                it.moveTo( x, y );
                if ( it.getTileType() != TileType.ABSENT && FanBase.isCornerTile( board, x, y ) )
                {
                    stones.add( it.getPosition() );
                }
            }
        }

        return stones;
    }

    /**
     * author Julius Hense
     * <p>
     * A tile is considered to be a corner tile if this tile can never be captured without using an override stone.
     * <p>
     * Complexity: constant
     *
     * @param board The board.
     * @param x,y   The position.
     * @return {@code true} if the tile is a corner tile.
     */
    public static boolean isCornerTile( Board board, int x, int y )
    {
        RayIterator it = board.getRayIterator( x, y );
        RayIterator itInv = board.getRayIterator( x, y );
        Direction dir;

        for ( int d = 0; d < 4; ++d )
        {
            dir = Direction.decode( (byte) d );
            it.reset( dir );
            itInv.reset( dir.invert() );

            if ( it.hasNext() && itInv.hasNext() )
            {
                return false;
            }
        }

        return true;
    }

    /**
     * author Julius Hense
     * <p>
     * Calculates how long one may walk along the ray until one hits an absent tile/a hole.
     * The calculation is done for each direction starting from the given position.
     * <p>
     * Complexity: linear in board size.
     *
     * @param board The board the game takes place on.
     * @param x,y   The position to start from.
     * @return An array storing the length of the ray for each direction.
     */
    public static int[] getRayLengths( Board board, int x, int y )
    {
        RayIterator it = board.getRayIterator( x, y );
        int[] counter = { 0, 0, 0, 0, 0, 0, 0, 0 };

        for ( Direction dir : Direction.values() )
        {
            it.reset( dir );

            while ( it.hasNext() )
            {
                it.next();
                ++counter[ dir.encode() ];
            }
        }

        return counter;
    }

    /**
     * author Julius Hense
     * <p>
     * Calculates how many non-absent tiles are reachable by performing BFS from the given position.
     *
     * @param board      The board the game takes place on.
     * @param x,y        The position to start from.
     * @param depthLimit The depth limit for BFS.
     * @return the number of reachable non-absent tiles.
     */
    public static int getNumberOfBFSReachableTiles( Board board, int x, int y, int depthLimit )
    {
        BFSBoardIterator it = board.getBFSBoardIterator( x, y, depthLimit );
        int counter = 0;

        while ( it.hasNext() )
        {
            it.next();
            ++counter;
        }

        return counter;
    }

    /**
     * author Julius Hense
     * <p>
     * Calculates how many stones of each player will be destroyed if a bomb is dropped at a given position.
     * <p>
     * Complexity: radius^2
     *
     * @param state  the state where a bomb move should be performed on
     * @param x,y    the desired position of the bomb move
     * @param radius the bomb radius
     * @return player evaluation with number of lost stones of each player
     */
    public static PlayerEval getNumberOfBombedStones( State state, int x, int y, int radius )
    {
        assert state != null;
        assert state.getBoard().hasPosition( x, y );
        assert radius >= 0;

        PlayerPool players = state.getPlayers();
        Player curPlayer;

        PlayerEval< Integer > nBombedStones = new PlayerEval<>( state.getPlayers() );

        for ( Player p : players )
            nBombedStones.setEval( p, 0 );


        BFSBoardIterator it = state.getBoard().getBFSBoardIterator( x, y, radius );

        while ( it.hasNext() )
        {
            it.next();

            if ( it.getTileType() != TileType.ABSENT && it.isOccupied() && !it.isOccupiedByExpansionStone() )
            {
                curPlayer = players.getPlayer( it.getOccupant() );
                nBombedStones.setEval( curPlayer, nBombedStones.getEval( curPlayer ) + 1 );
            }
        }

        return nBombedStones;
    }

    /**
     * author Julius Hense
     * <p>
     * Calculates the number of bombs left in the game, i.e. the sum of bombs all players own.
     *
     * @param state a state containing player data
     * @return number of bombs
     */
    public static int getNumberOfBombs( State state )
    {
        assert state != null;

        PlayerPool players = state.getPlayers();
        int nBombs = 0;

        for ( Player player : players )
        {
            nBombs += player.getNBombs();
        }

        return nBombs;
    }

    /**
     * author Julius Hense
     * <p>
     * Calculates how many stones each player has.
     * <p>
     * Complexity: linear in mapsize
     *
     * @param state a state containing a board
     * @return player evaluation with number of stones for each player
     */
    public static PlayerEval< Integer > getNumberOfStones( State state )
    {
        assert state != null;

        PlayerPool players = state.getPlayers();

        /* nValidMoves counts number of stones for each player in state's players array */
        PlayerEval< Integer > nStones = new PlayerEval<>( players );
        for ( Player p : players )
            nStones.setEval( p, 0 );

        BoardIterator it = state.getBoard().getBoardIterator();

        /* iterate over all board positions */
        for ( int x = 0; x < state.getBoard().getWidth(); ++x )
        {
            for ( int y = 0; y < state.getBoard().getHeight(); ++y )
            {
                it.moveTo( x, y );

                if ( it.getTileType() != TileType.ABSENT && it.isOccupied() && !it.isOccupiedByExpansionStone() )
                {
                    nStones.setEval( it.getOccupant(), nStones.getEval( it.getOccupant() ) + 1 );
                }
            }
        }

        return nStones;
    }

    /**
     * author Julius Hense
     * <p>
     * Returns the number of valid stone moves (i.e. all moves except for BombMoves) in a state for each
     * player. The possibility to give different preferences for choice and bonus tiles are not taken into
     * account.
     * <p>
     * Considering complexity, one needs to expect (number of tiles)*(number of players)*(validity-checking complexity)
     * steps. Therefore, it is a n^2 complexity where n is the board size.
     *
     * @param state the state where the value should be calculated for
     * @return a player evaluation with the number of valid moves for each player
     */
    public static PlayerEval< Integer > getNumberOfValidStoneMoves( State state )
    {
        return FanBase.getNumberOfValidStoneMoves( state, state.getPlayers().getAllPlayers() );
    }

    /**
     * author Julius Hense
     * <p>
     * Returns the number of valid stone moves (i.e. all moves except for BombMoves) in a state for each
     * desired player.
     * <p>
     * Considering complexity, one needs to expect (number of tiles)*(number of passed players)*(validity-checking complexity)
     * steps. Therefore, it is a n^2 complexity where n is the board size.
     *
     * @param state   the state where the value should be calculated for
     * @param players the players the value should be calculated for
     * @return a player evaluation with the number of valid moves for each player in players
     */
    public static PlayerEval< Integer > getNumberOfValidStoneMoves( State state, Set< Player > players )
    {
        assert state != null;
        assert players != null && players.size() > 0;

        /* nValidMoves counts number of valid moves for each player in players array */
        PlayerEval< Integer > nValidMoves = new PlayerEval<>( state.getPlayers() );
        int validmoves;
        for ( Player p : players )
        {
            validmoves = state.calcValidMoves( p ).size(); //generate all valid moves for all players
            nValidMoves.setEval( p, validmoves );
        }

        return nValidMoves;
    }

    /**
     * author Julius Hense
     * <p>
     * Calculates the current rank of each player according to the number of stones. Players with the same amount
     * of stones are ranked equally at the highest possible rank (e.g. ranks may be 1 - 2 - 2 - 4 ...).
     * <p>
     * Complexity: linear in mapsize.
     *
     * @param state a state containing a board
     * @return player evaluation with ranks of each player where 1 is top
     */
    public static PlayerEval< Integer > getRanks( State state )
    {
        assert state != null;

        PlayerPool players = state.getPlayers();

        PlayerEval< Integer > ranks = new PlayerEval( state.getPlayers() );
        PlayerEval< Integer > nStonesEval = FanBase.getNumberOfStones( state );

        PriorityQueue< Integer > nStonesSorted =
                new PriorityQueue<>( state.getPlayers().getNumberOfPlayers(), Collections.reverseOrder() );
        Iterator< Integer > iterator;
        int rank;

        for ( Player p : players )
        {
            nStonesSorted.add( nStonesEval.getEval( p ) );
        }

        for ( Player p : players )
        {
            iterator = nStonesSorted.iterator();
            rank = 1;

            while ( iterator.hasNext() )
            {
                if ( iterator.next() == nStonesEval.getEval( p ) )
                {
                    ranks.setEval( p, rank );
                    break;
                }
                ++rank;
            }
        }

        return ranks;
    }

    /**
     * author: Julius Hense
     * <p>
     * Calculates for a stone how many players are able to capture this stone. The result is split up among the
     * directions where other players may place a stone. For example, if you want to know how many players may
     * capture the stone through placing a stone NORTH from the stone, you will find the result in resultarray[NORTH.decode()].
     * <p>
     * WARNING: If you enable the consideration of overriding stones, there are very special cases which increase the
     * respective result number although this is actually not a capturing process. Consider overriding also includes the
     * possibility of placing an override stone on the given stone itself.
     * <p>
     * Complexity: (number of directions)*(maximal capturing path length), therefore linear in mapsize but practically
     * much faster. In principle, the time effort of this method grows with the number of placed stones on the board.
     *
     * @param state              a state to be considered
     * @param x,y                the position of the stone
     * @param considerOverriding pass true if you want to consider the possibility of using override stones
     * @return an int array with the number of players threatening the given stone for each direction
     */
    public static int[] getStoneNumberOfThreatsPerDirection( State state, int x, int y, boolean considerOverriding )
    {
        assert state != null;
        assert state.getBoard().hasPosition( x, y );

        Board board = state.getBoard();
        BoardIterator it = board.getBoardIterator( x, y );
        PlayerPool players = state.getPlayers();
        Player player = players.getPlayer( it.getOccupant() );
        int[] threatCounter = new int[ 8 ];
        for ( int i = 0; i < threatCounter.length; ++i )
            threatCounter[ i ] = 0;

        if ( it.getTileType() == TileType.ABSENT || !it.isOccupied() || it.isOccupiedByExpansionStone() )
        {
            return threatCounter;
        }

        Direction initialDir;
        Direction curDir;
        Direction nextDir;

        PlayerEval< Boolean > appearsInDir = new PlayerEval<>( players );
        boolean dirIsCapturable;
        PlayerEval< Boolean > appearsInInvDir = new PlayerEval<>( players );
        boolean invDirIsCapturable;

        for ( int d = 0; d < 4; ++d )
        {
            /* reset direction data */
            for ( Player p : players )
            {
                appearsInDir.setEval( p, false );
                appearsInInvDir.setEval( p, false );
            }
            dirIsCapturable = true;
            invDirIsCapturable = true;

            /* initialize variables for direction */
            initialDir = Direction.decode( (byte) d );
            it.moveTo( x, y );
            curDir = initialDir;

            /* move in direction and store player ids of all stones on this path */
            /* moreover, check whether placing a stone is possible after this path */
            while ( it.hasNext( curDir ) )
            {
                nextDir = it.peekNeighborIncomingDirection( curDir ).invert();
                it.next( curDir );
                curDir = nextDir;

                if ( it.getX() == x && it.getY() == y )
                {
                    if ( !considerOverriding )
                        dirIsCapturable = false;
                    break;
                }
                else if ( !it.isOccupied() )
                {
                    break;
                }
                else if ( !it.isOccupiedByExpansionStone() && it.getOccupant() != player.getID() )
                {
                    appearsInDir.setEval( players.getPlayer( it.getOccupant() ), true );
                }
            }

            if ( !it.hasNext( curDir ) && !considerOverriding )
            {
                dirIsCapturable = false;
            }

            /* initialize variables for inverted direction */
            it.moveTo( x, y );
            curDir = initialDir.invert();

            /* move in inverted direction and store player ids of all stones on this path */
            /* moreover, check whether placing a stone is possible after this path */
            while ( it.hasNext( curDir ) )
            {
                nextDir = it.peekNeighborIncomingDirection( curDir ).invert();
                it.next( curDir );
                curDir = nextDir;

                if ( it.getX() == x && it.getY() == y )
                {
                    if ( !considerOverriding )
                        invDirIsCapturable = false;
                    break;
                }
                else if ( !it.isOccupied() )
                {
                    break;
                }
                else if ( !it.isOccupiedByExpansionStone() && it.getOccupant() != player.getID() )
                {
                    appearsInInvDir.setEval( players.getPlayer( it.getOccupant() ), true );
                }
            }

            if ( !it.hasNext( curDir ) && !considerOverriding )
            {
                invDirIsCapturable = false;
            }

            /* count how many players are able to capture the stone by placing a stone in dir and inverted dir */
            if ( dirIsCapturable )
            {
                for ( Player p : players )
                {
                    if ( appearsInInvDir.getEval( p ) && ( considerOverriding || !appearsInDir.getEval( p ) ) )
                        ++threatCounter[ initialDir.encode() ];
                }
            }
            if ( invDirIsCapturable )
            {
                for ( Player p : players )
                {
                    if ( appearsInDir.getEval( p ) && ( considerOverriding || !appearsInInvDir.getEval( p ) ) )
                        ++threatCounter[ initialDir.invert().encode() ];
                }
            }
        }

        return threatCounter;
    }
}