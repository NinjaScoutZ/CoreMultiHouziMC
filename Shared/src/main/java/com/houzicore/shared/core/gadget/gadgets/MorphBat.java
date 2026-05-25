package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.core.disguise.disguises.DisguiseBat;
import com.houzicore.shared.core.projectile.IThrown;
import com.houzicore.shared.core.projectile.ProjectileUser;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.MorphGadget;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

public class MorphBat extends MorphGadget implements IThrown {
	public MorphBat(GadgetManager manager) {
		super(manager, "Bat Morph",
				new String[] { C.cWhite + "Flap around and annoy people by",
						C.cWhite + "screeching loudly into their ears!", " ",
						C.cYellow + "Left Click" + C.cGray + " to use " + C.cGreen + "Screech",
						C.cYellow + "Double Jump" + C.cGray + " to use " + C.cGreen + "Flap",
						C.cYellow + "Tap Sneak" + C.cGray + " to use " + C.cGreen + "Poop", },
				40000, Material.PLAYER_HEAD, (byte) 1);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) {
		if (target != null) {
			// Effect
			target.playEffect(EntityEffect.HURT);

			target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 1), true);

			// Inform
			UtilPlayer.message(target, F.main("Skill",
					F.name(UtilEnt.getName(data.GetThrower())) + " hit you with " + F.skill("Bat Poop") + "."));

			UtilPlayer.message(data.GetThrower(), F.main("Skill",
					"You hit " + F.name(UtilEnt.getName(target)) + " with " + F.skill("Bat Poop") + "."));
		}

		data.GetThrown().remove();
	}

	@Override
	public void DisableCustom(Player player) {
		RemoveArmor(player);
		Manager.getDisguiseManager().undisguise(player);

		player.setAllowFlight(false);
		player.setFlying(false);
	}

	@Override
	public void EnableCustom(final Player player) {
		ApplyArmor(player);

		final DisguiseBat disguise = new DisguiseBat(player);
		//disguise.setName(player.getName(), Manager.getClientManager().Get(player).GetRank());
		//disguise.setCustomNameVisible(true);
		Manager.getDisguiseManager().disguise(disguise);
	}

	@Override
	public void Expire(ProjectileUser data) {
		data.GetThrown().remove();
	}

	@EventHandler
	public void Flap(PlayerToggleFlightEvent event) {
		final Player player = event.getPlayer();

		if (player.getGameMode() == GameMode.CREATIVE)
			return;

		if (!IsActive(player))
			return;

		event.setCancelled(true);
		player.setFlying(false);

		// Disable Flight
		player.setAllowFlight(false);

		// Velocity
		UtilAction.velocity(player, player.getLocation().getDirection(), 0.8, false, 0, 0.5, 0.8, true);

		// Sound
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, (float) (0.3 + player.getExp()),
				(float) (Math.random() / 2 + 0.5));

		// Set Recharge
		Recharge.Instance.use(player, GetName(), 40, false, false);
	}

	@EventHandler
	public void FlapUpdate(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK)
			return;

		for (final Player player : GetActive()) {
			if (player.getGameMode() == GameMode.CREATIVE) {
				continue;
			}

			if (UtilEnt.isGrounded(player)
					|| UtilBlock.solid(player.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
				player.setAllowFlight(true);
			} else if (Recharge.Instance.usable(player, GetName())) {
				player.setAllowFlight(true);
			}
		}
	}

	@Override
	public void Idle(ProjectileUser data) {
		data.GetThrown().remove();
	}

	@EventHandler
	public void Poop(PlayerToggleSneakEvent event) {
		final Player player = event.getPlayer();

		if (player.isSneaking())
			return;

		if (player.getGameMode() == GameMode.CREATIVE)
			return;

		if (!IsActive(player))
			return;

		if (!Recharge.Instance.use(player, "Poop", 4000, true, false))
			return;

		// Action
		final Item item = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()),
				ItemStackFactory.Instance.CreateStack(Material.MELON_SEEDS));
		UtilAction.velocity(item, player.getLocation().getDirection(), 0.01, true, -0.3, 0, 10, false);

		Manager.getProjectileManager().AddThrow(item, player, this, -1, true, true, true, null, 1f, 1f, null, null, 0,
				UpdateType.TICK, 0.5f);

		// Inform
		boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
		UtilPlayer.message(player, F.main("Skill", isThai ? "\u00A77\u0e04\u0e38\u0e13\u0e43\u0e0a\u0e49 " + F.skill("Poop") : "§7You used " + F.skill("Poop")));

		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1f, 0.1f);
	}

	@EventHandler
	public void Screech(PlayerInteractEvent event) {
		final Player player = event.getPlayer();

		if (!IsActive(player))
			return;

		if (!UtilEvent.isAction(event, ActionType.L))
			return;

		if (!Recharge.Instance.use(player, GetName(), 100, false, false))
			return;

		// Effect
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_HURT, 1f, 1f);
	}
}
