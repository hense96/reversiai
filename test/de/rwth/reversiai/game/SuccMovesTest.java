package de.rwth.reversiai.game;

import static org.junit.Assert.*;

import de.rwth.reversiai.board.*;
import de.rwth.reversiai.board.iterator.BoardIterator;
import de.rwth.reversiai.move.*;
import de.rwth.reversiai.util.StateBuilder;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class SuccMovesTest
{
    @Test
    public void testValidMoves() throws Exception
    {
        State state = new StateBuilder().parseFile( "D:\\OneDrive - rwth-aachen.de\\Aktuelles Semester\\group7\\maps\\025_50_50_8_25_rnd_1.map" ).buildState();

        Set< Move > oldValidMoves = calcValidMovesOld( state );
        Set< Move > newValidMoves = state.getAllValidMoves();

        while ( !oldValidMoves.isEmpty() )
        {
            for ( Move move : oldValidMoves )
            {
                assertTrue( state.toString() + "\n" + move.toString(), newValidMoves.contains( move ) );
            }

            for ( Move move : newValidMoves )
            {
                assertTrue( state.toString() + "\n" + move.toString(), oldValidMoves.contains( move ) );
            }

            state = oldValidMoves.iterator().next().execute();

            oldValidMoves = calcValidMovesOld( state );
            newValidMoves = state.getAllValidMoves();
        }
    }

    private Set< Move > calcValidMovesOld( State state )
    {
        Set< Move > moves = new HashSet<>();

        BoardIterator iterator = state.getBoard().getBoardIterator();

        for ( int y = 0; y < state.getBoard().getHeight(); y++ )
        {
            for ( int x = 0; x < state.getBoard().getWidth(); x++ )
            {
                iterator.moveTo( x, y );

                if ( iterator.getTileType() != TileType.ABSENT )
                {
                    Move.Type mt = state.getMoveType( x, y );

                    if ( mt == Move.Type.ChoiceTileMove )
                    {
                        for ( Player p : state.getPlayers() )
                        {
                            Move move = state.buildMove( x, y, p.getID() );

                            if ( move.isValid() )
                            {
                                moves.add( move );
                            }
                        }
                    }
                    else if ( mt == Move.Type.BonusTileMove )
                    {
                        Move move = state.buildMove( x, y, StandardStoneMove.BonusPref.OVERRIDE );

                        if ( move.isValid() )
                            moves.add( move );

                        move = state.buildMove( x, y, StandardStoneMove.BonusPref.BOMB );

                        if ( move.isValid() )
                            moves.add( move );
                    }
                    else
                    {
                        Move move = state.buildMove( x, y, null );

                        if ( move.isValid() )
                            moves.add( move );
                    }
                }
            }
        }

        return moves;
    }
}
