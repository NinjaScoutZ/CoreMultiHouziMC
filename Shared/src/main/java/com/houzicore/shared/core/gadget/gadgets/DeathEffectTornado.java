package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.DeathEffectGadget;

public class DeathEffectTornado extends DeathEffectGadget {
	public DeathEffectTornado(GadgetManager manager) {
		super(manager, "Tornado Death", new String[] { "A violent tornado sucks your", "lifeforce away upon death!" }, -2, Material.BLAZE_POWDER, (byte) 0);
	}

	@Override
	public void PlayEffect(Player player) {
		UtilParticle.drawTornadoFrame(player.getLocation(), UtilParticle.ParticleType.FIREWORKS_SPARK, 1.5, 4.0, 0);
	}
}
