package de.rwth.reversiai.game;

import de.rwth.reversiai.board.Board;
import de.rwth.reversiai.board.Direction;
import de.rwth.reversiai.board.TileType;
import de.rwth.reversiai.board.iterator.BoardIterator;
import de.rwth.reversiai.board.iterator.RayIterator;
import de.rwth.reversiai.move.BombMove;
import de.rwth.reversiai.move.Move;
import de.rwth.reversiai.move.OverrideStoneMove;
import de.rwth.reversiai.move.StandardStoneMove;
import de.rwth.reversiai.util.LogTopic;

import java.util.HashSet;
import java.util.Set;

/**
 * Representation of a game state. A game state consists of
 * <ul>
 * <li>the current board,</li>
 * <li>the current player data,</li>
 * <li>a turn player in each state, i.e. a player who is about to make her move,</li>
 * <li>the current game phase.</li>
 * </ul>
 * Moreover, the state caches the complete set of valid moves for the turn player. Using this set, the state class
 * implicitly calculates which player is the turn player and whether to proceed to the next game phase.
 */
public class State
{
    /**
     * The current board containing stones and tiles.
     */
    private final Board board;

    /**
     * The current player pool containing all relevant player data.
     */
    private final PlayerPool players;

    /**
     * The set of all valid moves the turn player may perform in this state.
     * {@code getAllValidMoves()} will return always return the correct set.
     */
    private Set< Move > validMoves = null;

    /**
     * The current game phase. Will be set correctly after the constructor call.
     */
    private GamePhase phase;

    /**
     * The current turn player. Will be set correctly after the constructor call.
     */
    private Player turnPlayer;

    /**
     * Constructor for generating an initial state. Game phase is set to PHASE1 and turn player
     * is player 1 (provided that she is able to make a move).
     *
     * @param board   The current game board.
     * @param players The current player data.
     */
    public State( Board board, PlayerPool players )
    {
        assert board != null;
        assert players != null;
        assert players.getPlayer( 1 ) != null;

        this.board = board;
        this.players = players;
        this.phase = GamePhase.PHASE1;
        this.turnPlayer = players.getPlayer( 1 );

        this.turnPlayer.setTurn( true );

        this.calculatePhaseAndTurnPlayer();
    }

    /**
     * Constructor for deriving a new state. It will calculate the new game phase as well as the new turn player.
     * The new board and the new player data have to be passed by the user of the function.
     *
     * @param board          An updated board.
     * @param players        Updated player data.
     * @param lastGamePhase  The game phase from the predecessor state.
     * @param lastTurnPlayer The turn player from the predecessor state.
     * @param considerTurn   Pass {@code true} if you want the game phase and the turn player to be updated.
     */
    public State(
            Board board, PlayerPool players, GamePhase lastGamePhase, Player lastTurnPlayer, boolean considerTurn )
    {
        assert board != null;
        assert players != null;
        assert lastTurnPlayer != null;
        assert lastGamePhase != GamePhase.END;

        this.board = board;
        this.players = players;
        this.turnPlayer = players.getPlayer( lastTurnPlayer.getID() );
        // anstatt this.turnPlayer = lastTurnPlayer;
        this.phase = lastGamePhase;

        if ( considerTurn )
        {
            this.switchTurnPlayer();

            this.calculatePhaseAndTurnPlayer();
        }
    }

    /**
     * Calculates the next turn player and sets the phase accordingly if no moves are possible.
     */
    private void calculatePhaseAndTurnPlayer()
    {
        assert turnPlayer != null;
        assert phase != null;

        Player initialPlayer = this.turnPlayer;

        while ( !this.hasPossibleMove( this.turnPlayer ) )
        {
            this.switchTurnPlayer();

            if ( initialPlayer.equals( this.turnPlayer ) )
            {
                if ( this.phase == GamePhase.PHASE1 )
                {
                    this.phase = GamePhase.PHASE2;
                }
                else // this.phase == GamePhase.PHASE2
                {
                    this.phase = GamePhase.END;

                    return;
                }
            }
        }
    }

    /**
     * Switches the turn player to next player who is not disqualified.
     */
    void switchTurnPlayer()
    {
        if ( this.turnPlayer != null )
        {
            this.turnPlayer.setTurn( false );

            int turn = this.turnPlayer.getID();

            do
            {
                turn = ( turn % this.players.getNumberOfPlayers() ) + 1;
            }
            while ( turn != this.turnPlayer.getID() && this.players.getPlayer( turn ).disqualified() );

            if ( turn != this.turnPlayer.getID() )
            {
                this.turnPlayer = this.players.getPlayer( turn );

                this.turnPlayer.setTurn( true );
            }
        }
    }

