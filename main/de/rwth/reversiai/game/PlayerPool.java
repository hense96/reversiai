package de.rwth.reversiai.game;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A PlayerPool maintains all player data taking part in a game. The class should only be used to maintain
 * <strong>every player's</strong> data, so do not use this for subsets of players. For such subsets (e.g.
 * all disqualified players), use {@link java.util.Set}.
 *
 * @author Julius Hense
 * @see Player
 */
public class PlayerPool implements Iterable< Player >
{
    /**
     * Array containing all player data. It should neither be empty nor modified.
     * For efficiency, the condition
     * {@code ( player.getID() == (arrayindex + 1) )}
     * always needs to be ensured.
     */
    private final Player[] players;

    /**
     * Initializes player data. Use only if using copy constructor is not possible.
     * Initially, the first player is about to move.
     *
     * @param players An array containing all players. The array should at least contain one player.
     *                Moreover, no player should be null. For every player the condition
     *                {@code ( player.getID() == (arrayindex + 1) )}
     *                should hold.
     */
    public PlayerPool( Player[] players )
    {
        /* check array */
        assert players != null && players.length > 0;

        for ( int i = 0; i < players.length; ++i )
            assert players[ i ] != null && players[ i ].getID() == i + 1;

        /* initialize values */
        this.players = players;
    }

    /**
     * Copy constructor.
     *
     * @param toCopy A PlayerPool, should be not {@code null}.
     */
    public PlayerPool( PlayerPool toCopy )
    {
        assert toCopy != null;

        this.players = new Player[ toCopy.players.length ];

        for ( int i = 0; i < this.players.length; ++i )
        {
            this.players[ i ] = new Player( toCopy.players[ i ] );
        }
    }

    /**
     * @param playerID A player ID.
     * @return {@code true} if the pool contains a player with this ID.
     */
    public boolean contains( int playerID )
    {
        return 1 <= playerID && playerID <= this.getNumberOfPlayers();
    }

    /**
     * @return set of all players who are not disqualified.
     */
    public Set< Player > getActivePlayers()
    {
        HashSet< Player > activePlayers = new HashSet<>();
        for ( Player player : this )
        {
            if ( !player.disqualified() )
                activePlayers.add( player );
        }
        return activePlayers;
    }

    /**
     * @return set of all players.
     */
    public Set< Player > getAllPlayers()
    {
        HashSet< Player > allPlayers = new HashSet<>();
        for ( Player player : this )
        {
            allPlayers.add( player );
        }
        return allPlayers;
    }

    /**
     * @return set of all players who are disqualified.
     */
    public Set< Player > getDisqualifiedPlayers()
    {
        HashSet< Player > disqPlayers = new HashSet<>();
        for ( Player player : this )
        {
            if ( player.disqualified() )
                disqPlayers.add( player );
        }
        return disqPlayers;
    }

    /**
     * @return number of players stored in this pool.
     */
    public int getNumberOfPlayers()
    {
        return players.length;
    }

    /**
     * @param id The ID of a player.
     * @return the {@code Player} object.
     */
    public Player getPlayer( int id )
    {
        assert this.contains( id ) : id;

        return players[ id - 1 ];
    }

    /**
     * Disqualifies the specified player and switches the turn Player
     *
     * @param id Player ID to be disqualified.
     */
    void disqualify( int id )
    {
        this.getPlayer( id ).disqualify();
    }

    /**
     * Method to display information about the players.
     */
    public String toString()
    {
        StringBuilder out = new StringBuilder();

        for ( int i = 0; i < players.length; i++ )
        {
            out.append( players[ i ].toString() );
            if ( i + 1 < players.length )
            {
                out.append( '\n' );
            }
        }

        return out.toString();
    }

    /**
     * @return Iterator to access all player data stored in the PlayerPool ordered by player ID.
     * <p>
     * {@code remove()} method is unsupported.
     */
    @Override
    public Iterator< Player > iterator()
    {
        return new Iterator< Player >()
        {
            private int pos = 0;

            @Override
            public boolean hasNext()
            {
                return pos < players.length;
            }

            @Override
            public Player next()
            {
                return players[ pos++ ];
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException( "Cannot remove a player from player pool!" );
            }
        };
    }
}