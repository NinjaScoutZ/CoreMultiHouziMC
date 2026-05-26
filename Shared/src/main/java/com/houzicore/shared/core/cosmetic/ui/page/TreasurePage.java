package com.houzicore.shared.core.cosmetic.ui.page;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.cosmetic.CosmeticManager;
import com.houzicore.shared.core.cosmetic.ui.CosmeticShop;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.core.treasure.TreasureManager;
import com.houzicore.shared.core.treasure.TreasureType;
import com.houzicore.shared.core.treasure.gui.TreasureDetailPage;

import net.kyori.adventure.text.minimessage.MiniMessage;

public class TreasurePage extends ShopPageBase<CosmeticManager, CosmeticShop> {

	private final TreasureManager _treasureManager;
	private static final MiniMessage mm = MiniMessage.miniMessage();

	public TreasurePage(CosmeticManager plugin, CosmeticShop shop, CoreClientManager clientManager,
			DonationManager donationManager, TreasureManager treasureManager, String name, Player player) {
		super(plugin, shop, clientManager, donationManager, name, player, 45); // ขยายขอบเขตสล็อตเป็น 5 แถว (45 ช่อง)
		_treasureManager = treasureManager;
		buildPage();
	}

	@Override
	protected void buildPage() {
		// วาดกระจกกรอบนิรภัยด้านข้างล็อบบี้ส่วนกลาง
		ItemStack borderPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
		for (int i = 0; i < 9; i++) {
			getInventory().setItem(i, borderPane);
			getInventory().setItem(36 + i, borderPane);
		}
		getInventory().setItem(9, borderPane);
		getInventory().setItem(17, borderPane);
		getInventory().setItem(18, borderPane);
		getInventory().setItem(26, borderPane);
		getInventory().setItem(27, borderPane);
		getInventory().setItem(35, borderPane);

		boolean isThai = LangManager.get().isThai(getPlayer());

		// จัดเรียงสล็อต 5 เกรดเซียนให้อยู่ตำแหน่งสมมาตรและเปิดเข้าหน้าต่างย่อยได้โดยตรง
		renderTierIcon(19, TreasureType.OLD, isThai);
		renderTierIcon(21, TreasureType.ANCIENT, isThai);
		renderTierIcon(23, TreasureType.MYTHICAL, isThai);
		renderTierIcon(25, TreasureType.IMMORTAL, isThai);
		renderTierIcon(31, TreasureType.DIVINE, isThai); // อยู่ศูนย์กลางแถวที่ 4 พรีเมียมที่สุด
	}

	private void renderTierIcon(int slot, TreasureType type, boolean isThai) {
		ItemStack chestItem = new ItemStack(type.getMaterial());
		var meta = chestItem.getItemMeta();
		if (meta != null) {
			meta.displayName(mm.deserialize("<bold>" + type.getDisplayName(isThai) + "</bold>"));
			
			List<net.kyori.adventure.text.Component> lines = new ArrayList<>();
			lines.add(mm.deserialize("<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>"));
			lines.add(isThai ? mm.deserialize("<gray>▪ คลิกซ้ายเพื่อตรวจสอบ <yellow>อัตราเรทสุ่มรางวัล</yellow></gray>")
							 : mm.deserialize("<gray>▪ Left-Click to inspect <yellow>Drop Rates & Store</yellow></gray>"));
			lines.add(isThai ? mm.deserialize("<gray>▪ และทำพิธีเปิดหลอมโอสถเพิ่มคอสเมติกสวมใส่</gray>")
							 : mm.deserialize("<gray>▪ and unlock exclusive cosmetic packages.</gray>"));
			lines.add(mm.deserialize("<dark_gray>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬</dark_gray>"));
			
			meta.lore(lines);
			chestItem.setItemMeta(meta);
		}

		// ดึงระบบเวิร์กโหลดเวกเตอร์ย้ายหน้าต่างไปยังหน้าคลังย่อย TreasureDetailPage
		addButton(slot, chestItem, (player, click) -> {
			player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_CHEST_OPEN, 1.0f, 1.1f);
			
			// สร้างอินสแตนซ์หน้าต่างย่อยและเปิดสลับ Layout ทันที
			var detailPage = new TreasureDetailPage(
					_treasureManager,
					null, // ใช้พอร์ตเนทีฟส่งผ่านค่าความเสถียร
					_treasureManager.getTreasureLocation(player.getLocation()), // ค้นหาจุดพิกัดแท่นบูชาใกล้เคียง
					getClientManager(),
					getDonationManager(),
					_treasureManager.getInventoryService(),
					type,
					this,
					player
			);
			player.openInventory(detailPage.getInventory());
		});
	}

	public void update() {
		buildPage();
	}
}