    /**
     * Disqualifies a player and updates phase and turn player, if necessary.
     *
     * @param playerID The player's ID.
     */
    public void disqualify( byte playerID )
    {
        this.players.disqualify( playerID );

        if ( this.getTurnPlayer().getID() == playerID )
        {
            this.switchTurnPlayer();
        }

        this.validMoves = null;

        this.calculatePhaseAndTurnPlayer();
    }

    /**
     * Builds a move at a given position with respect to current game phase.
     *
     * @param x,y  The desired position on the board.
     * @param pref A BonusPreference for a bonus tile or a preference which player id to
     *             to switch stones with (given as Byte).
     * @return A move, {@code null} for a wrong preference or a move type that does not fit to the current game phase.
     */
    public Move buildMove( int x, int y, Object pref )
    {
        Move.Type moveType = this.getMoveType( x, y );

        return buildMove( moveType, x, y, pref );
    }

    /**
     * Builds a move at a given position with respect to current game phase.
     *
     * @param moveType The desired type of the move.
     * @param x,y      The desired position on the board.
     * @param pref     A BonusPreference for a bonus tile or a preference which player id to
     *                 to switch stones with (given as Byte).
     * @return a move, {@code null} for a wrong preference or a move type that does not fit to the current game phase.
     */
    private Move buildMove( Move.Type moveType, int x, int y, Object pref )
    {
        if ( ( moveType != Move.Type.BombMove && this.isBombingPhase() )
             || ( moveType == Move.Type.BombMove && !this.isBombingPhase() ) )
        {
            return null;
        }

        switch ( moveType )
        {
            case StandardStoneMove:
            case InversionTileMove:
                return new StandardStoneMove( this, x, y );

            case BonusTileMove:
                if ( pref instanceof StandardStoneMove.BonusPref )
                {
                    return new StandardStoneMove( this, x, y, (StandardStoneMove.BonusPref) pref );
                }
                else if ( StandardStoneMove.BonusPref.createBonusPref( (byte) pref ) !=
                          StandardStoneMove.BonusPref.NONE )
                {
                    return new StandardStoneMove( this, x, y,
                                                  StandardStoneMove.BonusPref.createBonusPref( (byte) pref ) );
                }
                else
                {
                    return null;
                }

            case ChoiceTileMove:
                if ( !( pref instanceof StandardStoneMove.BonusPref ) )
                {
                    return new StandardStoneMove( this, x, y, (byte) pref );
                }
                else
                {
                    return null;
                }

            case OverrideStoneMove:
                return new OverrideStoneMove( this, x, y );

            case BombMove:
                return new BombMove( this, x, y );

            default:
                throw new RuntimeException( "This should not happen!" );
        }
    }

