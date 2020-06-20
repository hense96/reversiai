package de.rwth.reversiai.game;

import static org.junit.Assert.*;

import de.rwth.reversiai.move.*;

import de.rwth.reversiai.util.StateBuilder;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class ValidPositionTest
{
    @Test
    public void testValidMoves() throws Exception
    {
        long start = System.nanoTime();
        long end = start + TimeUnit.SECONDS.toNanos(1000);
        State state = new StateBuilder().parseFile( "D:\\OneDrive - rwth-aachen.de\\Aktuelles Semester\\group7\\maps\\025_50_50_8_25_rnd_1.map" ).buildState();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while ( !state.getAllValidMoves().isEmpty() && System.nanoTime() < end)
        {

            for ( Move move : state.getAllValidMoves() )
            {
                if (!move.isValid()) {
                    move.isValid();
                    String name = br.readLine();
                    assertTrue( move.toString()+" is inavlid! Board: \n"+state.getBoard().toString(), move.isValid() );
                }
            }
            if (!state.getAllValidMoves().iterator().hasNext()) {
                return;
            }
            state = state.getAllValidMoves().iterator().next().execute();
        }
    }

}
