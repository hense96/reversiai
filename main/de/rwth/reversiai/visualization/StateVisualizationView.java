package de.rwth.reversiai.visualization;

import de.rwth.reversiai.game.State;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * View class for a {@link StateVisualization}.
 *
 * @author Marvin Pf&ouml;rtner
 */
class StateVisualizationView extends Frame
{
    /**
     * Sub-view visualizing the game board
     */
    final BoardVisualizationView boardView;

    /**
     * Sub-view visualizing the player data
     */
    final PlayerVisualizationView playerView;

    /**
     * Sub-view used to render the GUI elements controlling the replay feature
     */
    final ReplayControlsView replayControlsView;

    /**
     * Constructs a new {@link StateVisualization} with or without replay controls
     *
     * @param state          The state to visualize initially
     * @param replayControls Whether or not to render replay controls
     */
    StateVisualizationView( State state, boolean replayControls )
    {
        this.boardView = new BoardVisualizationView( state.getBoard() );
        this.playerView = new PlayerVisualizationView( state.getPlayers(), state.getTurnPlayer() );

        if ( replayControls )
        {
            this.replayControlsView = new ReplayControlsView();
        }
        else
        {
            this.replayControlsView = null;
        }

        this.setTitle( "Group 7 Reversi Client" );

        this.setSize( 700, 600 );
        this.setPreferredSize( new Dimension( 700, 600 ) );
        this.addWindowListener( new WindowAdapter()
        {
            @Override
            public void windowClosing( WindowEvent e )
            {
                super.windowClosing( e );

                e.getWindow().dispose();
                System.exit( 0 );
            }
        } );

        this.setLayout( new BorderLayout() );

        this.add( "Center", this.boardView );
        this.add( "East", this.playerView );

        if ( replayControls )
        {
            this.add( "South", this.replayControlsView );
        }

        this.pack();
        this.setVisible( true );
    }

    /**
     * Will be called by the controller once the state to be visualized is updated
     *
     * @param state The new state to be visualized
     */
    void updateState( State state )
    {
        this.boardView.updateBoard( state.getBoard() );
        this.playerView.updatePlayers( state.getPlayers(), state.getTurnPlayer() );
    }
}