    /**
     * Calculates all valid moves that might be performed by the specified player.
     *
     * @param player The player whose possible moves ought to be calculated.
     * @return the set of all possible moves that might be performed by the specified player.
     */
    public Set< Move > calcValidMoves( Player player )
    {
        if ( this.isOver() || this.isBombingPhase() && !player.hasBomb() )
        {
            // If the game is over or if the player does not have any bombs in bombing phase, no move is possible
            return new HashSet<>();
        }

        Set< Move > validMoves = new HashSet<>();

        BoardIterator iterator = this.board.getBoardIterator();

        RayIterator rayIterator = this.board.getRayIterator();

        // Iterate over all the position on the board
        for ( int y = 0; y < this.board.getHeight(); y++ )
        {
            for ( int x = 0; x < this.board.getWidth(); x++ )
            {
                iterator.moveTo( x, y );

                if ( this.isBombingPhase() )
                {
                    if ( iterator.getTileType() != TileType.ABSENT )
                    {
                        // One may drop a bomb on every tile that is not a hole during bombing phase
                        validMoves.add( new BombMove( this, x, y ) );
                    }

                    // There are no other moves in bombing phase
                    continue;
                }

                if ( iterator.getOccupant() == player.getID() )
                {
                    // Every stone of the turn player has the potential to capture other stones, so cast rays in all
                    // eight directions to find stones that can be captured
                    rayIterator.moveTo( x, y );

                    rays:
                    for ( Direction direction : Direction.values() )
                    {
                        rayIterator.reset( direction );

                        // One must capture at least one stone of an opponent in order to perform a valid move
                        if ( rayIterator.hasNext() )
                        {
                            rayIterator.next();

                            if ( !rayIterator.isOccupied() || rayIterator.getOccupant() == player.getID() )
                            {
                                continue rays;
                            }
                        }

                        // Find all possible captures on this ray
                        ray:
                        while ( rayIterator.hasNext() )
                        {
                            rayIterator.next();

                            // If the ray returned to the starting point, capturing is not possible
                            if ( rayIterator.getX() == x && rayIterator.getY() == y )
                            {
                                break ray;
                            }

                            // Capture via override stone
                            if ( rayIterator.isOccupied() )
                            {
                                if ( rayIterator.getOccupant() == player.getID() )
                                {
                                    if ( player.hasOverrideStone() )
                                    {
                                        // One may place an override stone on another players stone if at least one
                                        // opponent stone is captured
                                        validMoves.add( new OverrideStoneMove( this, rayIterator.getX(),
                                                                               rayIterator.getY() ) );
                                    }

                                    // One can not capture one's own stones which is why the ray needs to end after
                                    // we found the first one of our own stones
                                    break ray;
                                }
                                else
                                {
                                    if ( player.hasOverrideStone() )
                                    {
                                        // One may place an override stone on another players stone if at least one
                                        // opponent stone is captured
                                        validMoves.add( new OverrideStoneMove( this, rayIterator.getX(),
                                                                               rayIterator.getY() ) );
                                    }

                                    // There might still be capturable stones after this one, so we continue casting the
                                    // ray
                                    continue ray;
                                }
                            }
                            else // !rayIterator.isOccupied()
                            {
                                // We know that a continuous line of opponent stones is capturable, so the type of move
                                // allowed depends solely on the type of the free tile
                                switch ( rayIterator.getTileType() )
                                {
                                    case STANDARD:
                                    case INVERSION:
                                    {
                                        // Just a regular move is possible
                                        validMoves.add( new StandardStoneMove( this, rayIterator.getX(),
                                                                               rayIterator.getY() ) );

                                        break;
                                    }

                                    case BONUS:
                                    {
                                        // One may choose either a bomb or an override stone
                                        validMoves.add( new StandardStoneMove( this, rayIterator.getX(),
                                                                               rayIterator.getY(),
                                                                               StandardStoneMove.BonusPref.BOMB ) );

                                        validMoves.add( new StandardStoneMove( this, rayIterator.getX(),
                                                                               rayIterator.getY(),
                                                                               StandardStoneMove.BonusPref.OVERRIDE ) );
                                        break;
                                    }

                                    case CHOICE:
                                    {
                                        // One may exchange stones with each other player (including oneself!)
                                        for ( Player other : this.players )
                                        {
                                            validMoves.add( new StandardStoneMove( this, rayIterator.getX(),
                                                                                   rayIterator.getY(),
                                                                                   other.getID() ) );
                                        }

                                        break;
                                    }
                                }

                                // One cannot capture an empty tile, so break the ray
                                break ray;
                            }
                        }
                    }
                }
                else if ( iterator.isOccupiedByExpansionStone() && player.hasOverrideStone() )
                {
                    // One may place an override stone on an expansion stone
                    validMoves.add( new OverrideStoneMove( this, x, y ) );
                }
            }
        }

        return validMoves;
    }

