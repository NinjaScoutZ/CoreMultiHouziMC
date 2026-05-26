---
name: create-shop-gui
description: Create a premium Shop GUI menu using ShopBase/ShopPageBase with blue glass border and bilingual support
---

# Create Shop GUI Skill

Use this skill when the user asks to create a menu, shop, GUI, or inventory-based UI.

## ⚠️ CRITICAL RULE
**NEVER use `Bukkit.createInventory()` + `InventoryClickEvent`** — always use the HouziCore `ShopBase` / `ShopPageBase` framework.

## Persistence Check

If this GUI buys, grants, consumes, equips, or previews an owned item, also read:

- `.agents/references/database_persistence.md`
- `.agents/rules/database_sql_rules.md`

Before implementing, decide:

1. is this preview-only?
2. or does it change ownership?

If ownership changes, define the canonical item key and category, then verify the `InventoryManager` path.

## Step 1: Create the Shop Class

```java
package com.houzicore.lobby.hub.modules.myfeature;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import org.bukkit.entity.Player;

public class MyShop extends ShopBase<MyManager> {
    public MyShop(MyManager plugin, CoreClientManager clientManager, DonationManager donationManager) {
        super(plugin, clientManager, donationManager, "My Shop Title");
    }

    @Override
    protected ShopPageBase<MyManager, MyShop> buildPagesFor(Player player) {
        return new MyShopPage(getPlugin(), this, getClientManager(), getDonationManager(), player);
    }
}
```

## Step 2: Create the Page (54-slot)

```java
package com.houzicore.lobby.hub.modules.myfeature;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MyShopPage extends ShopPageBase<MyManager, MyShop> {
    public MyShopPage(MyManager plugin, MyShop shop, CoreClientManager clientManager,
                      DonationManager donationManager, Player player) {
        super(plugin, shop, clientManager, donationManager, "My Page", player, 54);
        buildPage();
    }

    @Override
    protected void buildPage() {
        // 1. Blue glass border (MANDATORY — never leave empty slots)
        ItemStack glass = ItemStackFactory.Instance.CreateStack(
            Material.STAINED_GLASS_PANE, (byte) 3, 1,
            "§bEssence: " + getDonationManager().Get(getPlayer()).getGems()
        );
        for (int i = 0; i < getSize(); i++) {
            getInventory().setItem(i, glass);
        }

        // 2. Add interactive buttons using IButton lambda
        addButton(13, ItemStackFactory.Instance.CreateStack(
            Material.DIAMOND, (byte) 0, 1, "§a§lClick Me!",
            new String[]{"§7Description line 1", "§8───────────", "§eClick to activate!"}),
            (player, clickType) -> {
                playAcceptSound(player);
                player.sendMessage("§a✓ Action performed!");
            }
        );

        // 3. Deny button example
        addButton(31, ItemStackFactory.Instance.CreateStack(
            Material.BARRIER, (byte) 0, 1, "§c§lLocked"),
            (player, clickType) -> {
                playDenySound(player);
                player.sendMessage("§c✗ You don't have permission!");
            }
        );
    }
}
```

## Step 3: Open the Shop

From your Manager:
```java
private MyShop _shop;

// In constructor:
_shop = new MyShop(this, clientManager, donationManager);

// When triggered:
_shop.attemptShopOpen(player);
```

## Pagination (28 items per page)

For dynamic lists:
- **28 usable slots** per page (center area)
- **Slot 45:** ⬅️ Previous Page
- **Slot 49:** 🛌 Go Back / Close
- **Slot 53:** ➡️ Next Page

## Design Rules Checklist

- [ ] Blue glass pane border (byte 3) on ALL empty slots
- [ ] Bilingual item names via `LangManager.get().get(player, "key")`
- [ ] Sound on click: `playAcceptSound()` for success, `playDenySound()` for failure
- [ ] Lore text: 45 chars max per line, `§8───` separators, poetic Thai style
- [ ] Enchant glow on equipped items: `addUnsafeEnchantment` + `HIDE_ENCHANTS`
- [ ] 1-tick delay between `closeInventory()` and `openInventory()` (ShopBase handles this)
- [ ] Currency display in glass pane name: `§bEssence: §a{amount}`
- [ ] If the GUI changes ownership, item key/category/persistence path checked against DB rules
