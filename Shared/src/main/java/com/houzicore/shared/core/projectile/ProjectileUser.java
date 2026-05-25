package com.houzicore.shared.core.projectile;

public class ProjectileUser {
    public ProjectileUser(Object... args) {}
    public boolean CanPickup(org.bukkit.entity.Player p) { return true; } {}
    public boolean Collision() { return false; } {}
    public void Effect(Object updateEvent) {}
    public org.bukkit.entity.LivingEntity GetThrower() { return null; }
    public org.bukkit.entity.Entity GetThrown() { return null; }
}
