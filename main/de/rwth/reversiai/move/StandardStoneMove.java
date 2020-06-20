package de.rwth.reversiai.move;

import de.rwth.reversiai.board.Board;
import de.rwth.reversiai.board.TileType;
import de.rwth.reversiai.board.iterator.BoardIterator;
import de.rwth.reversiai.game.Player;
import de.rwth.reversiai.game.PlayerPool;
import de.rwth.reversiai.game.State;

/**
 * Representation of a standard stone move, i.e. placing a stone on an uncaptured tile
 * and capturing other stones or extension tiles with this.
 *
 * @author Julius Hense
 */
public class StandardStoneMove extends StoneMove
{
    /**
     * Enum for storing information about the preferences when a bonus tile
     * is captured.
     */
    public enum BonusPref
    {
        NONE( (byte) 0 ),
        OVERRIDE( (byte) 21 ),
        BOMB( (byte) 20 );

        private byte type;

        BonusPref( byte type )
        {
            this.type = type;
        }

        public byte getType()
        {
            return type;
        }

        public static BonusPref createBonusPref( byte b )
        {
            switch ( b )
            {
                case 0x14:
                    return BOMB;
                case 0x15:
                    return OVERRIDE;
                default:
                    return NONE;
            }
        }
    }

    /**
     * Stores information about what to chose if a bonus tile is captured.
     * Needs to be assigned to BonusPref.NONE if move is not performed on a bonus tile.
     */
    private final BonusPref bonusPref;

    /**
     * Stores information about which player to switch colors with if a choice tile is captured.
     * Needs to be assigned to 0 if move is not performed on a choice tile.
     */
    private final byte choicePref;

    /**
     * Constructor for placing a standard stone on a standard or inversion tile.
     * <p>
     * Player executing the move is automatically set to player which is about to move.
     * Player who is about to move will be updated in execute() method.
     *
     * @param state a state containing a board and player data (not null and not empty)
     * @param x,y   a valid position on this board
     */
    public StandardStoneMove( State state, int x, int y )
    {
        super( state, x, y );

        this.bonusPref = BonusPref.NONE;
        this.choicePref = 0;
    }

    /**
     * Constructor for a placing a standard stone on a bonus tile.
     * <p>
     * Player executing the move is automatically set to player which is about to move.
     * Player who is about to move will be updated in execute() method.
     *
     * @param state     a state containing a board and player data (not null and not empty)
     * @param x,y       a valid position on this board
     * @param bonusPref a bonus tile preference (not NONE)
     */
    public StandardStoneMove( State state, int x, int y, BonusPref bonusPref )
    {
        super( state, x, y );

        assert bonusPref == BonusPref.OVERRIDE || bonusPref == BonusPref.BOMB;

        this.bonusPref = bonusPref;
        this.choicePref = 0;
    }

    /**
     * Constructor for a placing a standard stone on a choice tile.
     * <p>
     * Player executing the move is automatically set to player which is about to move.
     * Player who is about to move will be updated in execute() method.
     *
     * @param state      a state containing a board and player data (not null and not empty)
     * @param x,y        a valid position on this board
     * @param choicePref a choice tile preference (valid player id)
     */
    public StandardStoneMove( State state, int x, int y, byte choicePref )
    {
        super( state, x, y );

        assert 1 <= choicePref && choicePref <= this.state.getBoard().getPlayers();

        this.bonusPref = BonusPref.NONE;
        this.choicePref = choicePref;
    }

