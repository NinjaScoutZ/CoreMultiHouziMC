package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.Iterator;

import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class PerkRangedBleeding extends Perk
{

    public PerkRangedBleeding()
    {
        super("Bleeding", new String[]
            {
                    "After being hit by a arrow", "You will bleed for a few seconds"
            });
    }

    @EventHandler
    public void onSecond(UpdateEvent event)
    {
        if (event.getType() != UpdateType.FAST)
        {
            return;
        }

        Iterator<LivingEntity> itel = _timeBleeding.keySet().iterator();
        while (itel.hasNext())
        {
            LivingEntity entity = itel.next();
            if (entity.isDead() || (entity instanceof Player && !Manager.IsAlive((Player) entity)))
            {
                itel.remove();
                continue;
            }
            Manager.GetDamage().NewDamageEvent(entity, null, null, DamageCause.CUSTOM, 1, false, true, true, "Bleed",
                    "Stitcher Bleeding");
            com.houzicore.shared.common.util.UtilParticle.PlayParticle(
                    com.houzicore.shared.common.util.UtilParticle.ParticleType.RED_DUST, 
                    entity.getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 0f, 10, 
                    com.houzicore.shared.common.util.UtilParticle.ViewDist.NORMAL, 
                    com.houzicore.shared.common.util.UtilServer.getPlayers());
            if (_timeBleeding.get(entity) <= 1)
            {
                itel.remove();
            }
            else
            {
                _timeBleeding.put(entity, _timeBleeding.get(entity) - 1);
            }
        }
    }

    private NautHashMap<LivingEntity, Integer> _timeBleeding = new NautHashMap<LivingEntity, Integer>();

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event)
    {
        if (event.getCause() == DamageCause.PROJECTILE)
        {
            if (!(event.getDamager() instanceof Player)) return;
            Player player = ((Player) event.getDamager());
            if (player != null && Kit.HasKit(player))
            {
                LivingEntity entity = (LivingEntity)event.getEntity();
                if (!_timeBleeding.containsKey(entity))
                {
                    _timeBleeding.put(entity, 4);
                }
                else
                {
                    _timeBleeding.put(entity, _timeBleeding.get(entity) + 4);
                }
            }
        }
    }

}
