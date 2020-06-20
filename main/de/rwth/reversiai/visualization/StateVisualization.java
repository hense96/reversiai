package de.rwth.reversiai.visualization;

import de.rwth.reversiai.clients.StandaloneClient;
import de.rwth.reversiai.game.State;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.*;

/**
 * This class is a GUI controller used to instantiate a visualization of a game state. Instead of a model class we use a
 * state object to store data. The view class is represented by {@link StateVisualizationView} which is itself divided
 * up into three individual sub-views.
 *
 * @author Marvin Pf&ouml;rtner
 */
public class StateVisualization
{
    /**
     * Instance of the view class.
     */
    protected final StateVisualizationView stateView;

    /**
     * The {@link State} object that is currently visualized.
     */
    protected State state;

    /**
     * If the {@link StateVisualization} is used in a {@link StandaloneClient}, we need a reference to that object in
     * order to call the corresponding callbacks for the replay controls
     */
    protected StandaloneClient client;

    /**
     * The position of the tile, the mouse hovers over at the moment or the unassigned position if the mouse does not
     * hover over a tile.
     */
    private volatile Position mousePosition = Position.unassigned;

    /**
     * Constructs a state visualization that simply visualizes the given state.
     *
     * @param state The game state that should be visualized.
     */
    public StateVisualization( State state )
    {
        this.state = state;
        this.stateView = new StateVisualizationView( state, false );

        this.stateView.boardView.addMouseMotionListener( new BoardVisualizationMouseMotionAdapter() );
        this.stateView.addKeyListener( new StateVisualizationKeyboardAdapter() );
    }

    /**
     * Constructs a state visualization with replay controls that visualizes the given state and connects to the given
     * {@link StandaloneClient}.
     *
     * @param state  The game state that should be visualized.
     * @param client The {@link StandaloneClient} to use for GUI callbacks.
     */
    public StateVisualization( State state, StandaloneClient client )
    {
        this.state = state;
        this.client = client;
        this.stateView = new StateVisualizationView( state, true );

        this.stateView.boardView.addMouseMotionListener( new BoardVisualizationMouseMotionAdapter() );
        this.stateView.addKeyListener( new StateVisualizationKeyboardAdapter() );

        ReplayControlsListener listener = new ReplayControlsListener();

        this.stateView.replayControlsView.playButton.addActionListener( listener );
        this.stateView.replayControlsView.pauseButton.addActionListener( listener );
        this.stateView.replayControlsView.nextButton.addActionListener( listener );

        this.stateView.replayControlsView.delaySlider.addChangeListener( listener );
    }

    /**
     * Updates the state that is currently visualized in the GUI.
     *
     * @param state The state to be visualized.
     */
    public void updateState( State state )
    {
        this.state = state;

        this.stateView.updateState( state );
    }

    /**
     * Method that pops up a small message window showing the given {@link String}.
     *
     * @param message The message to display
     */
    public void displayPopUpMessage( String message )
    {
        JOptionPane.showMessageDialog( this.stateView, message );
    }

    /**
     * This {@link MouseMotionAdapter} is responsible for updating the current mouse position in order to visualize
     * board transitions once the shift key is pressed.
     */
    private class BoardVisualizationMouseMotionAdapter extends MouseMotionAdapter
    {
        @Override
        public void mouseMoved( MouseEvent e )
        {
            Position newPos = StateVisualization.this.stateView.boardView.getMousePosition( e.getPoint() );

            if ( !StateVisualization.this.mousePosition.equals( newPos ) )
            {
                StateVisualization.this.mousePosition = newPos;

                // If the shift key is pressed, the transitions at the current position of the mouse cursor will be
                // visualized.
                if ( e.isShiftDown() )
                {
                    StateVisualization.this.stateView.boardView.startHighlightingTransitions(
                            StateVisualization.this.mousePosition
                    );
                }
                else
                {
                    StateVisualization.this.stateView.boardView.stopHighlightingTransitions();
                }
            }
        }
    }

    /**
     * This {@link KeyAdapter} detects whether the shift key is pressed or released and toggles the visualization of
     * transitions accordingly.
     */
    private class StateVisualizationKeyboardAdapter extends KeyAdapter
    {
        /**
         * Flag indicating whether the shift key was pressed before a key release and after a key press
         */
        private volatile boolean shiftWasDown;

        @Override
        public void keyPressed( KeyEvent e )
        {
            if ( e.isShiftDown() )
            {
                this.shiftWasDown = true;

                StateVisualization.this.stateView.boardView.startHighlightingTransitions( mousePosition );
            }
        }

        @Override
        public void keyReleased( KeyEvent e )
        {
            if ( this.shiftWasDown && !e.isShiftDown() )
            {
                this.shiftWasDown = false;

                StateVisualization.this.stateView.boardView.stopHighlightingTransitions();
            }
        }
    }

    /**
     * This custom listener class is used to propagate UI events on the replay controls to the {@link StandaloneClient}
     * class
     */
    private class ReplayControlsListener implements ActionListener, ChangeListener
    {
        /**
         * Will be called when the play, pause or next button is pressed
         *
         * @param e
         */
        @Override
        public void actionPerformed( ActionEvent e )
        {
            switch ( ( (JButton) e.getSource() ).getText() )
            {
                case "Play":
                    StateVisualization.this.client.setPause( false );
                    StateVisualization.this.client.nextMove();
                    break;

                case "Pause":
                    StateVisualization.this.client.setPause( true );
                    break;

                case "Next":
                    StateVisualization.this.client.nextMove();
                    break;
            }
        }

        /**
         * Will be called when the value of the delay slider changes
         *
         * @param e
         */
        @Override
        public void stateChanged( ChangeEvent e )
        {
            StateVisualization.this.client.setDelay( ( (JSlider) e.getSource() ).getValue() * 200 );
        }
    }
}
