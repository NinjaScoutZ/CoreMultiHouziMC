package com.houzicore.shared.core.treasure;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.houzicore.shared.core.blockrestore.BlockRestore;
import com.houzicore.shared.core.hologram.HologramManager;
import com.houzicore.shared.core.reward.Reward;
import com.houzicore.shared.core.treasure.animation.Animation;
import com.houzicore.shared.core.treasure.animation.CarouselAnimation;
import com.houzicore.shared.core.treasure.animation.ParticleAnimation;
import com.houzicore.shared.core.treasure.animation.Xianxia3DAnimationEngine;
import com.houzicore.shared.core.treasure.animation.XianxiaAlchemyAnimation;
import com.houzicore.shared.core.displayentity.DisplayEntityManager;

public class Treasure {
	private final Player _player;
	private final Block _centerBlock;
	private int _tickCount;
	private final TreasureType _treasureType;

	private final Reward[] _rewards;
	private boolean _finished;
	private int _finishedTickCount;

	private final LinkedList<Animation> _animations;
	private ParticleAnimation _ambientParticles;  // Non-blocking ambient effect

	private final HologramManager _hologramManager;
	private final DisplayEntityManager _displayEntityManager;

	public Treasure(Player player, Random seed, Reward[] rewards, Block centerBlock, Block[] chestBlocks,
			TreasureType treasureType, HologramManager hologramManager, DisplayEntityManager displayEntityManager) {
		_player = player;
		_treasureType = treasureType;
		_centerBlock = centerBlock;
		_animations = new LinkedList<>();
		_hologramManager = hologramManager;
		_displayEntityManager = displayEntityManager;
		_rewards = rewards;

		// Ambient tier-specific particle orbit (non-blocking — won't prevent finish)
		_ambientParticles = new ParticleAnimation(this);
		// Start the Premium Xianxia 3D Animation Engine immediately
		_animations.add(new Xianxia3DAnimationEngine(this, _hologramManager, _displayEntityManager));
	}

	public Treasure(Player player, Reward[] rewards, Block centerBlock, Block[] chestBlocks, TreasureType treasureType,
			BlockRestore blockRestore, HologramManager hologramManager, DisplayEntityManager displayEntityManager) {
		this(player, new Random(), rewards, centerBlock, chestBlocks, treasureType, hologramManager, displayEntityManager);
	}

	public void cleanup() {
		for (final Animation animation : _animations) {
			animation.finish();
		}
		_animations.clear();
		if (_ambientParticles != null) {
			_ambientParticles.finish();
			_ambientParticles = null;
		}
	}

	public Block getCenterBlock() {
		return _centerBlock;
	}

	public int getFinishedTickCount() {
		return _finishedTickCount;
	}

	public Player getPlayer() {
		return _player;
	}

	public Reward[] getRewards() {
		return _rewards;
	}

	public TreasureType getTreasureType() {
		return _treasureType;
	}

	public boolean isFinished() {
		return _finished;
	}

	public boolean hasActiveAnimations() {
		return !_animations.isEmpty();
	}

	public void update() {
		if (_finished && !hasActiveAnimations()) {
			_finishedTickCount++;
		}

		final Iterator<Animation> taskIterator = _animations.iterator();
		while (taskIterator.hasNext()) {
			final Animation animation = taskIterator.next();

			if (animation.isRunning()) {
				animation.run();
			} else {
				taskIterator.remove();
			}
		}

		// Tick ambient particles (non-blocking — stops when main animations finish)
		if (_ambientParticles != null && _ambientParticles.isRunning()) {
			if (_animations.isEmpty()) {
				_ambientParticles.finish();
				_ambientParticles = null;
			} else {
				_ambientParticles.run();
			}
		}

		// When all blocking animations are done, mark as finished so TreasureLocation cleans it up
		if (_animations.isEmpty() && !_finished) {
			_finished = true;
		}

		_tickCount++;
	}
}
