package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.forms;

import com.houzicore.shared.api.disguise.DisguiseArchetype;
import com.houzicore.shared.api.disguise.DisguiseRequest;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.lang.PropRushLang;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import com.houzicore.shared.common.util.UtilTextMiddle;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.HideSeek;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BlockForm extends Form
{
    private Material _mat;
    private DisguiseRequest _disguiseRequest;

    public BlockForm(HideSeek host, Player player, Material mat)
    {
        super(host, player);
        _mat = mat;
    }

    @Override
    public void applyUsingExistingEngineState()
    {
        applyInternal(true);
    }

    @Override
    public void Apply()
    {
        applyInternal(false);
    }

    private void applyInternal(boolean isReplace)
    {
        // Clean up old passenger hacks just in case
        if (Player.getPassenger() != null)
        {
            Player.getPassenger().remove();
            Player.eject();
        }

        _disguiseRequest = buildDisguiseRequest();

        if (!isReplace) {
            Host.Manager.GetDisguise().getService().apply(Player, _disguiseRequest);
        }

        // Ensure helmet is totally cleared so it doesn't float above block.
        Player.getInventory().setHelmet(null);

        // Inform
        String blockName = ItemStackFactory.Instance.GetName(_mat, (byte) 0, false);
        if (!blockName.contains("Block")) {
            blockName += " Block";
        }

        String title = PropRushLang.get().getFallback(Player, "prop_rush.notice.form_changed");
        String sub = PropRushLang.get().getFallback(Player, "prop_rush.notice.form_changed_sub", Placeholder.unparsed("block", blockName));

        com.houzicore.shared.common.util.UtilTextBottom.display(com.houzicore.shared.common.actionbar.ActionBarChannel.GAME_STATUS, title + " » " + sub, Player);

        // Give Icon Item (Morph Tool)
        ItemStack morphTool = com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.runtime.PropRushAbilityDefinition.PROP_SWAP.createItem(Player);
        morphTool.setType(Host.GetItemEquivilent(_mat));
        Player.getInventory().setItem(com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.runtime.PropRushAbilityDefinition.PROP_SWAP.getSlot(), morphTool);
        UtilInv.Update(Player);

        Player.playSound(Player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 2f, 2f);
    }

    @Override
    public void Remove()
    {
        SolidifyRemove();
        Host.Manager.GetDisguise().getService().clear(Player);
        
        // Clean up XP bar and level values
        Player.setExp(0f);
        Player.setLevel(0);
        
        // Final override to guarantee Tablist and Hub visibility upon death/game end
        Player[] players = UtilServer.getPlayers();
        for (Player other : players) {
            if (!other.equals(Player)) {
                other.showPlayer(Host.Manager.getPlugin(), Player);
            }
        }
    }

    public void SolidifyUpdate()
    {
        // Read solidify state from the native engine and reflect it as an EXP bar indicator
        com.houzicore.shared.core.disguise.v2.engine.NativeDisguiseData disguise = Host.Manager.GetDisguise().getEngine().getDisguise(Player);
        if (disguise != null)
        {
            if (disguise.isSolidified())
            {
                Player.setExp(0.999f);
                Player.setLevel(100);
            }
            else
            {
                float progress = (float) disguise.getStillTicks() / 40.0f;
                float expVal = Math.min(0.999f, Math.max(0.0f, progress));
                Player.setExp(expVal);
                Player.setLevel((int) (expVal * 100));
            }
        }
        else
        {
            Player.setExp(0f);
            Player.setLevel(0);
        }
    }

    public void SolidifyRemove()
    {
        // Delegate to the native engine so it breaks the block and updates tracking
        Host.Manager.GetDisguise().getEngine().breakSolidify(Player, Host.Manager.GetDisguise().getEngine().getDisguise(Player));
        
        // Reset XP bar and level values upon breaking solidification
        Player.setExp(0f);
        Player.setLevel(0);
    }

    public Material GetMaterial()
    {
        return _mat;
    }

    public Block GetBlock()
    {
        return Host.Manager.GetDisguise().getEngine().getSolidBlock(Player);
    }

    /** Exposes the stored DisguiseRequest so HideSeek can re-apply after a timed reveal. */
    public DisguiseRequest getDisguiseRequest()
    {
        return _disguiseRequest;
    }

    private DisguiseRequest buildDisguiseRequest()
    {
        java.util.HashMap<String, String> attributes = new java.util.HashMap<>();
        attributes.put("blockMaterial", _mat.name());
        attributes.put("interpolationDuration", "3");
        attributes.put("notifyBar", "false");
        attributes.put("modifyBoundingBox", "true");
        attributes.put("selfViewLocked", "false");

        return new DisguiseRequest(
                Player.getUniqueId(),
                DisguiseArchetype.DISPLAY_ONLY,
                "BLOCK_DISPLAY",
                true,
                false,
                false,
                null,
                false,
                attributes);
    }
}
