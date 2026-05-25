package com.houzicore.shared.common.util;

import org.bukkit.entity.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import java.util.UUID;

public class UtilSkull
{
	public static byte getSkullData(Entity entity)
	{
		if (entity instanceof WitherSkeleton)
			return 1;
		else if (entity instanceof Skeleton)
			return 0;
		else if (entity instanceof Zombie || entity instanceof Giant)
		{
			return 2;
		}
		else if (entity instanceof Creeper)
		{
			return 4;
		}
		else
			return 3;
	}

	public static boolean isPlayerHead(byte data)
	{
		return data == 3;
	}

	public static String getPlayerHeadName(Entity entity)
	{
		String name = "MHF_Alex";

		// order is important for some of these
		if (entity instanceof Blaze)
			name = "MHF_Blaze";
		else if (entity instanceof CaveSpider)
			name = "MHF_CaveSpider";
		else if (entity instanceof Spider)
			name = "MHF_Spider";
		else if (entity instanceof Chicken)
			name = "MHF_Chicken";
		else if (entity instanceof MushroomCow)
			name = "MHF_MushroomCow";
		else if (entity instanceof Cow)
			name = "MHF_Cow";
		else if (entity instanceof Creeper)
			name = "MHF_Creeper";
		else if (entity instanceof Enderman)
			name = "MHF_Enderman";
		else if (entity instanceof Ghast)
			name = "MHF_Ghast";
		else if (entity instanceof Golem)
			name = "MHF_Golem";
		else if (entity instanceof PigZombie)
			name = "MHF_PigZombie";
		else if (entity instanceof MagmaCube)
			name = "MHF_LavaSlime";
		else if (entity instanceof Slime)
			name = "MHF_Slime";
		else if (entity instanceof Ocelot)
			name = "MHF_Ocelot";
		else if (entity instanceof PigZombie)
			name = "MHF_PigZombie";
		else if (entity instanceof Pig)
			name = "MHF_Pig";
		else if (entity instanceof Sheep)
			name = "MHF_Pig";
		else if (entity instanceof Squid)
			name = "MHF_Squid";
		else if (entity instanceof HumanEntity)
			name = "MHF_Steve";
		else if (entity instanceof Villager)
			name = "MHF_Villager";

		return name;
	}

    /**
     * Generates a custom skull ItemStack from a base64 texture string.
     */
    public static ItemStack getCustomSkull(String texture) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null && texture != null && !texture.isEmpty()) {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), null);
            profile.setProperty(new ProfileProperty("textures", texture));
            meta.setPlayerProfile(profile);
            head.setItemMeta(meta);
        }
        return head;
    }
}
