package com.houzicore.arcade.nautilus.game.arcade.kit.traits;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.entity.Player;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;

public class TraitManager {
    
    private ArcadeManager _manager;
    private HashSet<Trait> _registeredTraits = new HashSet<>();
    
    // Map of Player Name -> Map of MapName/GameType/KitName -> Equipped Trait Key
    // For now, simpler: Player Name -> Map of Kit Name -> Trait
    private HashMap<String, HashMap<String, Trait>> _equippedTraits = new HashMap<>();

    public TraitManager(ArcadeManager manager) {
        _manager = manager;
    }
    
    public void registerTrait(Trait trait) {
        _registeredTraits.add(trait);
    }
    
    public HashSet<Trait> getTraitsForKit(Kit kit) {
        HashSet<Trait> applicable = new HashSet<>();
        for (Trait t : _registeredTraits) {
            if (t.appliesToKit(kit)) {
                applicable.add(t);
            }
        }
        return applicable;
    }
    
    public Trait getEquippedTrait(Player player, Kit kit) {
        if (!_equippedTraits.containsKey(player.getName())) return null;
        return _equippedTraits.get(player.getName()).get(kit.GetName());
    }
    
    public void equipTrait(Player player, Kit kit, Trait trait) {
        if (!_equippedTraits.containsKey(player.getName())) {
            _equippedTraits.put(player.getName(), new HashMap<>());
        }
        
        Trait old = _equippedTraits.get(player.getName()).get(kit.GetName());
        if (old != null) old.onUnequip(player);
        
        _equippedTraits.get(player.getName()).put(kit.GetName(), trait);
        
        if (trait != null) {
            trait.onEquip(player);
        }
        
        // TODO: Save to SQL account_stats asynchronously
    }

    public void clearEquippedTrait(Player player, Kit kit) {
        equipTrait(player, kit, null);
    }
    
    // Check if player owns the trait via Donation Manager
    public boolean ownsTrait(Player player, Trait trait) {
        return _manager.GetDonation().Get(player.getName()).OwnsUnknownPackage(getTraitSalesPackageName(trait));
    }
    
    public String getTraitSalesPackageName(Trait trait) {
        return "Arcade Trait " + trait.getKey();
    }
}
