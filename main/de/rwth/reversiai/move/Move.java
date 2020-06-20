package de.rwth.reversiai.move;

import de.rwth.reversiai.game.Player;
import de.rwth.reversiai.game.State;
import de.rwth.reversiai.net.MoveRespondPacket;
import de.rwth.reversiai.net.Packet;

/**
 * Abstract representation of a move in a game.
 * A move maintains a board, current player data (included in a state) and a related position where
 * the move is desired to be executed at as well as the player who is doing the move.
 *
 * @author Julius Hense
 */
public abstract class Move
{
    /**
     * The state a move may be performed in.
     */
    protected final State state;

    /**
     * The board position of the move.
     */
    protected final int x;

    /**
     * The board position of the move.
     */
    protected final int y;

    /**
     * The player performing the move.
     * Generally, this player equals the turn player stored in the state.
     * In some cases, however, it may be comfortable to simulate a move of another player,
     * what is also possible.
     */
    protected final Player player;

    /**
     * Flag that indicates whether this move considers the turn information,
     * i.e. the performing player is the turn player or the move is just a simulation.
     * If it is true, turn data will be updated when executing the move.
     */
    protected final boolean considerTurn;

    /**
     * Player executing the move is automatically set to player which is about to move.
     * Player who is about to move will be updated in execute() method.
     *
     * @param state a state containing a board and player data (all not null)
     * @param x,y   a valid position on this board
     */
    public Move( State state, int x, int y )
    {
        assert state != null && state.getBoard() != null && state.getPlayers() != null;
        assert state.getBoard().hasPosition( x, y );
        assert state.getTurnPlayer() != null
               && 1 <= state.getTurnPlayer().getID()
               && state.getTurnPlayer().getID() <= state.getBoard().getPlayers();

        this.state = state;
        this.x = x;
        this.y = y;
        this.player = state.getTurnPlayer();
        this.considerTurn = true;
    }

    /**
     * @return the state of the move
     */
    public State getState()
    {
        return this.state;
    }

    /**
     * @return position of the move
     */
    public int getX()
    {
        return this.x;
    }

    /**
     * @return position of the move
     */
    public int getY()
    {
        return this.y;
    }

    /**
     * @return player doing this move
     */
    public Player getPlayer()
    {
        return this.player;
    }

    /**
     * Interface method for calculating the state resulting from executing this move.
     *
     * @return resulting state
     */
    public abstract State execute();

    /**
     * Interface method that decides whether a move is valid or not.
     *
     * @return true if the move is valid.
     */
    public abstract boolean isValid();

    /**
     * Enum for the move type.
     */
    public enum Type
    {
        StandardStoneMove,
        OverrideStoneMove,
        BonusTileMove,
        ChoiceTileMove,
        InversionTileMove,
        BombMove,
        InvalidMove
    }

    /**
     * @return The Type of this Object
     */
    public Type getType()
    {
        if ( this instanceof BombMove )
        {
            return Type.BombMove;
        }
        else if ( this instanceof OverrideStoneMove )
        {
            return Type.OverrideStoneMove;
        }
        else if ( this instanceof StandardStoneMove )
        {
            if ( ( (StandardStoneMove) this ).getChoicePref() != 0x0 )
            {
                return Type.ChoiceTileMove;
            }
            else if ( !( (StandardStoneMove) this ).getBonusPref().equals( StandardStoneMove.BonusPref.NONE ) )
            {
                return Type.BonusTileMove;
            }
        }
        return Type.StandardStoneMove;
    }

    /**
     * Method to compare two moves. Moves are considered equal if they are on the same position, executed by the same
     * player and hold the same preferences.
     * <p>
     * The state and therefore the tile type is not considered! Therefore, only compare two moves that are created
     * in the same context, meaning that they are executed in the same game state.
     *
     * @param obj another object
     * @return true if the other object is a move and it is considered to equal this move
     */
    public boolean equals( Object obj )
    {
        if ( obj == null || !( obj instanceof Move ) )
        {
            return false;
        }
        else
        {
            return ( this.hashCode() == obj.hashCode() );
        }
    }

    /**
     * Calculates a hash code. Hash codes of two moves differ iff their position differs or the executing player differs
     * or the bonus or choice preference differs or if not both are bomb moves.
     *
     * @return a hash code
     */
    public int hashCode()
    {
        // 0 <= coordinate <= 49 <= 63 = 2^6 - 1
        // 0 <= playerID - 1 <= 7 = 2^3 - 1
        // Thus, this hash uses the last 15 binary digits
        return this.x | ( this.y << 6 ) | ( ( this.player.getID() - 1 ) << 12 );
    }

    /**
     * @return A String containing the data of the move.
     */
    public String toString()
    {
        String out = "";
        if ( !this.getType().equals( Type.StandardStoneMove ) && !this.getType().equals( Type.BombMove ) )
        {
            if ( this.getType().equals( Type.OverrideStoneMove ) )
            {
                out += "Player " + getPlayer().getID() + " has overridden a stone";
            }
            else if ( this.getType().equals( Type.BonusTileMove ) )
            {
                out += "Player " + getPlayer().getID() + " chose ";
                if ( ( (StandardStoneMove) this ).getBonusPref().equals( StandardStoneMove.BonusPref.BOMB ) )
                {

                    out += "a bomb";
                }
                else if ( ( (StandardStoneMove) this ).getBonusPref().equals( StandardStoneMove.BonusPref.OVERRIDE ) )
                {
                    out += "an override stone";
                }
            }
            else
            {
                out += "Player " + getPlayer().getID() + " swapped ";
                out += "colors with Player " + ( (StandardStoneMove) this ).getChoicePref();
            }
        }
        else if ( !this.getType().equals( Type.BombMove ) )
        {
            out += "Player " + getPlayer().getID() + " placed";
        }
        else
        {
            out += "Player " + getPlayer().getID() + " dropped a bomb";
        }
        out += " on (" + getX() + "," + getY() + ")";
        return out;
    }

    /**
     * @return A packet representing this move.
     */
    public Packet toPacket()
    {
        switch ( this.getType() )
        {
            case StandardStoneMove:
            case OverrideStoneMove:
            case BombMove:
            case InversionTileMove:
                return new MoveRespondPacket( this.x, this.y );

            case BonusTileMove:
                return new MoveRespondPacket( this.x, this.y, ( (StandardStoneMove) this ).getBonusPref() );

            case ChoiceTileMove:
                return new MoveRespondPacket( this.x, this.y, ( (StandardStoneMove) this ).getChoicePref() );

            case InvalidMove:
            default:
                throw new RuntimeException( "This should not happen!" );
        }
    }
}