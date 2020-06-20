package de.rwth.reversiai.search.nodeeval;

import de.rwth.reversiai.game.Player;

/**
 * A double value between zero and one where one player is the MAX player (i.e. the higher the value the better)
 * and all other players are MIN players (i.e. the lower the value the better).
 */
public class ParanoidDouble extends NodeEval
{
    /**
     * The double value between 0 and 1.
     */
    private double value;

    /**
     * The MAX player, i.e. the player who prefers high double values.
     */
    private Player maxPlayer;

    /**
     * @param value     A value.
     * @param maxPlayer The MAX player.
     */
    public ParanoidDouble( double value, Player maxPlayer )
    {
        this.value = value;
        this.maxPlayer = maxPlayer;
    }

    @Override
    public int compareTo( NodeEval otherEval, Player perspective )
    {
        assert otherEval instanceof ParanoidDouble;

        double otherValue = ( (ParanoidDouble) otherEval ).getValue();
        int result;

        if ( this.value < otherValue )
        {
            result = 1;
        }
        else if ( this.value == otherValue )
        {
            result = 0;
        }
        else
        {
            result = -1;
        }

        if ( perspective.equals( this.maxPlayer ) )
        {
            result = result * ( -1 );
        }

        return result;
    }

    /**
     * @return the double value.
     */
    public double getValue()
    {
        return this.value;
    }

    /**
     * @return the max player.
     */
    public Player getMaxPlayer()
    {
        return this.maxPlayer;
    }

    @Override
    public boolean haveSameStrategy( Player player1, Player player2 )
    {
        return ( ( !player1.equals( this.maxPlayer ) && !player2.equals( this.maxPlayer ) )
                 || ( player1.equals( player2 ) ) );
    }

    @Override
    public boolean isBetter( NodeEval otherEval, Player perspective )
    {
        return this.compareTo( otherEval, perspective ) == 1;
    }

    @Override
    public boolean isEqual( NodeEval otherEval, Player perspective )
    {
        return this.compareTo( otherEval, perspective ) == 0;
    }

    @Override
    public boolean isWorse( NodeEval otherEval, Player perspective )
    {
        return this.compareTo( otherEval, perspective ) == -1;
    }

    @Override
    public String toString()
    {
        return "" + this.getValue();
    }
}
