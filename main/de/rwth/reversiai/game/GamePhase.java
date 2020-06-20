package de.rwth.reversiai.game;

/**
 * There are three possible game phases. In {@code PHASE1}, the players are asked to place their stones.
 * In {@code PHASE2}, bombs are dropped. When no move is possible anymore, the {@code END} is reached.
 */
public enum GamePhase
{
    PHASE1,
    PHASE2,
    END
}
