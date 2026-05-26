package com.houzicore.shared.core.treasure.animation;

import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.cosmetic.ui.CosmeticUITemplate;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.reward.Reward;
import com.houzicore.shared.core.reward.RewardData;
import com.houzicore.shared.core.reward.RewardRarity;
import com.houzicore.shared.core.treasure.Treasure;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class XianxiaAlchemyAnimation extends Animation {

    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final String SYSTEM_PREFIX = "<gradient:#ffcc00:#ff5555><bold>☯ แดนสุขาวดี ☯</bold></gradient>";

    private final List<Reward> _rewards;
    private final Location _cauldronLoc;
    private final Location _elixirLoc;

    private BlockDisplay _cauldron;
    private ItemDisplay _elixirCore;
    private TextDisplay _rewardBoard;

    private boolean _rewardGiven = false;

    public XianxiaAlchemyAnimation(Treasure treasure, List<Reward> rewards) {
        super(treasure);
        _rewards = rewards;
        
        Location baseLoc = treasure.getCenterBlock().getLocation();
        _cauldronLoc = baseLoc.clone().add(0.5, 0.0, 0.5);
        _elixirLoc = baseLoc.clone().add(0.5, 1.2, 0.5);
    }

    private void spawnEntities() {
        // Spawn Cauldron centered on block (scale 1.45, centered translation offset = -0.725f)
        _cauldron = _cauldronLoc.getWorld().spawn(_cauldronLoc, BlockDisplay.class, entity -> {
            entity.setBlock(org.bukkit.Bukkit.createBlockData(Material.CAULDRON));
            Vector3f translation = new Vector3f(-0.725f, 0.0f, -0.725f);
            Vector3f scale = new Vector3f(1.45f, 1.45f, 1.45f);
            entity.setTransformation(new Transformation(translation, new Quaternionf(), scale, new Quaternionf()));
        });

        // Spawn Elixir Core (Dragon Egg)
        _elixirCore = _elixirLoc.getWorld().spawn(_elixirLoc, ItemDisplay.class, entity -> {
            entity.setItemStack(new ItemStack(Material.DRAGON_EGG));
            entity.setItemDisplayTransform(org.bukkit.entity.ItemDisplay.ItemDisplayTransform.GROUND);
            Vector3f translation = new Vector3f(0f, 0f, 0f);
            Vector3f scale = new Vector3f(1.2f, 1.2f, 1.2f);
            entity.setTransformation(new Transformation(translation, new Quaternionf(), scale, new Quaternionf()));
        });
    }

    @Override
    protected void tick() {
        Player player = getTreasure().getPlayer();
        if (player == null || !player.isOnline()) {
            finish();
            return;
        }

        int t = getTicks();

        // Spawn entities on the first tick
        if (t == 0) {
            spawnEntities();
        }

        // Fast Loop: 25 ticks = 1.25 seconds to finish
        if (t >= 25) {
            executeFinishPhase(player);
            finish();
            return;
        }

        // Shaking & brewing animation
        if (_cauldron != null && _cauldron.isValid()) {
            float shakeX = (float) (Math.sin(t * 2.0) * 0.04f);
            float shakeZ = (float) (Math.cos(t * 2.0) * 0.04f);
            Vector3f translation = new Vector3f(-0.725f + shakeX, 0.0f, -0.725f + shakeZ);
            Vector3f scale = new Vector3f(1.45f, 1.45f, 1.45f);
            _cauldron.setTransformation(new Transformation(translation, new Quaternionf(), scale, new Quaternionf()));
        }

        if (_elixirCore != null && _elixirCore.isValid()) {
            float angle = (float) Math.toRadians(t * 20.0); // Spin core egg faster (20 degrees per tick)
            Quaternionf rot = new Quaternionf().rotateY(angle);
            Vector3f translation = new Vector3f(0f, 0f, 0f);
            Vector3f scale = new Vector3f(1.2f, 1.2f, 1.2f);
            _elixirCore.setTransformation(new Transformation(translation, rot, scale, new Quaternionf()));
        }

        // Particle effects
        _cauldronLoc.getWorld().spawnParticle(
                Particle.SOUL_FIRE_FLAME,
                _cauldronLoc.clone().add(0, 0.15, 0),
                1, 0.1, 0.05, 0.1, 0.01
        );

        _elixirLoc.getWorld().spawnParticle(
                Particle.DRAGON_BREATH,
                _elixirLoc,
                2, 0.1, 0.1, 0.1, 0.04
        );

        if (t % 5 == 0) {
            player.playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 0.7f, 0.8f + (t * 0.03f));
        }
    }

    private void executeFinishPhase(Player player) {
        if (_rewardGiven) return;
        _rewardGiven = true;

        cleanupEntities();

        if (_rewards == null || _rewards.isEmpty()) return;

        Reward mainReward = _rewards.get(0);
        RewardData rewardData = mainReward.giveReward("Treasure", player);

        // Grant remaining rewards if any
        for (int i = 1; i < _rewards.size(); i++) {
            _rewards.get(i).giveReward("Treasure", player);
        }

        if (rewardData == null) return;

        RewardRarity rarity = mainReward.getRarity();
        CosmeticRarity cosmeticRarity = mapRarity(rarity);
        String rColor = CosmeticUITemplate.getRarityHexColor(cosmeticRarity);
        String cleanRewardName = org.bukkit.ChatColor.stripColor(rewardData.getFriendlyName());

        // Particle Boom effects
        _cauldronLoc.getWorld().spawnParticle(Particle.SONIC_BOOM, _elixirLoc, 1);
        _cauldronLoc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, _elixirLoc, 50, 0.3, 0.3, 0.3, 0.12);

        // Lightning strike for Legendary or Mythical rarities
        if (cosmeticRarity == CosmeticRarity.LEGENDARY || cosmeticRarity == CosmeticRarity.MYTHIC) {
            _elixirLoc.getWorld().strikeLightningEffect(_elixirLoc);
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8f, 1.0f);
        }

        // Victory sounds
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.15f);

        // Spawn Floating Text Board
        _rewardBoard = _elixirLoc.getWorld().spawn(_elixirLoc, TextDisplay.class, textEnt -> {
            textEnt.text(mm.deserialize("<bold>" + SYSTEM_PREFIX + "</bold>\n<bold><" + rColor + ">" + cleanRewardName + "</" + rColor + "></bold>"));
            textEnt.setBillboard(TextDisplay.Billboard.CENTER);
            textEnt.setShadowed(true);
            textEnt.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0));
        });

        // Autoclean text board after 2.5 seconds (50 ticks)
        try {
            org.bukkit.plugin.Plugin plugin = JavaPlugin.getProvidingPlugin(XianxiaAlchemyAnimation.class);
            if (plugin != null) {
                final TextDisplay finalBoard = _rewardBoard;
                org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (finalBoard != null && finalBoard.isValid()) {
                        finalBoard.remove();
                    }
                }, 50L);
            }
        } catch (Exception ignored) {}

        // Localized Announcements (Thai / English support)
        boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
        String personalMsg;
        if (isThai) {
            personalMsg = SYSTEM_PREFIX + " <gray>เตาหลอมยาแตกออก! คุณหลอมได้คัมภีร์ลับ:</gray> <" + rColor + "><bold>[" + cleanRewardName + "]</bold></" + rColor + ">";
        } else {
            personalMsg = SYSTEM_PREFIX + " <gray>The alchemy cauldron bursts! You have mastered:</gray> <" + rColor + "><bold>[" + cleanRewardName + "]</bold></" + rColor + ">";
        }
        player.sendMessage(mm.deserialize(personalMsg));

        // Global chat announcement for Epic, Legendary, and Mythical tiers
        if (cosmeticRarity == CosmeticRarity.EPIC || cosmeticRarity == CosmeticRarity.LEGENDARY || cosmeticRarity == CosmeticRarity.MYTHIC) {
            for (Player oPlayer : UtilServer.getPlayers()) {
                boolean oIsThai = com.houzicore.shared.core.lang.LangManager.get().isThai(oPlayer);
                String msg;
                if (oIsThai) {
                    msg = SYSTEM_PREFIX + " <gold>" + player.getName() + "</gold> <gray>ได้ไขว่คว้าโชคชะตาหลอมโอสถสำเร็จ ได้รับ:</gray> <" + rColor + "><bold>[" + cleanRewardName + "]</bold></" + rColor + ">";
                } else {
                    msg = SYSTEM_PREFIX + " <gold>" + player.getName() + "</gold> <gray>has refined a celestial elixir, unlocking:</gray> <" + rColor + "><bold>[" + cleanRewardName + "]</bold></" + rColor + ">";
                }
                oPlayer.sendMessage(mm.deserialize(msg));
            }
        }
    }

    private CosmeticRarity mapRarity(RewardRarity rarity) {
        if (rarity == null) return CosmeticRarity.COMMON;
        return switch (rarity) {
            case OTHER, COMMON -> CosmeticRarity.COMMON;
            case UNCOMMON -> CosmeticRarity.RARE;
            case RARE -> CosmeticRarity.EPIC;
            case LEGENDARY -> CosmeticRarity.LEGENDARY;
            case MYTHICAL -> CosmeticRarity.MYTHIC;
        };
    }

    private void cleanupEntities() {
        if (_cauldron != null && _cauldron.isValid()) {
            _cauldron.remove();
            _cauldron = null;
        }
        if (_elixirCore != null && _elixirCore.isValid()) {
            _elixirCore.remove();
            _elixirCore = null;
        }
        if (_rewardBoard != null && _rewardBoard.isValid()) {
            _rewardBoard.remove();
            _rewardBoard = null;
        }
    }

    @Override
    protected void onFinish() {
        cleanupEntities();

        // Safe check: if animation ended prematurely (e.g. quit event), ensure rewards are given
        if (!_rewardGiven) {
            Player player = getTreasure().getPlayer();
            if (player != null && _rewards != null) {
                for (Reward reward : _rewards) {
                    reward.giveReward("Treasure", player);
                }
            }
            _rewardGiven = true;
        }
    }
}
