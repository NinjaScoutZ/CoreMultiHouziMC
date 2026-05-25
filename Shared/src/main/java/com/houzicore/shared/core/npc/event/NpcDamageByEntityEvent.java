package com.houzicore.shared.core.npc.event;

import org.bukkit.entity.LivingEntity;

public class NpcDamageByEntityEvent extends NpcEvent {
	private final LivingEntity _damager;

	public NpcDamageByEntityEvent(LivingEntity npc, LivingEntity damager) {
		super(npc);

		_damager = damager;
	}

	public LivingEntity getDamager() {
		return _damager;
	}
}
