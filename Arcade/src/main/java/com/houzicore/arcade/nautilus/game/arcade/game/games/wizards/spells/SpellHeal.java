package com.houzicore.arcade.nautilus.game.arcade.game.games.wizards.spells;

import com.houzicore.arcade.nautilus.game.arcade.game.games.wizards.Spell;
import com.houzicore.arcade.nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClick;
import org.bukkit.Effect;
import org.bukkit.entity.Player;

public class SpellHeal extends Spell implements SpellClick
{

	@Override
	public void castSpell(Player p)
	{
		if (p.getHealth() < p.getMaxHealth())
		{
			double health = p.getHealth() + (3 + getSpellLevel(p));

			if (health > p.getMaxHealth())
				health = p.getMaxHealth();

			p.setHealth(health);

			p.getWorld().spawnParticle(org.bukkit.Particle.HEART, p.getEyeLocation(), 6, 0.8, 0.4, 0.8, 0);

			charge(p);
		}
	}
}
