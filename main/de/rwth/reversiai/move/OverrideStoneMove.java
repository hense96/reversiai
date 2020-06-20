package de.rwth.reversiai.move;

import de.rwth.reversiai.board.Board;
import de.rwth.reversiai.board.TileType;
import de.rwth.reversiai.board.iterator.BoardIterator;
import de.rwth.reversiai.game.PlayerPool;
import de.rwth.reversiai.game.State;

/**
 * Representation of an override stone move, i.e. placing an override stone on a captured tile.
 *
 * @author Julius Hense
 */
public class OverrideStoneMove extends StoneMove
{
    /**
     * Player executing the move is automatically set to player which is about to move.
     * Player who is about to move will be updated in execute() method.
     *
     * @param state a state containing a board and player data (not null and not empty)
     * @param x,y   a valid position on this board
     */
    public OverrideStoneMove( State state, int x, int y )
    {
        super( state, x, y );
    }

    /**
     * Method for calculating the state resulting from executing this override stone move.
     * Precondition: move is valid.
     *
     * @return resulting state
     */
    public State execute()
    {
        assert this.isValid() : this.toString();

        /* derive new board */
        Board resultBoard = new Board( this.state.getBoard(), false );
        this.capture( resultBoard );

        /* derive new player data */
        PlayerPool resultPlayers = new PlayerPool( this.state.getPlayers() );
        resultPlayers.getPlayer( player.getID() ).useOverrideStone();

        return new State( resultBoard, resultPlayers, this.state.getPhase(), this.state.getTurnPlayer(),
                          this.considerTurn );
    }

    /**
     * This method decides whether an override stone move is valid or not.
     *
     * @return true if the move is valid.
     */
    public boolean isValid()
    {
        BoardIterator iterator = this.state.getBoard().getBoardIterator( x, y );

        /* valid if tile is an expansion tile or
         * if it is occupied and there is the possibility of capturing
         * furthermore, the player needs to have an override stone */
        if ( iterator.getTileType() == TileType.ABSENT || !player.hasOverrideStone() )
        {
            return false;
        }
        // expansion rule
        else if ( iterator.isOccupiedByExpansionStone() )
        {
            return true;
        }
        else
        {
            return iterator.isOccupied() && this.hasCapturingDirection();
        }
    }
}