    /**
     * Method for calculating the state resulting from executing this standard stone move.
     * Precondition: move is valid.
     *
     * @return resulting state
     */
    public State execute()
    {
        assert this.isValid();

        /* derive new board */
        Board resultBoard = new Board( this.state.getBoard(), false );
        this.capture( resultBoard );

        /* derive new player data */
        PlayerPool resultPlayers = new PlayerPool( this.state.getPlayers() );
        Player thisResultPlayer = resultPlayers.getPlayer( this.player.getID() );

        /* consider special tiles */
        BoardIterator iterator = resultBoard.getBoardIterator( this.x, this.y );
        TileType type = iterator.getTileType();

        if ( type == TileType.CHOICE )
        {
            /* if choice tile, switch all stones with choicePref */

            /* iterate over all tiles */
            for ( int y = 0; y < resultBoard.getHeight(); ++y )
            {
                for ( int x = 0; x < resultBoard.getWidth(); ++x )
                {
                    iterator.moveTo( x, y );
                    if ( iterator.getTileType() != TileType.ABSENT && iterator.isOccupied() )
                    {
                        if ( iterator.getOccupant() == thisResultPlayer.getID() )
                        {
                            iterator.setOccupant( choicePref );
                        }
                        else if ( iterator.getOccupant() == choicePref )
                        {
                            iterator.setOccupant( thisResultPlayer.getID() );
                        }
                    }
                }
            }

            /* tile turns to standard tile */
            iterator.moveTo( this.x, this.y );
            iterator.setTileType( TileType.STANDARD );
        }
        else if ( type == TileType.INVERSION )
        {
            /* if inversion tile, switch all stones according to specification */

            /* iterate over all tiles */
            for ( int y = 0; y < resultBoard.getHeight(); ++y )
            {
                for ( int x = 0; x < resultBoard.getWidth(); ++x )
                {
                    iterator.moveTo( x, y );
                    if ( iterator.getTileType() != TileType.ABSENT && iterator.isOccupied() )
                    {
                        /* avoid that tile is an uncaptured expansion tile */
                        if ( !iterator.isOccupiedByExpansionStone() )
                        {
                            iterator.setOccupant(
                                    (byte) ( ( iterator.getOccupant() % this.state.getBoard().getPlayers() ) + 1 ) );
                        }
                    }
                }
            }

            /* tile turns to standard tile */
            iterator.moveTo( this.x, this.y );
            iterator.setTileType( TileType.STANDARD );
        }
        else if ( type == TileType.BONUS )
        {
            /* if bonus tile, add the chosen bonus element to the executing player */

            if ( this.bonusPref == BonusPref.OVERRIDE )
            {
                thisResultPlayer.addOverrideStone();
            }
            else
            {
                thisResultPlayer.addBomb();
            }
            iterator.setTileType( TileType.STANDARD );
        }

        return new State( resultBoard, resultPlayers, this.state.getPhase(), this.state.getTurnPlayer(),
                          this.considerTurn );
    }

    /**
     * This method decides whether a standard stone move is valid or not.
     * A standard stone move is considered as invalid if a constructor not fitting
     * to the respective tile was used.
     *
     * @return true if the move is valid.
     */
    public boolean isValid()
    {
        BoardIterator iterator = this.state.getBoard().getBoardIterator( this.x, this.y );
        TileType type = iterator.getTileType();

        /* possibly valid if tile is not a hole, tile is free and there is the possibility of capturing stones */
        if ( type == TileType.ABSENT || iterator.isOccupied() || !this.hasCapturingDirection() )
        {
            return false;
        }
        else
        {
            /* valid if additionally the move holds the correct information for the tile */
            if ( ( type == TileType.STANDARD && this.bonusPref == BonusPref.NONE && this.choicePref == 0 ||
                   type == TileType.INVERSION && this.bonusPref == BonusPref.NONE && this.choicePref == 0 )
                 || ( type == TileType.BONUS && this.bonusPref != BonusPref.NONE && this.choicePref == 0 )
                 || ( type == TileType.CHOICE && this.bonusPref == BonusPref.NONE && this.choicePref != 0 ) )
            {
                return true;
            }
            return false;
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
        int prefValue = 0;

        if ( this.choicePref != 0 )
            prefValue = this.choicePref;
        else if ( this.bonusPref == BonusPref.BOMB )
            prefValue = 9;
        else if ( this.bonusPref == BonusPref.OVERRIDE )
            prefValue = 10;

        // super.hashCode() uses the last 15 binary digits
        // 0 <= prefValue <= 10 < 15 = 2^4 - 1
        // Thus, this hashCode uses the last 19 binary digits
        return super.hashCode() | prefValue << 15;
    }

    /**
     * @return the preference for bonus tiles of the move.
     */
    public BonusPref getBonusPref()
    {
        return bonusPref;
    }

    /**
     * @return the preference for choice tiles of the move.
     */
    public byte getChoicePref()
    {
        return choicePref;
    }
}
