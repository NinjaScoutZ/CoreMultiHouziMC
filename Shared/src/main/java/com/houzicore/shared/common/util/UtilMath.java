package com.houzicore.shared.common.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class UtilMath
{
	public static double trim(int degree, double d) 
	{
		String format = "#.#";
		
		for (int i=1 ; i<degree ; i++)
			format += "#";

		DecimalFormatSymbols symb = new DecimalFormatSymbols(Locale.US);
		DecimalFormat twoDForm = new DecimalFormat(format, symb);
		return Double.valueOf(twoDForm.format(d));
	}

	
	public static Random random = new Random();
	public static int r(int i) 
	{
		return random.nextInt(i);
	}
	
	public static double offset2d(Entity a, Entity b) 
	{
		return offset2d(a.getLocation().toVector(), b.getLocation().toVector());
	}
	
	public static double offset2d(Location a, Location b) 
	{
		return offset2d(a.toVector(), b.toVector());
	}
	
	public static double offset2d(Vector a, Vector b)
	{
		a.setY(0);
		b.setY(0);
		return a.subtract(b).length();
	}
	
	public static double offset(Entity a, Entity b) 
	{
		return offset(a.getLocation().toVector(), b.getLocation().toVector());
	}
	
	public static double offset(Location a, Location b) 
	{
		return offset(a.toVector(), b.toVector());
	}
	
	public static double offset(Vector a, Vector b)
	{
		return a.subtract(b).length();
	}

	public static double offsetSquared(Entity a, Entity b)
	{
		return offsetSquared(a.getLocation(), b.getLocation());
	}

	public static double offsetSquared(Location a, Location b)
	{
		return offsetSquared(a.toVector(), b.toVector());
	}

	public static double offsetSquared(Vector a, Vector b)
	{
		return a.distanceSquared(b);
	}

	public static double rr(double d, boolean bidirectional)
	{
		if (bidirectional)
			return Math.random() * (2 * d) - d;
		
		return Math.random() * d;
	}

    // ═══════════════════════════════════════════════════════════════
    // Ported from HypixelSkyBlock — MathUtility
    // ═══════════════════════════════════════════════════════════════

    /**
     * Clamp a double value between min and max (inclusive).
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Clamp an int value between min and max (inclusive).
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Generates a list of points along a quadratic Bézier curve.
     * <pre>
     *   List&lt;Location&gt; trail = bezierCurve(startLoc, controlLoc, endLoc, 20);
     *   for (Location point : trail) {
     *       UtilParticle.PlayParticle(ParticleType.FLAME, point, ...);
     *   }
     * </pre>
     * @param start   Starting point of the curve
     * @param control Control point (determines the "bend")
     * @param end     Ending point of the curve
     * @param points  Number of points to generate along the curve
     * @return List of locations along the Bézier curve
     *
     * Ported from: net.swofty.type.generic.utility.MathUtility#bezierCurve()
     * Formula: B(t) = (1-t)²·P0 + 2(1-t)t·P1 + t²·P2
     */
    public static List<Location> bezierCurve(Location start, Location control, Location end, int points) {
        List<Location> locs = new ArrayList<>();
        for (int i = 0; i <= points; i++) {
            double t = (double) i / points;
            double oneMinusT = 1.0 - t;

            double x = oneMinusT * oneMinusT * start.getX()
                     + 2 * oneMinusT * t * control.getX()
                     + t * t * end.getX();
            double y = oneMinusT * oneMinusT * start.getY()
                     + 2 * oneMinusT * t * control.getY()
                     + t * t * end.getY();
            double z = oneMinusT * oneMinusT * start.getZ()
                     + 2 * oneMinusT * t * control.getZ()
                     + t * t * end.getZ();

            locs.add(new Location(start.getWorld(), x, y, z));
        }
        return locs;
    }

    /**
     * Calculates yaw and pitch for a location to "look at" a target.
     * Useful for making NPCs face players or turrets aim at targets.
     * <pre>
     *   Location facing = lookAt(npcLoc, playerLoc);
     *   npc.teleport(facing); // NPC now faces the player
     * </pre>
     *
     * Ported from: net.swofty.type.generic.utility.MathUtility#lookAt()
     */
    public static Location lookAt(Location from, Location target) {
        Location result = from.clone();
        double dx = target.getX() - from.getX();
        double dy = target.getY() - from.getY();
        double dz = target.getZ() - from.getZ();

        double distXZ = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) Math.toDegrees(-Math.atan2(dy, distXZ));

        result.setYaw(yaw);
        result.setPitch(pitch);
        return result;
    }
}
