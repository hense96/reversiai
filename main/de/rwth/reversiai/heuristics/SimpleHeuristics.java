package de.rwth.reversiai.heuristics;

import de.rwth.reversiai.board.Board;
import de.rwth.reversiai.game.Player;
import de.rwth.reversiai.game.PlayerEval;
import de.rwth.reversiai.game.PlayerPool;
import de.rwth.reversiai.game.State;

import java.util.*;

/**
 * This class is a heuristic class that contains heuristic functions that do no need any universal data.
 * Every heuristic function rates a situation (given by a node or a state) from the perspective of a player.
 */
public class SimpleHeuristics
{
    /**
     * author Julius Hense
     * <p>
     * Derives a real measure between 0 (bad) and 1 (good) from the number of valid moves a player and
     * the other players may perform in a state in the first phase plus the number of bombs a player owns.
     * The number of the player is compared against the best value in this ranking as well as the average.
     * Both comparisons are weighted with 0.5. If no move is possible in general, 0 is returned.
     * <p>
     * Complexity: the complexity is the complexity of FanBase.getNumberOfValidStoneMoves() and therefore
     * n^2 in the mapsize, although it can be expected to run faster in non-degenerated states.
     *
     * @param state  the state to be evaluated
     * @param player the player from which perspective the state should be evaluated
     * @param p1     Choose from [0,1]. The higher it is, the more important is the comparison to the best player. The
     *               lower it is, the more important is the comparison to the average of the players.
     * @return a real number between 0 (bad) and 1 (good)
     */
    public static double relativeNumberOfMoves( State state, Player player, double p1 )
    {
        PlayerPool players = state.getPlayers();
        PlayerEval< Integer > nValidMoves = FanBase.getNumberOfValidStoneMoves( state );

        double nMovesPlayer;
        double nMovesMax = 0;
        double nMovesSum = 0;

        // Calculate necessary data from numbers of valid moves for each player
        for ( Player p : players )
        {
            nValidMoves.setEval( p, nValidMoves.getEval( p ) + p.getNBombs() );
            if ( nValidMoves.getEval( p ).doubleValue() > nMovesMax )
            {
                nMovesMax = nValidMoves.getEval( p ).doubleValue();
            }
            nMovesSum += nValidMoves.getEval( p ).doubleValue();
        }
        nMovesPlayer = nValidMoves.getEval( player ).doubleValue();

        if ( nMovesMax == 0 )
        {
            return 0;
        }

        // Calculate measure
        return p1 * ( nMovesPlayer / nMovesMax ) + ( 1 - p1 ) * ( nMovesPlayer / nMovesSum );
    }

