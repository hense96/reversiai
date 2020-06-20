package de.rwth.reversiai.search.window;


import de.rwth.reversiai.game.Player;
import de.rwth.reversiai.search.GameTree;
import de.rwth.reversiai.search.nodeeval.ParanoidDouble;

import java.util.HashMap;

/**
 * See constructor for description.
 */
public class HardDeltaDoubleWindow extends AspirationWindow< ParanoidDouble >
{
    private Player maxPlayer;
    private Player minPlayer;
    private double deltaMax;
    private double deltaMin;
    private int maxWidth;

    /**
     * Creates a HardDeltaDoubleWindow. You can pass the deltas you accept for min and max players.
     * If the aspiration window is too narrow once, the window is widened by the respective deltas.
     *
     * @param deltaMax how much lower than the last depth's best evaluation may this evaluation be?
     * @param deltaMin how much higher than the last depth's best evaluation may this evaluation be?
     * @param maxWidth how often may the window be widened until you want to do alphabeta search without
     *                 aspiration window?
     */
    public HardDeltaDoubleWindow( double deltaMax, double deltaMin, int maxWidth )
    {
        assert deltaMax >= 0 && deltaMax <= 1;
        assert deltaMin >= 0 && deltaMin <= 1;
        assert maxWidth >= 1;

        this.evalList = new HashMap<>();

        this.maxPlayer = null;
        this.minPlayer = null;
        this.deltaMax = deltaMax;
        this.deltaMin = deltaMin;
        this.maxWidth = maxWidth;
    }

    @Override
    public HashMap< Player, ParanoidDouble > getWindow( GameTree.Node node )
    {
        if ( this.searchDepth == 0 || this.callcounter > this.maxWidth )
            return this.openWindow();

        if ( this.maxPlayer == null )
        {
            this.maxPlayer = this.gameTree.getMaxPlayer();
            this.minPlayer = maxPlayer.getID() != 1 ? node.getState().getPlayers().getPlayer( 1 ) :
                             node.getState().getPlayers().getPlayer( 2 );
        }

        ParanoidDouble refEval = this.evalList.get( this.searchDepth );

        HashMap< Player, ParanoidDouble > window = new HashMap<>();

        /* calculate window bound for the max player */
        double windowMax = refEval.getValue() - deltaMax * this.callcounter;
        windowMax = windowMax > 0.0 ? windowMax : 0.0;
        window.put( this.maxPlayer, new ParanoidDouble( windowMax, this.maxPlayer ) );

        /* calculate window bound for the min player(s) */
        double windowMin = refEval.getValue() + deltaMin * this.callcounter;
        windowMin = windowMin < 1.0 ? windowMin : 1.0;
        window.put( this.minPlayer, new ParanoidDouble( windowMin, this.maxPlayer ) );

        return window;
    }

    public double getDeltaMax()
    {
        return deltaMax;
    }

    public double getDeltaMin()
    {
        return deltaMin;
    }

    public void setDeltaMax( double deltaMax )
    {
        this.deltaMax = deltaMax;
    }

    public void setDeltaMin( double deltaMin )
    {
        this.deltaMin = deltaMin;
    }
}
