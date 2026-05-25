package com.houzicore.arcade.nautilus.game.arcade.kit.traits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;

public abstract class Trait {

    private String _key;
    private String _name;
    private String[] _desc;
    private int _cost;
    private Material _displayMaterial;

    public Trait(String key, String name, int cost, Material displayMaterial, String... desc) {
        _key = key;
        _name = name;
        _cost = cost;
        _displayMaterial = displayMaterial;
        _desc = desc;
    }

    public String getKey() {
        return _key;
    }

    public String getName() {
        return _name;
    }

    public String[] getDesc() {
        return _desc;
    }

    public int getCost() {
        return _cost;
    }

    public Material getDisplayMaterial() {
        return _displayMaterial;
    }

    // Whether this trait applies to a specific Kit. 
    // Traits are typically scoped per game or per kit.
    public abstract boolean appliesToKit(Kit kit);

    // Provide any initial application or unapplication logic if needed, 
    // though traits are usually read passively by features.
    public void onEquip(Player player) { }
    public void onUnequip(Player player) { }
}
