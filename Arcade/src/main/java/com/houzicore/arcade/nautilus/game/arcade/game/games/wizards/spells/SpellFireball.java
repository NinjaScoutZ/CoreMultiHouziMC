package com.houzicore.arcade.nautilus.game.arcade.game.games.wizards.spells;


import com.houzicore.shared.core.explosion.Explosion;
import com.houzicore.arcade.nautilus.game.arcade.game.games.wizards.Spell;
import com.houzicore.arcade.nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClick;
import org.bukkit.Sound;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class SpellFireball extends Spell implements SpellClick
{

	@EventHandler
	public void onHit(ProjectileHitEvent event)
	{
		Projectile projectile = event.getEntity();

		if (projectile.hasMetadata("FireballSpell"))
		{
			projectile.remove();

			int spellLevel = projectile.getMetadata("SpellLevel").get(0).asInt();

			float radius = (spellLevel * 0.3F) + 1F;
			java.util.HashMap<org.bukkit.entity.LivingEntity, Double> targets = com.houzicore.shared.common.util.UtilEnt.getInRadius(projectile.getLocation(), radius);
			for (org.bukkit.entity.LivingEntity target : targets.keySet()) {
				Wizards.getArcadeManager().GetDamage().NewDamageEvent(target, (Player) projectile.getMetadata("FireballSpell").get(0).value(), null, org.bukkit.event.entity.EntityDamageEvent.DamageCause.CUSTOM, (spellLevel + 6) * targets.get(target), false, true, false, "Wizards", "Fireball");
			}
			
			java.util.HashSet<org.bukkit.block.Block> blocks = new java.util.HashSet<org.bukkit.block.Block>(com.houzicore.shared.common.util.UtilBlock.getInRadius(projectile.getLocation(), radius).keySet());
			Wizards.getArcadeManager().GetExplosion().BlockExplosion(blocks, projectile.getLocation(), false, false);
		}
	}

	@Override
	public void castSpell(Player p)
	{
		org.bukkit.entity.Fireball fireball = (org.bukkit.entity.Fireball) p.getWorld().spawnEntity(p.getEyeLocation(),
				EntityType.FIREBALL);

		Vector vector = p.getEyeLocation().getDirection().normalize().multiply(0.14);

		fireball.setDirection(vector);


		fireball.setBounce(false);
		fireball.setShooter(p);
		fireball.setYield(0);
		fireball.setMetadata("FireballSpell", new FixedMetadataValue(Wizards.getArcadeManager().getPlugin(), p));
		fireball.setMetadata("SpellLevel", new FixedMetadataValue(Wizards.getArcadeManager().getPlugin(), getSpellLevel(p)));

		p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BLAZE_AMBIENT, 0.5F, 5F);
		charge(p);
	}
}
