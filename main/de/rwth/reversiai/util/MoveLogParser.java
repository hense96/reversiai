package de.rwth.reversiai.util;

import de.rwth.reversiai.game.State;
import de.rwth.reversiai.move.Move;
import de.rwth.reversiai.move.StandardStoneMove;

import java.io.*;
import java.util.HashMap;
import java.util.Stack;

/**
 * This class is a parser for logfiles from matchpoint (i.e. simply copies of the log tables screened in the
 * browser). It creates a move stack from the read in file which may be successively accessed through
 * {@code getNextMove} function.
 *
 * @author Julius Hense
 */
public class MoveLogParser
{
    /**
     * HashMap storing which group (group number stored as <code>String</code>) has which player number.
     * Set in <code>parsePlayers</code> method.
     */
    private HashMap< String, Byte > groupToPlayer;

    /**
     * Stack storing x coordinates of the moves.
     * Set in <code>parseMoves</code> method.
     */
    private Stack< Integer > xko;

    /**
     * Stack storing y coordinates of the moves.
     * Set in <code>parseMoves</code> method.
     */
    private Stack< Integer > yko;

    /**
     * Stack storing the bonus preferences of the moves.
     * Set in <code>parseMoves</code> method.
     */
    private Stack< StandardStoneMove.BonusPref > bonusPrefs;

    /**
     * Stack storing the choice preferences of the moves.
     * Set in <code>parseMoves</code> method.
     */
    private Stack< Byte > choicePrefs;

    /**
     * @param path            path of the log file
     * @param numberOfPlayers number of players taking part in the match
     * @return the MoveLogParser
     * @throws IOException if there are problems with the expected data format
     */
    public MoveLogParser parseFile( String path, int numberOfPlayers ) throws IOException
    {
        this.parsePlayers( new BufferedReader( new InputStreamReader( new FileInputStream( path ) ) ),
                           numberOfPlayers );
        this.parseMoves( new BufferedReader( new InputStreamReader( new FileInputStream( path ) ) ) );

        return this;
    }

    /**
     * @param logString       log string
     * @param numberOfPlayers number of players taking part in the match
     * @return the MoveLogParser
     * @throws IOException if there are problems with the expected data format
     */
    public MoveLogParser parseString( String logString, int numberOfPlayers ) throws IOException
    {
        this.parsePlayers( new BufferedReader( new StringReader( logString ) ), numberOfPlayers );
        this.parseMoves( new BufferedReader( new StringReader( logString ) ) );

        return this;
    }

    /**
     * Finds out each team's player number.
     *
     * @param reader          a reader for the logging data
     * @param numberOfPlayers number of players taking part in the match
     * @throws IOException if there are problems with the expected data format
     */
    public void parsePlayers( BufferedReader reader, int numberOfPlayers ) throws IOException
    {
        String buffer;
        String[] bufferParts;

        this.groupToPlayer = new HashMap<>();

        byte playercounter = (byte) numberOfPlayers;

        buffer = reader.readLine();

        while ( buffer != null )
        {
            bufferParts = buffer.split( "\t" );

            if ( bufferParts.length >= 3 && bufferParts[ 2 ].equals( "Starting client ..." ) )
            {
                this.groupToPlayer.put( bufferParts[ 1 ], playercounter );
                --playercounter;
            }

            buffer = reader.readLine();
        }

        reader.close();
    }

    /**
     * Creates the move data stacks.
     *
     * @param reader a reader for the logging data
     * @throws IOException if there are problems with the expected data format
     */
    public void parseMoves( BufferedReader reader ) throws IOException
    {
        assert this.groupToPlayer != null;

        String buffer;
        String[] bufferParts;
        String[] coordinates;

        this.xko = new Stack<>();
        this.yko = new Stack<>();
        this.bonusPrefs = new Stack<>();
        this.choicePrefs = new Stack<>();

        buffer = reader.readLine();

        while ( buffer != null )
        {
            buffer = buffer.split( "\t" )[ 2 ];
            bufferParts = buffer.split( " " );

            if ( bufferParts.length > 1 && bufferParts[ 0 ].equals( "Disqualified" ) )
            {
                if ( bufferParts[ 1 ].equals( "(timeout" ) )
                {
                    this.xko.push( -1 );
                    this.yko.push( -1 );
                    bonusPrefs.push( StandardStoneMove.BonusPref.NONE );
                    choicePrefs.push( (byte) 0 );
                }
            }
            if ( bufferParts.length >= 3 && bufferParts[ 2 ].charAt( 0 ) == '(' )
            {
                coordinates = bufferParts[ 2 ].split( "," );
                xko.push( Integer.parseInt( coordinates[ 0 ].substring( 1 ) ) );
                yko.push( Integer.parseInt( coordinates[ 1 ].substring( 0, coordinates[ 1 ].length() - 1 ) ) );

                if ( bufferParts.length >= 8 && bufferParts[ 6 ].charAt( 0 ) == '[' )
                {
                    if ( bufferParts[ 7 ].equals( "overwrite]" ) )
                    {
                        bonusPrefs.push( StandardStoneMove.BonusPref.OVERRIDE );
                        choicePrefs.push( (byte) 0 );
                    }
                    else if ( bufferParts[ 7 ].equals( "bomb]" ) )
                    {
                        bonusPrefs.push( StandardStoneMove.BonusPref.BOMB );
                        choicePrefs.push( (byte) 0 );
                    }
                    else if ( bufferParts[ 6 ].equals( "[choosing" ) )
                    {
                        bonusPrefs.push( StandardStoneMove.BonusPref.NONE );
                        String group = "";
                        int i = 7;
                        while ( bufferParts[ i ].charAt( bufferParts[ i ].length() - 1 ) != ']' )
                        {
                            group += bufferParts[ i ] + " ";
                            ++i;
                        }
                        group += bufferParts[ i ].substring( 0, bufferParts[ i ].length() - 1 );

                        if ( !this.groupToPlayer.containsKey( group ) )
                        {
                            throw new IOException( "Call 017666844739 and tell him that his parser is shitty." );
                        }

                        choicePrefs.push( this.groupToPlayer.get( group ) );
                    }
                }
                else
                {
                    bonusPrefs.push( StandardStoneMove.BonusPref.NONE );
                    choicePrefs.push( (byte) 0 );
                }
            }

            buffer = reader.readLine();
        }

        reader.close();
    }

    /**
     * Creates the next move from the logging data for the given state.
     *
     * @param state the state of the game
     * @return next move, null if current player gets disqualified
     */
    public Move getNextMove( State state )
    {
        assert hasNextMove();

        int curxko = xko.pop();
        int curyko = yko.pop();
        StandardStoneMove.BonusPref curBP = bonusPrefs.pop();
        Byte curCP = choicePrefs.pop();

        if ( curxko == -1 )
        {
            return null;
        }
        else
        {
            Move move =
                    state.buildMove( curxko, curyko, curBP == StandardStoneMove.BonusPref.NONE ? curCP : curBP );

            if ( !move.isValid() )
            {
                return null;
            }
            else
            {
                return move;
            }
        }
    }

    /**
     * @return {@code true} if there there are still move data on the stack.
     */
    public boolean hasNextMove()
    {
        return !xko.isEmpty();
    }

}