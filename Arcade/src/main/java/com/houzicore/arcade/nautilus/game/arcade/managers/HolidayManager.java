package com.houzicore.arcade.nautilus.game.arcade.managers;

import java.util.HashSet;
import java.util.Iterator;

import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.util.UtilWorld;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class HolidayManager implements Listener
{
	public enum HolidayType
	{
		Christmas(Material.CHEST, "Present", Sound.ENTITY_PLAYER_LEVELUP),
		Halloween(Material.PUMPKIN, "Pumpkin", Sound.ENTITY_ZOMBIE_VILLAGER_CURE),
		Easter(Material.CHEST, "Egg Basket", Sound.ENTITY_CAT_AMBIENT);


		private Material _blockType;
		private String _blockName;
		private Sound _blockBreakSound;

		HolidayType(Material blockType, String blockName, Sound blockBreakSound)
		{
			_blockType = blockType;
			_blockName = blockName;
			_blockBreakSound = blockBreakSound;
		}

		public String getBlockName() 
		{
			return _blockName;
		}

		public Sound getBlockSound()
		{
			return _blockBreakSound;
		}

		public Material getBlockType() 
		{
			return _blockType;
		}
	}

	private HolidayType type = HolidayType.Easter;

	ArcadeManager Manager;

	public HashSet<Block> _active = new HashSet<Block>();

	private HashSet<Item> _eggs = new HashSet<Item>();

	private HashSet<Item> _coins = new HashSet<Item>();

	public long _lastSpawn = System.currentTimeMillis();

	public HolidayManager(ArcadeManager manager)
	{
		Manager = manager;

		Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
	}	

	@EventHandler
	public void reset(GameStateChangeEvent event) 
	{
		_active.clear();

		_lastSpawn = System.currentTimeMillis();
	}

	@EventHandler
	public void blockEffect(UpdateEvent event)
	{
		if (event.getType() == UpdateType.TICK)
			return;

		Iterator<Block> blockIterator = _active.iterator();

		while (blockIterator.hasNext())
		{
			Block block = blockIterator.next();

			//Break
			if (block.getType() != Material.PUMPKIN && 
					block.getType() != Material.JACK_O_LANTERN && 
					block.getType() != Material.CHEST)
			{
				specialBlockBreak(block);
				blockIterator.remove();
				continue;
			}

			if (type == HolidayType.Halloween)
			{
				UtilParticle.PlayParticle(ParticleType.FLAME, block.getLocation().add(0.5, 0.5, 0.5), 0, 0, 0, 0.06f, 4,
						ViewDist.LONG, UtilServer.getPlayers());
				if (Math.random() > 0.90)
				{
					if (block.getType() == Material.PUMPKIN)
					{
						block.setType(Material.JACK_O_LANTERN);
					}
					else
					{
						block.setType(Material.PUMPKIN);
					}
				}
			}
			else if (type == HolidayType.Easter)
			{
				UtilParticle.PlayParticle(ParticleType.HAPPY_VILLAGER, block.getLocation().add(0.5, 0.2, 0.5), 0.3f, 0.2f, 0.3f, 0, 1,
						ViewDist.LONG, UtilServer.getPlayers());
				
				if (Math.random() > 0.90)
				{
					Item egg = block.getWorld().dropItem(block.getLocation().add(0.5, 0.8, 0.5), 
							ItemStackFactory.Instance.CreateStack(Material.EGG, (byte)0, 1, System.currentTimeMillis() + "Egg"));
					egg.setVelocity(new Vector((Math.random()-0.5)*0.3, Math.random()-0.4, (Math.random()-0.5)*0.3));

					_eggs.add(egg);

					block.getWorld().playSound(block.getLocation(), Sound.ENTITY_CHICKEN_EGG, 0.25f + (float)Math.random() * 0.75f, 0.75f + (float)Math.random() * 0.5f);

				}
				
				if (Math.random() > 0.95)
				{
					sendChestPackets(block);
				}
			}
		}
	}

	@EventHandler
	public void spawnSpecialBlockUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		if (Manager.GetGame() == null)
			return;

		Game game = Manager.GetGame();

		int requirement = (int)((double)Manager.GetPlayerFull() * 0.5d);
		if (UtilServer.getPlayers().length < requirement)
			return;

		if (game.GetState() != GameState.Live)
			return; 

		if (!UtilTime.elapsed(_lastSpawn, 90000))
			return;

		if (Math.random() > 0.01)
			return;

		int toDrop = Math.max(1, game.GetPlayers(false).size()/6);
		for (int i=0 ; i< toDrop ; i++)
		{
			double interval = 1 / (double)(toDrop);

			if (Math.random() >= (i * interval)) // Diminishing per growth
			{
				spawnSpecialBlock(findSpecialBlockLocation(game));
			}
		}	

		_lastSpawn = System.currentTimeMillis();
	}

	private void spawnSpecialBlock(Block block)
	{
		if (block == null)
		{
			return;
		}

		block.setType(type.getBlockType());
		block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, type.getBlockType());

		if (type.getBlockType() == Material.CHEST)
		{
			sendChestPackets(block);
		}

		_active.add(block);

	} 

	private void sendChestPackets(Block block)
	{
		if (block.getState() instanceof org.bukkit.block.Chest) {
			org.bukkit.block.Chest chest = (org.bukkit.block.Chest) block.getState();
			chest.open();
		}
	}

	
	private Block findSpecialBlockLocation(Game game)
	{
		Block block = null;
		int attempts = 2000;
		while (attempts > 0)
		{
			attempts--;

			int x = game.WorldData.MinX + UtilMath.r(Math.abs(game.WorldData.MaxX - game.WorldData.MinX));
			int z = game.WorldData.MinZ + UtilMath.r(Math.abs(game.WorldData.MaxZ - game.WorldData.MinZ));

			block = UtilBlock.getHighest(game.WorldData.World, x, z, null);

			if (block.getLocation().getY() <= 2 || block.getLocation().getY() < game.WorldData.MinY || block.getLocation().getY() > game.WorldData.MaxY)
				continue;

			if (block.getRelative(BlockFace.DOWN).isLiquid())
				continue;

			if (!UtilBlock.airFoliage(block) || !UtilBlock.airFoliage(block.getRelative(BlockFace.UP)))
				continue;

			if (!UtilBlock.solid(block.getRelative(BlockFace.DOWN)))
				continue;

			boolean nextToChest = false;
			for (Block other : UtilBlock.getSurrounding(block, false))
			{
				if (other.getType() == Material.CHEST)
					nextToChest = true;
			}
			if (nextToChest)
				continue;

			return block;
		}

		return null;
	}

	@EventHandler
	public void specialBlockInteract(PlayerInteractEvent event)
	{
		if (UtilPlayer.isSpectator(event.getPlayer()))
			return;
		
		if (!UtilEvent.isAction(event, ActionType.R_BLOCK))
			return;
		
		if (event.getClickedBlock() == null)
			return;
		
		if (event.getClickedBlock().getType() != Material.CHEST)
			return;
		
		if (!_active.contains(event.getClickedBlock()))
			return;
		
		event.setCancelled(true);
		
		specialBlockBreak(event.getClickedBlock());
	}
	
	@EventHandler
	public void specialBlockDamage(BlockDamageEvent event)
	{
		if (UtilPlayer.isSpectator(event.getPlayer()))
			return;

		if (!_active.contains(event.getBlock()))
			return;
		
		specialBlockBreak(event.getBlock());
	}

	private void returnHubNoResPack(Player player)
	{
		player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 10f, 1f);
		Manager.GetPortal().sendPlayerToServer(player, "Lobby");
	}

	private void specialBlockBreak(Block block)
	{
		block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, type.getBlockType());
		block.setType(Material.AIR);

		//Coins
		for (int i=0 ; i < 4 + Math.random()*16 ; i++)
		{
			Item coin = block.getWorld().dropItem(block.getLocation().add(0.5, 1, 0.5), 
					ItemStackFactory.Instance.CreateStack(Material.SUNFLOWER, (byte)0, 1, UtilMath.r(999999) + "Coin"));


			Vector vel = new Vector(
					(Math.random() - 0.5) * 0.5, 
					0.1 + Math.random() * 0.3, 
					(Math.random() - 0.5) * 0.5);

			coin.setVelocity(vel);

			coin.setPickupDelay(20);

			_coins.add(coin);
		}

		//Effect
		block.getWorld().playSound(block.getLocation(), type.getBlockSound(), 1f, 1f);
	}

	@EventHandler
	public void coinPickup(PlayerPickupItemEvent event)
	{
		if (UtilPlayer.isSpectator(event.getPlayer()))
			return;
		
		if (_coins.contains(event.getItem()))
		{
			event.setCancelled(true);
			event.getItem().remove();

			Manager.GetDonation().RewardCoins(null, type + " Coins", event.getPlayer().getName(), Manager.GetClients().Get(event.getPlayer()).getAccountId(), 4 * event.getItem().getItemStack().getAmount());

			event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f);

		}

		else if (_eggs.contains(event.getItem()))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void itemClean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		Iterator<Item> coinIterator = _coins.iterator();

		while (coinIterator.hasNext())
		{
			Item coin = coinIterator.next();

			if (!coin.isValid() || coin.getTicksLived() > 1200)
			{
				coin.remove();
				coinIterator.remove();
			}
		}

		Iterator<Item> eggIterator = _eggs.iterator();

		while (eggIterator.hasNext())
		{
			Item egg = eggIterator.next();

			if (!egg.isValid() || egg.getTicksLived() > 40)
			{
				egg.remove();
				eggIterator.remove();
			}
		}
	}
}
