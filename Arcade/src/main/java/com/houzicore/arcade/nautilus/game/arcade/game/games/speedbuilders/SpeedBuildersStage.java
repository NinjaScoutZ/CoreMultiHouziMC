package com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders;

public enum SpeedBuildersStage
{
    WARM_UP,       // Rounds 1-3: 60s base time, no elimination
    MAIN_GAME,     // Rounds 4-9: 45s base time, combos active, eliminate on Round 6 and 9
    SUDDEN_DEATH   // Rounds 10+: 30s base time, 2x score, eliminate every round
}
