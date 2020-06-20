package de.rwth.reversiai.move;

import de.rwth.reversiai.board.Board;
import de.rwth.reversiai.board.TileType;
import de.rwth.reversiai.board.iterator.BFSBoardIterator;
import de.rwth.reversiai.board.iterator.BoardIterator;
import de.rwth.reversiai.game.PlayerPool;
import de.rwth.reversiai.game.State;

/**
 * Representation of a bomb move, i.e. dropping a bomb on a tile.
 *
 * @author Julius Hense
 */
public class BombMove extends Move
{
    /**
     * Player executing the move is automatically set to player which is about to move.
     * Player who is about to move will be updated in execute() method.
     *
     * @param state a state containing a board and player data (not null and not empty)
     * @param x,y   a valid position on this board
     */
    public BombMove( State state, int x, int y )
    {
        super( state, x, y );
    }

    /**
     * Method for bombing tiles.
     * All tiles reachable within radius steps turn into holes.
     *
     * @param resultBoard board to perform bombing on
     * @param x,y         center of bombing
     * @param radius      radius of bombing
     */
    private void bombTiles( Board resultBoard, int x, int y, int radius )
    {
        assert resultBoard != null;
        assert state.getBoard().hasPosition( x, y );
        assert radius >= 0;

        BFSBoardIterator iterator = resultBoard.getBFSBoardIterator( x, y, radius );

        while ( iterator.hasNext() )
        {
            iterator.next();

            iterator.makeHole();
        }
    }

    /**
     * Method for calculating the state resulting from executing this bomb move.
     * Precondition: move is valid.
     *
     * @return resulting state
     */
    public State execute()
    {
        assert this.isValid();

        /* derive new board */
        Board resultBoard = new Board( this.state.getBoard(), true );
        this.bombTiles( resultBoard, this.x, this.y, this.state.getBoard().getBombRadius() );

        /* derive new player data */
        PlayerPool resultPlayers = new PlayerPool( this.state.getPlayers() );
        resultPlayers.getPlayer( player.getID() ).useBomb();

        return new State( resultBoard, resultPlayers, this.state.getPhase(), this.state.getTurnPlayer(),
                          this.considerTurn );
    }

    /**
     * This method decides whether a bomb move is valid or not.
     *
     * @return true if the move is valid.
     */
    public boolean isValid()
    {
        BoardIterator iterator = this.state.getBoard().getBoardIterator( this.x, this.y );

        /* valid if tile is no hole and player has a bomb */
        return ( iterator.getTileType() != TileType.ABSENT && this.player.hasBomb() );
    }

    /**
     * Calculates a hash code. Hash codes of two moves differ iff their position differs or the executing player differs
     * or the bonus or choice preference differs or if not both are bomb moves.
     *
     * @return a hash code
     */
    public int hashCode()
    {
        // super.hashCode() uses the last 15 binary digits and StandardStoneMove.hashCode() uses the last 19 binary
        // digits, so use the 20th bit
        return super.hashCode() | 1 << 19;
    }
}
