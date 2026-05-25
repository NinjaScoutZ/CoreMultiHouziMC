package com.houzicore.arcade.nautilus.game.arcade.game.games.wizards;

import java.util.ArrayList;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.arcade.nautilus.game.arcade.game.games.wizards.SpellType.SpellElement;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import com.houzicore.shared.core.lang.LangManager;

public class SpellMenuPage extends ShopPageBase<WizardSpellMenu, WizardSpellMenuShop>
{
	private Wizards _wizard;

	public SpellMenuPage(WizardSpellMenu plugin, WizardSpellMenuShop shop, CoreClientManager clientManager,
			DonationManager donationManager, Player player, Wizards wizard)
	{
		super(plugin, shop, clientManager, donationManager, LangManager.get().isThai(player) ? "เมนูเวทมนตร์" : "Spell Menu", player);
		_wizard = wizard;
		buildPage();
	}

	@Override
	protected void buildPage()
	{
		Wizard wizard = getWizards().getWizard(getPlayer());

		ArrayList<Integer> usedNumbers = new ArrayList<Integer>();

		for (SpellElement ele : SpellElement.values())
		{
			addItem(ele.getSlot(), new ShopItem(ele.getIcon(), ele.name(), ele.name(), 1, true, true));

			for (int i = ele.getFirstSlot(); i <= ele.getSecondSlot(); i++)
			{
				usedNumbers.add(i);
			}
		}

		for (int i = 0; i < 54; i++)
		{
			SpellType spell = null;

			for (SpellType spells : SpellType.values())
			{
				if (spells.getSlot() == i)
				{
					spell = spells;
					break;
				}
			}

			if (usedNumbers.contains(i % 9) && spell != null)
			{

				int spellLevel = wizard == null ? 1 : wizard.getSpellLevel(spell);

				if (spellLevel > 0)
				{
					ItemBuilder builder = new ItemBuilder(spell.getSpellItem());

					builder.setTitle(spell.getElement().getColor() + C.Bold + spell.getSpellName());

					builder.setAmount(spellLevel);

					builder.addLore("");

					if (wizard == null)
					{
						builder.addLore(C.cYellow + C.Bold + (LangManager.get().isThai(getPlayer()) ? "เลเวลสูงสุด: " : "Max Level: ") + C.cWhite + spell.getMaxLevel());
					}
					else
					{
						builder.addLore(C.cYellow + C.Bold + (LangManager.get().isThai(getPlayer()) ? "เลเวลเวท: " : "Spell Level: ") + C.cWhite + spellLevel);
					}

					builder.addLore(C.cYellow + C.Bold + (LangManager.get().isThai(getPlayer()) ? "ใช้มานา: " : "Mana Cost: ") + C.cWhite
							+ (wizard == null ? spell.getBaseManaCost() : spell.getManaCost(wizard)));
					builder.addLore(C.cYellow + C.Bold + (LangManager.get().isThai(getPlayer()) ? "คูลดาวน์: " : "Cooldown: ") + C.cWhite
							+ (wizard == null ? spell.getBaseCooldown() : spell.getSpellCooldown(wizard)) + (LangManager.get().isThai(getPlayer()) ? " วินาที" : " seconds"));
					builder.addLore("");

					for (String lore : spell.getDesc())
					{
						builder.addLore(C.cGray + lore, 40);
					}

					if (wizard == null)
					{
						addItem(i, new ShopItem(builder.build(), spell.name(), spell.name(), 1, true, true));
					}
					else
					{
						builder.addLore("");

						builder.addLore(C.cGreen + C.Bold + (LangManager.get().isThai(getPlayer()) ? "คลิกซ้าย" : "Left-Click") + C.cWhite + (LangManager.get().isThai(getPlayer()) ? " ผูกติดไม้กายสิทธิ์" : " Bind to Wand"));

						builder.addLore(C.cGreen + C.Bold + (LangManager.get().isThai(getPlayer()) ? "คลิกขวา" : "Right-Click") + C.cWhite + (LangManager.get().isThai(getPlayer()) ? " ร่ายเวททันที" : " Quickcast Spell"));

						addButton(i, new ShopItem(builder.build(), spell.name(), spell.name(), 1, true, true), new SpellButton(
								this, spell));
					}
				}
				else
				{
					addItem(i, new ShopItem(new ItemBuilder(Material.INK_SAC, 1, (byte) 6).setTitle(C.cRed + C.Bold + (LangManager.get().isThai(getPlayer()) ? "ไม่ทราบ" : "Unknown"))
							.build(), "Unknown", "Unknown", 1, true, true));
				}
			}
			else if (!usedNumbers.contains(i % 9))
			{
				addItem(i, new ShopItem(new ItemBuilder(Material.INK_SAC, 1, (byte) 9).setTitle(C.cRed + "").build(), LangManager.get().isThai(getPlayer()) ? "ไม่มีไอเทม" : "No Item",
						LangManager.get().isThai(getPlayer()) ? "ไม่มีไอเทม" : "No Item", 1, true, true));
			}
		}
	}

	public Wizards getWizards()
	{
		return _wizard;
	}
}
