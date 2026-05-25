package com.houzicore.arcade.nautilus.game.arcade.game.games.minestrike;

import com.houzicore.shared.common.util.UtilMath;

import org.bukkit.Sound;

public enum Radio
{
	BOMB_PLANT(new Sound[] {Sound.ENTITY_WOLF_PANT}),
	BOMB_DEFUSE(new Sound[] {Sound.ENTITY_WOLF_SHAKE}),
	CT_WIN(new Sound[] {Sound.ENTITY_WOLF_WHINE}),
	T_WIN(new Sound[] {Sound.ENTITY_ZOMBIE_DEATH}),
	
	CT_GRENADE_HE(new Sound[] {Sound.ENTITY_SPIDER_AMBIENT}),
	CT_GRENADE_FLASH(new Sound[] {Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR}),
	CT_GRENADE_SMOKE(new Sound[] {Sound.ENTITY_WOLF_GROWL}),
	CT_GRENADE_FIRE(new Sound[] {Sound.ENTITY_WOLF_GROWL}),
	
	T_GRENADE_HE(new Sound[] {Sound.ENTITY_WITHER_HURT}),
	T_GRENADE_FLASH(new Sound[] {Sound.ENTITY_WOLF_AMBIENT}),
	T_GRENADE_SMOKE(new Sound[] {Sound.ENTITY_VILLAGER_AMBIENT}),
	T_GRENADE_FIRE(new Sound[] {Sound.ENTITY_WITHER_AMBIENT}),
	
	CT_START(new Sound[] {Sound.ENTITY_VILLAGER_HURT}),
	T_START(new Sound[] {Sound.ENTITY_VILLAGER_TRADE}),
	
	T_BOMB_PLANT(new Sound[] {Sound.ENTITY_ZOMBIE_VILLAGER_CURE}),
	T_BOMB_DROP(new Sound[] {Sound.ENTITY_ZOMBIE_INFECT}),
	;
	
	private Sound[] _sounds;
	
	Radio(Sound[] sounds)
	{
		_sounds = sounds;
	}
	
	public Sound getSound()
	{
		if (_sounds.length == 1)
			return _sounds[0];
		
		return _sounds[UtilMath.r(_sounds.length - 1)];
	}
}
