package de.rwth.reversiai.visualization;

import javax.swing.*;
import java.awt.*;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Sub-view used to render the play, pause and next button as well as the delay slider to the screen. These GUI elements
 * control the replay features.
 *
 * @author Marvin Pf&ouml;rtner
 */
class ReplayControlsView extends JPanel
{
    /**
     * The play button
     */
    final JButton playButton;

    /**
     * The pause button
     */
    final JButton pauseButton;

    /**
     * The next button
     */
    final JButton nextButton;

    /**
     * The delay slider
     */
    final JSlider delaySlider;

    /**
     * Constructs a new {@link ReplayControlsView}
     */
    public ReplayControlsView()
    {
        super( new BorderLayout( 40, 0 ) );

        this.playButton = new JButton( "Play" );
        this.pauseButton = new JButton( "Pause" );
        this.nextButton = new JButton( "Next" );

        JPanel buttonPanel = new JPanel( new FlowLayout() );

        buttonPanel.add( playButton );
        buttonPanel.add( pauseButton );
        buttonPanel.add( nextButton );

        JPanel speedPanel = new JPanel( new BorderLayout( 0, 0 ) );

        Dictionary< Integer, JLabel > labels = new Hashtable<>();

        for ( int i = 0; i <= 5; i++ )
        {
            labels.put( i * 5, new JLabel( String.valueOf( i ) ) );
        }

        this.delaySlider = new JSlider( 0, 20, 20 );
        this.delaySlider.setMajorTickSpacing( 5 );
        this.delaySlider.setMinorTickSpacing( 1 );
        this.delaySlider.setInverted( true );
        this.delaySlider.setPaintTicks( true );
        this.delaySlider.setPaintLabels( true );
        this.delaySlider.setLabelTable( labels );

        speedPanel.add( "West", new JLabel( "Move Delay (s)" ) );
        speedPanel.add( "Center", delaySlider );

        this.add( "West", buttonPanel );
        this.add( "Center", speedPanel );
    }
}
