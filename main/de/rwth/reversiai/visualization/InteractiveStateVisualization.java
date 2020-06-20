package de.rwth.reversiai.visualization;

import de.rwth.reversiai.clients.StandaloneClient;
import de.rwth.reversiai.game.State;
import de.rwth.reversiai.move.Move;
import de.rwth.reversiai.move.StandardStoneMove;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * The {@link InteractiveStateVisualization} is essentially a state visualization that adds the possibility to enter a
 * move via the GUI in order to play against the AIs as a human player.
 *
 * @author Marvin Pf&ouml;rtner
 */
public class InteractiveStateVisualization extends StateVisualization
{
    /**
     * Flag indicating whether the AI accepts human input
     */
    private volatile boolean acceptInput = false;

    /**
     * Constructs a new {@link InteractiveStateVisualization} of the given state. We always need a {@link StandaloneClient}
     * to connect to.
     *
     * @param initialState The state to visualize initially
     * @param client       The {@link StandaloneClient} to connect to
     */
    public InteractiveStateVisualization( State initialState, StandaloneClient client )
    {
        super( initialState, client );

        this.stateView.boardView.addMouseListener( new InteractiveBoardVisualizationMouseAdapter() );
    }

    /**
     * Method that will be called by the {@link StandaloneClient} once it is ready to receive a GUI move.
     */
    public void acceptInput()
    {
        this.acceptInput = true;
    }

    /**
     * Pops up a small GUI prompt asking the user to choose a bonus preference.
     *
     * @return The chosen bonus preference.
     */
    private StandardStoneMove.BonusPref getBonusPreference()
    {
        Object[] options = {
                "Bomb Stone",
                "Override Stone"
        };

        int choice = JOptionPane.showOptionDialog(
                this.stateView,
                "You placed on a bonus tile, pick a bonus stone!",
                "Bonus Tile Preference",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[ 1 ]
        );

        return ( choice == 0 ) ? StandardStoneMove.BonusPref.BOMB : StandardStoneMove.BonusPref.OVERRIDE;
    }

    /**
     * Pops up a small GUI prompt asking the user to enter a choice preference.
     *
     * @return The entered choice preference.
     * @throws NumberFormatException If the number entered is not a positive integer.
     */
    private byte getChoicePreference() throws NumberFormatException
    {
        String res = JOptionPane.showInputDialog(
                this.stateView,
                "You placed on a choice tile, enter the ID of the\nplayer with whom you want to switch stones",
                "Choice Tile Preference",
                JOptionPane.PLAIN_MESSAGE
        );

        return Byte.parseByte( res );
    }

    /**
     * This {@link MouseAdapter} is used to detect clicks to the game board as a possibility to enter moves.
     */
    private class InteractiveBoardVisualizationMouseAdapter extends MouseAdapter
    {
        @Override
        public void mouseClicked( MouseEvent e )
        {
            Position clickPosition =
                    InteractiveStateVisualization.this.stateView.boardView.getMousePosition( e.getPoint() );

            if ( !clickPosition.isUnassigned() && InteractiveStateVisualization.this.acceptInput )
            {
                try
                {
                    Move.Type moveType = InteractiveStateVisualization.this.state.getMoveType(
                            clickPosition.x,
                            clickPosition.y
                    );

                    Object pref = null;

                    if ( moveType == Move.Type.BonusTileMove )
                    {
                        pref = InteractiveStateVisualization.this.getBonusPreference();
                    }
                    else if ( moveType == Move.Type.ChoiceTileMove )
                    {
                        try
                        {
                            pref = InteractiveStateVisualization.this.getChoicePreference();
                        }
                        catch ( NumberFormatException exc )
                        {
                            InteractiveStateVisualization.this.displayPopUpMessage( "A valid player ID is required!" );
                        }
                    }

                    InteractiveStateVisualization.this.client.notifyWithInteractiveMoveData(
                            clickPosition.x, clickPosition.y, moveType,
                            pref
                    );

                    InteractiveStateVisualization.this.acceptInput = false;
                }
                catch ( UnsupportedOperationException exception )
                {
                    InteractiveStateVisualization.this.displayPopUpMessage( "You must place a stone on a tile!" );
                }
            }
        }
    }
}
