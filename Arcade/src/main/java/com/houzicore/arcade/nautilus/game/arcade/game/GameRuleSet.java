package com.houzicore.arcade.nautilus.game.arcade.game;

public class GameRuleSet {

    // Combat
    private boolean damage = true;
    private boolean damagePvP = true;
    private boolean damagePvE = true;
    private boolean damageEvP = true;
    private boolean damageFall = true;
    private boolean damageSelf = false;
    private boolean damageTeamSelf = false;
    private boolean damageTeamOther = true;

    // Movement
    private boolean sprint = true;

    // Items
    private boolean itemDrop = false;
    private boolean itemPickup = false;

    // World interaction
    private boolean blockBreak = false;
    private boolean blockPlace = false;

    // Death / quit behaviour
    private boolean deathOut = true;
    private boolean deathDropItems = false;
    private boolean quitDropItems = false;

    // Prepare phase
    private boolean prepareFreeze = true;

    // Inventory interaction
    private boolean inventoryClick = false;
    private boolean inventoryOpenBlock = false;
    private boolean inventoryOpenChest = false;

    // Default constructor for standard minigame
    public GameRuleSet() {}

    // Builder pattern for fluently defining rules
    public static class Builder {
        private final GameRuleSet rules = new GameRuleSet();

        public Builder damage(boolean val) { rules.damage = val; return this; }
        public Builder damagePvP(boolean val) { rules.damagePvP = val; return this; }
        public Builder damagePvE(boolean val) { rules.damagePvE = val; return this; }
        public Builder damageEvP(boolean val) { rules.damageEvP = val; return this; }
        public Builder damageFall(boolean val) { rules.damageFall = val; return this; }
        public Builder damageSelf(boolean val) { rules.damageSelf = val; return this; }
        public Builder damageTeamSelf(boolean val) { rules.damageTeamSelf = val; return this; }
        public Builder damageTeamOther(boolean val) { rules.damageTeamOther = val; return this; }

        public Builder sprint(boolean val) { rules.sprint = val; return this; }

        public Builder itemDrop(boolean val) { rules.itemDrop = val; return this; }
        public Builder itemPickup(boolean val) { rules.itemPickup = val; return this; }
        public Builder blockBreak(boolean val) { rules.blockBreak = val; return this; }
        public Builder blockPlace(boolean val) { rules.blockPlace = val; return this; }

        public Builder deathOut(boolean val) { rules.deathOut = val; return this; }
        public Builder deathDropItems(boolean val) { rules.deathDropItems = val; return this; }
        public Builder quitDropItems(boolean val) { rules.quitDropItems = val; return this; }

        public Builder prepareFreeze(boolean val) { rules.prepareFreeze = val; return this; }

        public Builder inventoryClick(boolean val) { rules.inventoryClick = val; return this; }
        public Builder inventoryOpenBlock(boolean val) { rules.inventoryOpenBlock = val; return this; }
        public Builder inventoryOpenChest(boolean val) { rules.inventoryOpenChest = val; return this; }

        public GameRuleSet build() { return rules; }
    }

    public boolean isDamage() { return damage; }
    public boolean isDamagePvP() { return damagePvP; }
    public boolean isDamagePvE() { return damagePvE; }
    public boolean isDamageEvP() { return damageEvP; }
    public boolean isDamageFall() { return damageFall; }
    public boolean isDamageSelf() { return damageSelf; }
    public boolean isDamageTeamSelf() { return damageTeamSelf; }
    public boolean isDamageTeamOther() { return damageTeamOther; }

    public boolean isSprint() { return sprint; }

    public boolean isItemDrop() { return itemDrop; }
    public boolean isItemPickup() { return itemPickup; }
    public boolean isBlockBreak() { return blockBreak; }
    public boolean isBlockPlace() { return blockPlace; }

    public boolean isDeathOut() { return deathOut; }
    public boolean isDeathDropItems() { return deathDropItems; }
    public boolean isQuitDropItems() { return quitDropItems; }

    public boolean isPrepareFreeze() { return prepareFreeze; }

    public boolean isInventoryClick() { return inventoryClick; }
    public boolean isInventoryOpenBlock() { return inventoryOpenBlock; }
    public boolean isInventoryOpenChest() { return inventoryOpenChest; }
}
