package com.houzicore.arcade.nautilus.game.arcade.game.games.wizards.spells;

import java.util.ArrayList;
import java.util.Iterator;

import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.core.explosion.Explosion;
import com.houzicore.arcade.nautilus.game.arcade.game.games.wizards.Spell;
import com.houzicore.arcade.nautilus.game.arcade.game.games.wizards.spellinterfaces.SpellClick;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class SpellAnvilDrop extends Spell implements SpellClick
{
	private ArrayList<FallingBlock> _fallingBlocks = new ArrayList<FallingBlock>();

	@Override
	public void castSpell(Player player)
	{
		ArrayList<Player> players = new ArrayList<Player>();
		players.add(player);
		int radius = 4 + (getSpellLevel(player) * 2);

		for (Entity entity : player.getNearbyEntities(radius, radius * 3, radius))
		{
			if (entity instanceof Player && Wizards.IsAlive(entity))
			{
				players.add((Player) entity);
			}
		}

		ArrayList<FallingBlock> newFallingBlocks = new ArrayList<FallingBlock>();

		for (Player p : players)
		{
			UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, p.getLocation(), 0, 0, 0, 0, 1,
					ViewDist.LONG, UtilServer.getPlayers());

			Location loc = p.getLocation().clone().add(0, 15 + (getSpellLevel(player) * 3), 0);
			int lowered = 0;

			while (lowered < 5 && loc.getBlock().getType() != Material.AIR)
			{
				lowered++;
				loc = loc.add(0, -1, 0);
			}

			if (loc.getBlock().getType() == Material.AIR)
			{

				FallingBlock anvil = p.getWorld().spawnFallingBlock(loc.getBlock().getLocation().add(0.5, 0.5, 0.5),
						Material.ANVIL, (byte) 0);

				anvil.setMetadata("SpellLevel", new FixedMetadataValue(Wizards.getArcadeManager().getPlugin(),
						getSpellLevel(player)));

				anvil.setMetadata("Wizard", new FixedMetadataValue(Wizards.getArcadeManager().getPlugin(), player));

				anvil.getWorld().playSound(anvil.getLocation(), Sound.BLOCK_ANVIL_USE, 1.9F, 0);

				newFallingBlocks.add(anvil);

			}

		}

		if (!newFallingBlocks.isEmpty())
		{
			_fallingBlocks.addAll(newFallingBlocks);
			charge(player);
		}
	}

	private void handleAnvil(Entity entity)
	{
		_fallingBlocks.remove(entity);

		int spellLevel = entity.getMetadata("SpellLevel").get(0).asInt();
		Player wizard = (Player) entity.getMetadata("Wizard").get(0).value();
		double radius = 1 + (spellLevel / 2F);
		double maxDamage = 6 + (spellLevel * 4);
		
		java.util.HashMap<org.bukkit.entity.LivingEntity, Double> targets = com.houzicore.shared.common.util.UtilEnt.getInRadius(entity.getLocation(), radius);
		for (org.bukkit.entity.LivingEntity target : targets.keySet()) {
			Wizards.getArcadeManager().GetDamage().NewDamageEvent(target, wizard, null, org.bukkit.event.entity.EntityDamageEvent.DamageCause.CUSTOM, maxDamage * targets.get(target), false, true, false, "Wizards", "Anvil Drop");
		}
		
		java.util.HashSet<org.bukkit.block.Block> blocks = new java.util.HashSet<org.bukkit.block.Block>(com.houzicore.shared.common.util.UtilBlock.getInRadius(entity.getLocation(), radius).keySet());
		Wizards.getArcadeManager().GetExplosion().BlockExplosion(blocks, entity.getLocation(), false, false);

		entity.remove();
	}

	@EventHandler
	public void onDrop(ItemSpawnEvent event)
	{
		Iterator<FallingBlock> itel = _fallingBlocks.iterator();
		FallingBlock b = null;

		while (itel.hasNext())
		{
			FallingBlock block = itel.next();

			if (block.isDead())
			{
				b = block;
				break;
			}
		}

		if (b != null)
		{
			event.setCancelled(true);
			handleAnvil(b);
		}
	}

	@EventHandler
	public void onPlace(EntityChangeBlockEvent event)
	{
		if (_fallingBlocks.contains(event.getEntity()))
		{
			handleAnvil(event.getEntity());
			event.setCancelled(true);
		}
	}
}
