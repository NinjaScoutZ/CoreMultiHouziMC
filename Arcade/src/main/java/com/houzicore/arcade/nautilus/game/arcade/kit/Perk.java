package com.houzicore.arcade.nautilus.game.arcade.kit;

import com.houzicore.arcade.ArcadeManager;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class Perk implements Listener
{
	public ArcadeManager Manager;
	public Kit Kit;

	private String _perkName;
	private String _languageKey;
	private String[] _perkDesc;

	private boolean _display;
	
	public Perk(String name)
	{
		this(name, new String[0], true);
	}

	public Perk(String name, String[] perkDesc)
	{
		_perkName = name;
		_perkDesc = perkDesc;
		_display = true;
	}
	
	public Perk(String name, String[] perkDesc, boolean display)
	{
		_perkName = name;
		_perkDesc = perkDesc;
		_display = display;
	}

	public void setupValues()
	{
	}

	public void setDesc(String... desc)
	{
		_perkDesc = desc;
	}

	protected boolean getPerkBoolean(String id)
	{
		return false;
	}

	protected int getPerkInt(String id)
	{
		return 0;
	}

	protected double getPerkDouble(String id)
	{
		return 0.0;
	}

	protected float getPerkFloat(String id)
	{
		return 0.0f;
	}

	protected double getPerkPercentage(String id)
	{
		return 0.0;
	}

	protected int getPerkTime(String id)
	{
		return 0;
	}

	protected boolean getPerkBoolean(String id, boolean defaultV)
	{
		return defaultV;
	}

	protected int getPerkInt(String id, int defaultV)
	{
		return defaultV;
	}

	protected double getPerkDouble(String id, double defaultV)
	{
		return defaultV;
	}

	protected double getPerkPercentage(String id, double defaultV)
	{
		return defaultV;
	}

	protected int getPerkTime(String id, int defaultV)
	{
		return defaultV * 1000;
	}
	public boolean hasPerk(Player player)
	{
		return Kit.HasKit(player);
	}

	public void SetHost(Kit kit)
	{
		Manager = kit.Manager;
		Kit = kit;
	}

	public String GetName()
	{	
		return _perkName;
	}

	public String GetName(Player player)
	{	
		if (_languageKey != null) {
			try {
				com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang lang = com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang.ensureInitialized(Manager.getPlugin());
				if (lang != null) {
					String key = "prop_rush.perk." + _languageKey + ".name";
					return lang.getString(player, key, _perkName);
				}
			} catch(Throwable t) {}
		}
		return _perkName;
	}

	public void setLanguageKey(String key)
	{
		_languageKey = key;
	}

	public String getLanguageKey()
	{
		return _languageKey;
	}
	
	public String[] GetDesc()
	{
		return _perkDesc;
	}

	public String[] GetDesc(Player player)
	{
		if (_languageKey != null) {
			try {
				com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang lang = com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang.ensureInitialized(Manager.getPlugin());
				if (lang != null) {
					String key = "prop_rush.perk." + _languageKey + ".desc";
					String[] translation = lang.getStringList(player, key, _perkDesc);
					if (translation != null && translation.length > 0) return translation;
				}
			} catch(Throwable t) {}
		}
		return _perkDesc;
	}

	public boolean IsVisible()
	{
		return _display;
	}
	
	public void Apply(Player player) 
	{
		//Null Default
	}

	public void registeredEvents()
	{
		// When listener has been registered
	}

	public void unregisteredEvents()
	{
		// When listener has been unregistered
	}
}
