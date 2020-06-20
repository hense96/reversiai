package de.rwth.reversiai.clients;

import de.rwth.reversiai.AI;
import de.rwth.reversiai.configuration.Configurator;
import de.rwth.reversiai.game.State;
import de.rwth.reversiai.move.Move;
import de.rwth.reversiai.util.LogTopic;
import de.rwth.reversiai.util.MoveLogParser;
import de.rwth.reversiai.util.StateBuilder;
import de.rwth.reversiai.visualization.InteractiveStateVisualization;
import de.rwth.reversiai.visualization.StateVisualization;

import java.io.IOException;

/**
 * The {@code StandaloneClient} manages a game that is not hosted by a server. It creates and maintains a visualization
 * of the game.
 * Gameplay options:
 * <ul>
 * <li>choose the board,</li>
 * <li>choose number of human players and AI configuration,</li>
 * <li>simulate a matchpoint game by passing its logfile.</li>
 * </ul>
 *
 * @see de.rwth.reversiai.visualization
 */
public class StandaloneClient extends Client
{
    /**
     * The current game state.
     */
    private State state = null;

    /**
     * The visualization of the game.
     */
    private StateVisualization visualization = null;

    /**
     * Array of agents playing against each other. Agent of index i belongs to player i+1. Might contain {@code null}
     * entries representing human players.
     */
    private AI[] agents;

    /**
     * Parser for a move log file from matchpoint. This parser maintains a stack of moves to be executed.
     */
    private MoveLogParser moves;

    /**
     * Variable that will contain a move entered via the GUI as an interactive player.
     */
    private volatile Move interactiveMove;

    /**
     * {@code true} if the move log parser still contains moves to be simulated.
     */
    private boolean replayPhase;

    /**
     * Flag indicating whether user currently wants the game not to progress.
     */
    private volatile boolean pause;

    /**
     * Flag whether the client is currently paused. We need this because we use Object.wait both when waiting for a user
     * input of a move and while pausing the automatic game progress
     */
    private volatile boolean paused;

    /**
     * Indicates how many ms the client waits between executing two moves.
     */
    private volatile long delay;

    /**
     * @param boardFile    A map file containing a board.
     * @param humanPlayers Number of human players.
     * @param replayFile   A logfile from matchpoint.
     * @param configurator A configurator containing an AI configuration.
     */
    public StandaloneClient( String boardFile, int humanPlayers, String replayFile, Configurator configurator )
    {
        LogTopic.info.log( "Starting Group 7 Reversi Standalone Client..." );

        try
        {
            LogTopic.info.log( "Parsing board from file..." );

            // Parse board from file
            this.state = new StateBuilder().parseFile( boardFile ).buildState();
        }
        catch ( Exception e )
        {
            LogTopic.error.error( e.getMessage() );
            System.exit( -1 );
        }

        if ( humanPlayers > this.state.getPlayers().getNumberOfPlayers() )
        {
            LogTopic.error.error(
                    "The given board may only be played by %d players!",
                    this.state.getPlayers().getNumberOfPlayers()
            );

            System.exit( -1 );
        }

        try
        {
            if ( replayFile != null )
            {
                LogTopic.info.log( "Parsing replay file..." );

                // Parse log file for replay feature
                this.moves = new MoveLogParser();
                this.moves.parseFile( replayFile, this.state.getPlayers().getNumberOfPlayers() );
                this.replayPhase = true;
            }
            else
            {
                this.replayPhase = false;
            }
        }
        catch ( IOException e )
        {
            LogTopic.error.error( e.getMessage() );
            System.exit( -1 );
        }


        // Initialize waiting properties
        this.setPause( true );
        this.setDelay( 4000 );

        // Initialize agents
        this.agents = new AI[ this.state.getPlayers().getNumberOfPlayers() ];

        // Human players will occupy the lower player IDs
        for ( byte id = 1; id <= humanPlayers; id++ )
        {
            this.agents[ id - 1 ] = null;
        }

        // Fill up the other players with AIs
        for ( byte id = (byte) ( humanPlayers + 1 ); id <= this.agents.length; id++ )
        {
            this.agents[ id - 1 ] = new AI( state, id, configurator.buildAIConfiguration() );
        }

        // Start the visualization
        if ( humanPlayers > 0 )
        {
            this.visualization = new InteractiveStateVisualization( this.state, this );
        }
        else
        {
            this.visualization = new StateVisualization( this.state, this );
        }
    }

