package de.rwth.reversiai.search.generator;


import de.rwth.reversiai.AI;
import de.rwth.reversiai.exceptions.TimerInterruptException;
import de.rwth.reversiai.game.Player;
import de.rwth.reversiai.game.PlayerEval;
import de.rwth.reversiai.game.PlayerPool;
import de.rwth.reversiai.game.State;
import de.rwth.reversiai.heuristics.FanBase;
import de.rwth.reversiai.move.Move;
import de.rwth.reversiai.search.GameTree;
import de.rwth.reversiai.search.evaluator.RestrictedBombEvaluator;
import de.rwth.reversiai.search.nodeeval.ParanoidDouble;

import java.util.*;

/**
 * Only use for bombing phase. Restricted BombGenerator only creates successor states for bomb
 * moves which fulfill certain criteria.
 * If no move fulfills criteria, a move with maximum opponent destruction is chosen.
 */
public class RestrictedBombGenerator extends Generator
{
    /**
     * Criterion that a bomb move needs to fulfill in order to be added to sucNodes.
     */
    private final int bombSimulationRadius = 1;

    /**
     * Criterion that a bomb move needs to fulfill in order to be added to sucNodes.
     */
    private final int maxNumberOfSelfBombing = 2;

    /**
     * Criterion that a bomb move needs to fulfill in order to be added to sucNodes.
     */
    private final int minNumberOfOtherBombing = 1;

    /**
     * Criterion that a bomb move needs to fulfill in order to be added to sucNodes.
     */
    private final double minOtherSelfBombingRatio = 3.0;

    /**
     * Criterion that a bomb move needs to fulfill in order to be added to sucNodes.
     */
    private final int deltaMaxOtherBombing = 3;

    /**
     * Criterion that a bomb move needs to fulfill in order to be added to sucNodes.
     */
    private final int deltaMinSelfBombing = 1;

    /**
     * Dummy evaluator to cache the state evaluation on which basis the move ordering done here.
     */
    private final RestrictedBombEvaluator evaluator = new RestrictedBombEvaluator();

    /**
     * @param maxNumberOfMovesToConsider Maximum number of moves that should be taken into consideration.
     */
    public RestrictedBombGenerator( int maxNumberOfMovesToConsider )
    {
        assert maxNumberOfMovesToConsider > 0;

        this.maxNumberOfMovesToConsider = maxNumberOfMovesToConsider;
    }

    @Override
    public List< GameTree.Node > generateSuccessors( GameTree.Node node ) throws TimerInterruptException
    {
        assert node != null;

        List< GameTree.Node > sucNodes = new ArrayList<>();
        Set< Move > validMoves = this.chooseRandomMoves( node.getState().getAllValidMoves() );

        Map< Move, Integer > ownStones = new HashMap<>();
        Map< Move, Integer > otherStones = new HashMap<>();
        int minOwnStones = 10;
        int maxOtherStones = -1;
        Move maxOtherStonesMove = validMoves.iterator().next();

        PlayerPool players = node.getState().getPlayers();
        Player turn = node.getState().getTurnPlayer();
        int radius = node.getState().getBoard().getBombRadius() > 0 ? this.bombSimulationRadius : 0;

        PlayerEval< Integer > bombedStones;

        /* calc how many stones of turn player and other players are bombed if bomb had a bombSimulationRadius as max */
        for ( Move move : validMoves )
        {
            AI.checkDeadline();

            bombedStones = FanBase.getNumberOfBombedStones( node.getState(), move.getX(), move.getY(), radius );
            otherStones.put( move, 0 );

            for ( Player p : players )
            {
                if ( p.equals( turn ) )
                    ownStones.put( move, bombedStones.getEval( p ) );
                else
                    otherStones
                            .put( move, otherStones.put( move, otherStones.get( move ) + bombedStones.getEval( p ) ) );
            }

            if ( ownStones.get( move ) < minOwnStones )
                minOwnStones = ownStones.get( move );
            if ( otherStones.get( move ) > maxOtherStones )
            {
                maxOtherStones = otherStones.get( move );
                maxOtherStonesMove = move;
            }
        }

        State sucState;
        GameTree.Node sucNode;
        int ownBombing;
        int otherBombing;

        for ( Move move : validMoves )
        {
            AI.checkDeadline();

            ownBombing = ownStones.get( move );
            otherBombing = otherStones.get( move );

            /* only execute move if it fulfills the following criteria */
            if ( ( radius > 0 &&
                   ownBombing <= this.maxNumberOfSelfBombing
                   && otherBombing >= this.minNumberOfOtherBombing
                   && (double) otherBombing / (double) ownBombing >= this.minOtherSelfBombingRatio
                   && ownBombing <= minOwnStones + deltaMinSelfBombing
                   && otherBombing >= maxOtherStones - deltaMaxOtherBombing )
                 || ( radius == 0 && otherBombing == 1 ) )
            {
                sucState = move.execute();
                sucNode = node.createSuccessorNode( sucState, move );

                sucNode.setEvaluation(
                        this.evaluator,
                        new ParanoidDouble( (double) otherBombing / (double) maxOtherStones, turn )
                );

                sucNodes.add( sucNode );
            }
        }
        /* if no node fulfills criteria, just add one with max other bombing */
        if ( sucNodes.isEmpty() )
        {
            sucState = maxOtherStonesMove.execute();
            sucNode = node.createSuccessorNode( sucState, maxOtherStonesMove );
            sucNode.setEvaluation( this.evaluator, new ParanoidDouble( 1.0, turn ) );
            sucNodes.add( sucNode );
        }

        AI.checkDeadline();

        /* sort according to which move is most promising in the low radius */
        sucNodes.sort( ( GameTree.Node n1, GameTree.Node n2 ) -> -1 * n1.getEval( this.evaluator )
                                                                        .compareTo( n2.getEval( this.evaluator ),
                                                                                    turn ) );

        return sucNodes;
    }
}
