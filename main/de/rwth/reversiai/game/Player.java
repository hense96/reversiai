package de.rwth.reversiai.game;

/**
 * Class representation of a player.
 * A player has an unique ID and a number of override stones and bombs.
 * Moreover, a player may be allowed to move or disqualified.
 *
 * @author Julius Hense
 */
public class Player
{
    /**
     * The player ID. Two player objects are considered equal iff they have the same ID.
     */
    private final byte id;

    /**
     * Current number of override stones the player holds.
     */
    private int nOverrideStones;

    /**
     * Current number of bombs the player holds.
     */
    private int nBombs;

    /**
     * {@code true} if the player is disqualified.
     */
    private boolean disqualification;

    /**
     * {@code true} if the player is allowed to move.
     */
    private boolean turn = false;

    /**
     * Bombs and override stones are initialized to 0. The Player is moreover not disqualified.
     *
     * @param id A unique, valid id between 1 and 8.
     */
    public Player( byte id )
    {
        this( id, 0, 0 );
    }

    /**
     * Constructor to initialize a player. It is assumed that the player is not disqualified.
     *
     * @param id              A unique, valid id between 1 and 8.
     * @param nOverrideStones Initial number of override stones.
     * @param nBombs          Initial number of bombs.
     */
    public Player( byte id, int nOverrideStones, int nBombs )
    {
        assert 1 <= id && id <= 8;
        assert 0 <= nOverrideStones;
        assert 0 <= nBombs;

        this.id = id;
        this.nOverrideStones = nOverrideStones;
        this.nBombs = nBombs;
        this.disqualification = false;
    }

    /**
     * Copy constructor.
     *
     * @param toCopy The player object to be copied (not {@code null}).
     */
    public Player( Player toCopy )
    {
        assert toCopy != null;

        this.id = toCopy.id;
        this.nOverrideStones = toCopy.nOverrideStones;
        this.nBombs = toCopy.nBombs;
        this.disqualification = toCopy.disqualification;
    }

    /**
     * Increases number of override stones by one.
     */
    public void addOverrideStone()
    {
        ++nOverrideStones;
    }

    /**
     * Increases number of bombs by one.
     */
    public void addBomb()
    {
        ++nBombs;
    }

    /**
     * @return {@code true} if the player is disqualified.
     */
    public boolean disqualified()
    {
        return this.disqualification;
    }

    /**
     * Marks the player as disqualified.
     */
    public void disqualify()
    {
        this.disqualification = true;
    }

    /**
     * Returns true iff two players have the same ID.
     * Attention: two player objects with the same id that contain different amounts of bombs
     * or override stones or have a different disqualification status are considered equal!
     */
    @Override
    public boolean equals( Object obj )
    {
        if ( obj instanceof Player )
            return this.getID() == ( (Player) obj ).getID();
        else
            return false;
    }

    /**
     * @return the player's ID.
     */
    public byte getID()
    {
        return id;
    }

    /**
     * @return number of override stones.
     */
    public int getNOverrideStones()
    {
        return nOverrideStones;
    }

    /**
     * @return number of bombs.
     */
    public int getNBombs()
    {
        return nBombs;
    }

    /**
     * @return {@code true} if the player has at least one override stone.
     */
    public boolean hasOverrideStone()
    {
        return ( nOverrideStones > 0 );
    }

    /**
     * @return {@code true} if the player has at least one bomb.
     */
    public boolean hasBomb()
    {
        return ( nBombs > 0 );
    }

    /**
     * @return a String containing all relevant player data.
     */
    public String toString()
    {
        String out = ( this.turn ) ? "-> " : "";
        out += "Player " + this.getID() + ":" + ( this.disqualified() ? " is disqualified" :
                                                  ( " override (" + this.getNOverrideStones() + "), bombs ("
                                                    + this.getNBombs() + ")" ) );
        return out;
    }

    /**
     * @param turn Pass {@code true} if this player should become a turn player.
     */
    void setTurn( boolean turn )
    {
        this.turn = turn;
    }

    /**
     * Reduces the number of override stones by one.
     */
    public void useOverrideStone()
    {
        assert hasOverrideStone();

        --nOverrideStones;
    }

    /**
     * Reduces the number of bombs by one.
     */
    public void useBomb()
    {
        assert hasBomb();

        --nBombs;
    }
}