    /**
     * Executes moves and updates current game state  as well as its visualization until the game is finished.
     */
    @Override
    public void run()
    {
        while ( !this.state.isOver() )
        {
            if ( this.replayPhase )
            {
                if ( this.moves.hasNextMove() )
                {
                    // Load next move from log file
                    Move move = this.moves.getNextMove( this.state );

                    // Pause and "only next move" feature
                    if ( this.pause )
                    {
                        this.pause();
                    }

                    // Wait feature
                    try
                    {
                        Thread.sleep( this.delay );
                    }
                    catch ( InterruptedException e )
                    {
                        // Will probably not happen, nothing to be done here
                    }

                    // Execute the move
                    if ( move != null )
                    {
                        this.updateState( move.execute() );
                    }
                    else
                    {
                        // If null is returned, player needs to be disqualified
                        this.state.disqualify( this.state.getTurnPlayer().getID() );
                        this.updateState( this.state );
                    }
                }
                else
                {
                    this.replayPhase = false;
                }
            }
            else
            {
                // Get the game agent whose turn it is to make a move
                AI agent = this.agents[ this.state.getTurnPlayer().getID() - 1 ];

                Move move = null;

                if ( agent == null )
                {
                    // Enable the move entry in the GUI
                    ( (InteractiveStateVisualization) this.visualization ).acceptInput();

                    synchronized ( this )
                    {
                        try
                        {
                            this.wait();
                        }
                        catch ( InterruptedException e )
                        {
                            // Will probably not happen, nothing to be done here
                        }
                    }

                    move = this.interactiveMove;

                    this.interactiveMove = null;
                }
                else
                {
                    // Pause and "only next move" feature
                    if ( this.pause )
                    {
                        this.pause();
                    }

                    long tic = System.currentTimeMillis();

                    move = agent.computeBestMove( this.delay * 1000000L, 0, 1 );

                    long toc = System.currentTimeMillis();

                    long computationTime = toc - tic;

                    if ( this.delay - computationTime > 0 )
                    {
                        // Wait feature
                        try
                        {
                            Thread.sleep( this.delay - computationTime );
                        }
                        catch ( InterruptedException e )
                        {
                            // Will probably not happen, nothing to be done here
                        }
                    }


                }

                this.updateState( move.execute() );
            }
        }

        this.visualization.displayPopUpMessage( "The game is over!" );
    }

    /**
     * Used to pause the automatic progress of a game until either the play or next button in the GUI is pressed.
     */
    private void pause()
    {
        this.paused = true;

        synchronized ( this )
        {
            try
            {
                this.wait();
            }
            catch ( InterruptedException e )
            {
                // Will probably not happen, nothing to be done here
            }
        }

        this.paused = false;
    }

    /**
     * Updates current game state and communicates the changes to the visualization and the AIs.
     *
     * @param state The new game state.
     */
    private void updateState( State state )
    {
        this.state = state;

        this.visualization.updateState( state );

        for ( AI ai : this.agents )
        {
            if ( ai != null )
            {
                ai.setState( state );
            }
        }
    }

    /**
     * Method called by the GUI controllers once a new move delay is set.
     *
     * @param delay Delay time in ms.
     */
    public void setDelay( int delay )
    {
        assert delay >= 0;

        this.delay = delay;
    }

    /**
     * Method called by the GUI controllers once the pause button is pressed.
     *
     * @param pause {@code true} if the game should not progress at the moment.
     */
    public void setPause( boolean pause )
    {
        this.pause = pause;
    }

    /**
     * Method called by the GUI controllers once the play or next button is pressed. Resumes the progress of the game
     * (for one step if the game is still paused)
     */
    public void nextMove()
    {
        if ( this.paused )
        {
            synchronized ( this )
            {
                this.notify();
            }
        }
    }

    /**
     * Method called by the GUI controllers once a move is entered.
     *
     * @param x         x coordinate on which the stone should be placed
     * @param y         y coordinate on which the stone should be placed
     * @param moveType  Type of the move to be made
     * @param prefValue Choice/Bonus preference or null if the move is not a choice/bonus tile move
     */
    public void notifyWithInteractiveMoveData( int x, int y, Move.Type moveType, Object prefValue )
    {
        if ( ( moveType == Move.Type.BonusTileMove || moveType == Move.Type.ChoiceTileMove ) && prefValue == null )
        {
            this.visualization.displayPopUpMessage( "Must specify a valid preference!" );

            // Try again
            ( (InteractiveStateVisualization) this.visualization ).acceptInput();

            return;
        }

        if ( prefValue instanceof Byte
             && ( (byte) prefValue < 1 || (byte) prefValue > this.state.getBoard().getPlayers() ) )
        {
            this.visualization.displayPopUpMessage( "Must specify a valid player ID to switch stones with!" );

            // Try again
            ( (InteractiveStateVisualization) this.visualization ).acceptInput();

            return;
        }

        Move move = this.state.buildMove( x, y, prefValue );

        if ( !move.isValid() )
        {
            this.visualization.displayPopUpMessage( "This is not a valid move!" );

            // Try again
            ( (InteractiveStateVisualization) this.visualization ).acceptInput();

            return;
        }

        this.interactiveMove = move;

        // Wake up the client
        synchronized ( this )
        {
            StandaloneClient.this.notify();
        }
    }
}