    /**
     * @param player The player whose possible moves ought to be calculated.
     * @return {@code true} if the passed player has at least one valid move.
     */
    public boolean hasPossibleMove( Player player )
    {
        if ( this.isOver() || this.isBombingPhase() && !player.hasBomb() )
        {
            // If the game is over or if the player does not have any bombs in bombing phase, no move is possible
            return false;
        }

        BoardIterator iterator = this.board.getBoardIterator();

        RayIterator rayIterator = this.board.getRayIterator();

        // Iterate over all the position on the board
        for ( int y = 0; y < this.board.getHeight(); y++ )
        {
            for ( int x = 0; x < this.board.getWidth(); x++ )
            {
                iterator.moveTo( x, y );

                if ( this.isBombingPhase() )
                {
                    if ( iterator.getTileType() != TileType.ABSENT )
                    {
                        // One may drop a bomb on every tile that is not a hole during bombing phase
                        return true;
                    }

                    // There are no other moves in bombing phase
                    continue;
                }

                if ( iterator.getOccupant() == player.getID() )
                {
                    // Every stone of the turn player has the potential to capture other stones, so cast rays in all
                    // eight directions to find stones that can be captured
                    rayIterator.moveTo( x, y );

                    rays:
                    for ( Direction direction : Direction.values() )
                    {
                        rayIterator.reset( direction );

                        // One must capture at least one stone of an opponent in order to perform a valid move
                        if ( rayIterator.hasNext() )
                        {
                            rayIterator.next();

                            if ( !rayIterator.isOccupied() || rayIterator.getOccupant() == player.getID() )
                            {
                                continue rays;
                            }
                        }

                        // Find all possible captures on this ray
                        ray:
                        while ( rayIterator.hasNext() )
                        {
                            rayIterator.next();

                            // If the ray returned to the starting point, capturing is not possible
                            if ( rayIterator.getX() == x && rayIterator.getY() == y )
                            {
                                break ray;
                            }

                            // Capture via override stone
                            if ( rayIterator.isOccupied() )
                            {
                                if ( rayIterator.getOccupant() == player.getID() )
                                {
                                    if ( player.hasOverrideStone() )
                                    {
                                        // One may place an override stone on another players stone if at least one
                                        // opponent stone is captured
                                        return true;
                                    }

                                    // One can not capture one's own stones which is why the ray needs to end after
                                    // we found the first one of our own stones
                                    break ray;
                                }
                                else
                                {
                                    if ( player.hasOverrideStone() )
                                    {
                                        // One may place an override stone on another players stone if at least one
                                        // opponent stone is captured
                                        return true;
                                    }

                                    // There might still be capturable stones after this one, so we continue casting the
                                    // ray
                                    continue ray;
                                }
                            }
                            else // !rayIterator.isOccupied()
                            {
                                // We know that a continuous line of opponent stones is capturable
                                return true;
                            }
                        }
                    }
                }
                else if ( iterator.isOccupiedByExpansionStone() && player.hasOverrideStone() )
                {
                    // One may place an override stone on an expansion stone
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @return all valid moves for the player who is about to move (players.getTurnPlayer()).
     */
    public Set< Move > getAllValidMoves()
    {
        if ( this.validMoves == null )
        {
            this.validMoves = this.calcValidMoves( this.getTurnPlayer() );
        }

        return this.validMoves;
    }

    /**
     * @return the board of the game.
     */
    public Board getBoard()
    {
        return this.board;
    }

    /**
     * @return {@code true}, if the state is currently in phase 2 (bombing phase).
     */
    public boolean isBombingPhase()
    {
        return this.phase == GamePhase.PHASE2 || this.phase == GamePhase.END;
    }

    /**
     * @return {@code true} if the game is over.
     */
    public boolean isOver()
    {
        return this.phase == GamePhase.END;
    }

    /**
     * Calculates the move type which is feasible at a given position with respect to current game phase.
     *
     * @param x,y The desired position on the board.
     * @return the move type feasible at this position.
     * @throws IndexOutOfBoundsException     If position is not on the board.
     * @throws UnsupportedOperationException If position is a hole.
     */
    public Move.Type getMoveType( int x, int y ) throws IndexOutOfBoundsException, UnsupportedOperationException
    {
        if ( !this.board.hasPosition( x, y ) )
        {
            LogTopic.error.error( "There is no position (%d, %d) on the board!", x, y );
            return Move.Type.InvalidMove;
        }

        BoardIterator iterator = this.board.getBoardIterator( x, y );

        if ( !this.isBombingPhase() )
        {
            switch ( iterator.getTileType() )
            {
                case STANDARD:
                    return ( !iterator.isOccupied() ) ? Move.Type.StandardStoneMove : Move.Type.OverrideStoneMove;

                case CHOICE:
                    return Move.Type.ChoiceTileMove;

                case INVERSION:
                    return Move.Type.InversionTileMove;

                case BONUS:
                    return Move.Type.BonusTileMove;

                case ABSENT:
                    throw new UnsupportedOperationException(
                            "Placing stone to position (" + x + ", " + y + ") is not a valid move!"
                    );

                default:
                    throw new RuntimeException( "This should not happen!" );
            }
        }
        else
        {
            if ( iterator.getTileType() == TileType.ABSENT )
            {
                throw new UnsupportedOperationException(
                        "Placing stone to position (" + x + ", " + y + ") is not a valid move!"
                );
            }
            else
                return Move.Type.BombMove;
        }
    }

    /**
     * @return the player pool of the game.
     */
    public PlayerPool getPlayers()
    {
        return this.players;
    }

    /**
     * @return the player who is about to move, {@code null} if all players are disqualified.
     */
    public Player getTurnPlayer()
    {
        return this.turnPlayer;
    }

    /**
     * @return the current game phase.
     */
    public GamePhase getPhase()
    {
        return this.phase;
    }

    /**
     * Method to display the state.
     */
    public String toString()
    {
        String out = this.players.toString();
        out += "\n" + this.board.toString();
        out += "\nGamephase: ";
        if ( this.isOver() )
            out += "End";
        else if ( this.isBombingPhase() )
            out += "Bombingphase";
        else
            out += "Standardphase";
        out += "\n";
        return out;
    }
}