    /**
     * author Julius Hense
     * <p>
     * Derives a real measure between 0 (bad) and 1 (good) from the number of stones that
     * may remain after the bombing phase. To give an estimate how many stones will be destroyed by bombing,
     * several bomb moves are simulated according to a pattern. The possibly destroyed stones are summed up
     * considering a worst-case szenario and the number of bombs the other players still have.
     * The number of possibly destroyed stones is moreover weighted by the current rank of a player (the lower
     * the less stones she will lose). Finally, the estimate for the remaining stones of a player is compared against
     * the best player's value and the average. Both comparisons are weighted by 0.5.
     * <p>
     * If no player has a bomb left or the function predicts that all stones will be destroyed, 0 is returned.
     * <p>
     * Complexity: (stones destroyable by one bomb)*(mapsize) + (mapsize)*(number of players)*(insertion time of priority queue)
     * I claim it is not worth than quadratic in mapsize and will show acceptable behaviour in practice :)
     *
     * @param state  the state to be evaluated
     * @param player the player from which perspective the state should be evaluated
     * @param p1     Choose from [0,1]. The higher it is, the more important is the comparison to the best player. The
     *               lower it is, the more important is the comparison to the average of the players.
     * @return a value between 0 (bad) and 1 (good)
     */
    public static double relativeNumberOfBombableTiles( State state, Player player, double p1 )
    {
        PlayerPool players = state.getPlayers();

        /* calculate the number of bombs left in the game and a player owning a bomb */
        int nBombs = FanBase.getNumberOfBombs( state );

        if ( nBombs == 0 )
            return 0.0;

        /* calculate how many stones may be destroyed by a heuristic bombing pattern */
        Set< PlayerEval< Integer > > bombableStonesList = SimpleHeuristics.applyBombingPattern( state );

        /* for every player: sum up how many stones may be destroyed by other players bombs */
        PriorityQueue< Integer > bombableStonesOfPlayer =
                new PriorityQueue<>( bombableStonesList.size(), Collections.reverseOrder() );
        PlayerEval< Integer > bombableStonesEstimate = new PlayerEval<>( players );
        Iterator< PlayerEval< Integer > > it;

        for ( Player p : players )
        {
            bombableStonesOfPlayer.clear();

            it = bombableStonesList.iterator();
            while ( it.hasNext() )
            {
                bombableStonesOfPlayer.add( it.next().getEval( p ) );
            }

            bombableStonesEstimate.setEval( p, 0 );
            for ( int i = 0; i < nBombs - p.getNBombs() && !bombableStonesOfPlayer.isEmpty(); ++i )
            {
                bombableStonesEstimate
                        .setEval( p, bombableStonesEstimate.getEval( p ) + bombableStonesOfPlayer.poll() );
            }
        }

        /* now weight the number of bombed stones by rank */
        PlayerEval< Integer > ranks = FanBase.getRanks( state );
        PlayerEval< Integer > leftStonesEstimate = new PlayerEval<>( players );

        for ( Player p : players )
        {
            bombableStonesEstimate.setEval( p, (int) ( bombableStonesEstimate.getEval( p ).doubleValue() *
                                                       ( 1 - 0.1 * ranks.getEval( p ).doubleValue() ) ) );
        }

        /* now calculate how many stones would be left then */
        PlayerEval< Integer > curNStones = FanBase.getNumberOfStones( state );

        for ( Player p : players )
        {
            leftStonesEstimate.setEval( p, curNStones.getEval( p ) - bombableStonesEstimate.getEval( p ) );
            if ( leftStonesEstimate.getEval( p ) < 0 )
                leftStonesEstimate.setEval( p, 0 );
        }

        /* now calculate a measure from the derived data */
        double nStonesPlayer = 0;
        double nStonesMax = 0;
        double nStonesSum = 0;

        for ( Player p : players )
        {
            if ( leftStonesEstimate.getEval( p ).doubleValue() > nStonesMax )
            {
                nStonesMax = leftStonesEstimate.getEval( p ).doubleValue();
            }
            nStonesSum += leftStonesEstimate.getEval( p ).doubleValue();
        }
        nStonesPlayer = leftStonesEstimate.getEval( player );

        if ( nStonesMax == 0 )
            return 0.0;

        return p1 * ( nStonesPlayer / nStonesMax ) + ( 1 - p1 ) * ( nStonesPlayer / nStonesSum );
    }

    /**
     * author Julius Hense
     * <p>
     * Calculates a heuristic value for a given player based on the rank. The value is 1 for first
     * place and 0 for the last place.
     * <p>
     * Complexity: complexity of computing the rank, therefore linear in mapsize.
     *
     * @param state  the state to be evaluated
     * @param player the player from which perspective the state should be evaluated
     * @return a value between 0 (bad) and 1 (good)
     */
    public static double relativeRank( State state, Player player )
    {
        PlayerEval< Integer > ranks = FanBase.getRanks( state );

        return ( 1 - ( ( (double) ( ranks.getEval( player ) - 1 ) ) /
                       ( (double) ( state.getPlayers().getNumberOfPlayers() - 1 ) ) ) );
    }

    /**
     * author Julius Hense
     * <p>
     * Applies several bomb moves on the board and saves how many stones each player loses by each bomb move in
     * a list of integer arrays.
     * <p>
     * Bombing pattern: start at (x,y)=(bombRadius,bombRadius) and recursively move 2*bombRadius in x and in
     * y direction.
     * <p>
     * Complexity: (mapsize)*(getNumberOfBombesStones complexity), therefore n^2 where n is the mapsize.
     *
     * @param state state to be evaluated
     * @return a list of arrays storing how many stones each player loses when a bomb move is performed
     */
    private static Set< PlayerEval< Integer > > applyBombingPattern( State state )
    {
        Set< PlayerEval< Integer > > bombableStonesList = new HashSet<>();
        PlayerPool players = state.getPlayers();
        Player bombingPlayer = null;
        Board board = state.getBoard();

        /* calculate a player owning a bomb */
        for ( Player player : players )
        {
            if ( player.getNBombs() > 0 )
            {
                bombingPlayer = player;
                break;
            }
        }
        if ( bombingPlayer == null )
            return bombableStonesList;
        int bombradius = board.getBombRadius() == 0 ? 1 : board.getBombRadius();
        /* apply bombing pattern */
        for ( int x = board.getBombRadius(); x < state.getBoard().getWidth(); x += 2 * bombradius )
        {
            for ( int y = board.getBombRadius(); y < state.getBoard().getHeight(); y += 2 * bombradius - 1 )
            {

                bombableStonesList
                        .add( FanBase.getNumberOfBombedStones( state, x, y, state.getBoard().getBombRadius() ) );
            }
        }

        return bombableStonesList;
    }
}
