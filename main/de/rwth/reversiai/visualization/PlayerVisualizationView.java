package de.rwth.reversiai.visualization;

import de.rwth.reversiai.game.Player;
import de.rwth.reversiai.game.PlayerPool;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Sub-view used to render player data to the GUI.
 *
 * @author Marvin Pf&ouml;rtner
 */
class PlayerVisualizationView extends JPanel
{
    /**
     * The player data that are currently rendered to the screen.
     */
    private PlayerPool players;

    /**
     * An array of GUI elements, each representing one of the players in the {@link PlayerPool}.
     */
    private PlayerPanel[] playerPanels;

    /**
     * Constructs a new {@link PlayerVisualizationView} for the given {@link PlayerPool} and turn player.
     *
     * @param players    The player data to be visualized.
     * @param turnPlayer The current turn player.
     */
    PlayerVisualizationView( PlayerPool players, Player turnPlayer )
    {
        this.players = players;

        int numPlayers = this.players.getNumberOfPlayers();

        this.playerPanels = new PlayerPanel[ numPlayers ];

        this.setBorder( new EmptyBorder( 0, 10, 0, 10 ) );

        this.setLayout( new GridLayout( numPlayers, 1 ) );

        int i = 0;

        for ( Player player : players )
        {
            this.playerPanels[ i ] = new PlayerPanel( player, turnPlayer.equals( player ) );

            this.add( this.playerPanels[ i ] );

            i++;
        }
    }

    /**
     * Updates the player data that are visualized by this view class.
     *
     * @param players    The player data to be visualized.
     * @param turnPlayer The current turn player.
     */
    void updatePlayers( PlayerPool players, Player turnPlayer )
    {
        this.players = players;

        int i = 0;

        for ( Player player : players )
        {
            this.playerPanels[ i ].updatePlayer( player, turnPlayer.equals( player ) );

            i++;
        }
    }

    /**
     * The {@link PlayerPanel} is a GUI element used to visualize the data of one player.
     */
    private class PlayerPanel extends JPanel
    {
        /**
         * The player data that are currently being visualized.
         */
        private Player player;

        /**
         * The GUI label in which to write whether a player is the turn player.
         */
        private JLabel turnLabel = new JLabel();

        /**
         * The GUI label in which to write the amount of override stones a player owns
         */
        private JLabel overrideLabel = new JLabel();

        /**
         * The GUI label in which to write the amount of bombs a player owns
         */
        private JLabel bombLabel = new JLabel();

        /**
         * The GUI label in which to write whether a player is disqualified
         */
        private JLabel disqualificationLabel = new JLabel();

        /**
         * Constructs a new {@link PlayerPanel} for the given {@link Player} object.
         *
         * @param player The player whose data should be visualized.
         * @param turn   Whether or not it's that player's turn.
         */
        PlayerPanel( Player player, boolean turn )
        {
            this.updatePlayer( player, turn );

            JPanel panelLeft = new JPanel( new GridLayout( 4, 1 ) );
            JPanel panelRight = new JPanel( new GridLayout( 4, 1 ) );

            this.setBorder( new EmptyBorder( 0, 0, 25, 0 ) );

            panelLeft.add( new JLabel( "Player" + player.getID() ) );
            panelRight.add( turnLabel );

            panelLeft.add( new JLabel( "Override Stones  " ) );
            panelRight.add( overrideLabel );

            panelLeft.add( new JLabel( "Bombs" ) );
            panelRight.add( bombLabel );

            panelLeft.add( new JLabel( "Disqualified" ) );
            panelRight.add( disqualificationLabel );

            this.add( panelLeft );
            this.add( panelRight );
        }

        /**
         * Updates the player data that are currently being visualized
         *
         * @param player The player whose data should be visualized.
         * @param turn   Whether or not it's that player's turn.
         */
        void updatePlayer( Player player, boolean turn )
        {
            this.player = player;

            this.turnLabel.setText( ( turn ) ? "<--" : "" );
            this.overrideLabel.setText( String.valueOf( this.player.getNOverrideStones() ) );
            this.bombLabel.setText( String.valueOf( player.getNBombs() ) );
            this.disqualificationLabel.setText( String.valueOf( player.disqualified() ) );
        }
    }
}
