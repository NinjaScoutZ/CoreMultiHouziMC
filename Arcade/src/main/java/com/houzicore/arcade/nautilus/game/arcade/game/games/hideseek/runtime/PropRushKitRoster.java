package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.runtime;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits.KitBloodhound;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits.KitBombBug;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits.KitBountyHunter;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits.KitChameleon;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits.KitDestroyer;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits.KitExorcist;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits.KitFalconer;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits.KitGhost;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits.KitLocksmith;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits.KitMimic;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits.KitSaboteur;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits.KitTracker;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits.KitTrapper;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits.KitWarden;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;

public final class PropRushKitRoster
{
    private PropRushKitRoster()
    {
    }

    public static Kit[] createLiveRoster(ArcadeManager manager)
    {
        return new Kit[]
        {
                new KitChameleon(manager),
                new KitGhost(manager),
                new KitBombBug(manager),
                new KitLocksmith(manager),
                new KitMimic(manager),
                new KitTracker(manager),
                new KitDestroyer(manager),
                new KitTrapper(manager),
                new KitBloodhound(manager),
                new KitSaboteur(manager),
                new KitBountyHunter(manager),
                new KitExorcist(manager),
                new KitFalconer(manager),
                new KitWarden(manager)
        };
    }
}
