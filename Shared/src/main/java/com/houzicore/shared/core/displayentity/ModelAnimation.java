package com.houzicore.shared.core.displayentity;

import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Client-side animation controller for Display entities.
 * All animations use Paper's built-in interpolation system,
 * meaning the SERVER does almost no work — the CLIENT handles smooth transitions.
 */
public class ModelAnimation {

    public enum AnimationType {
        /** Continuous rotation around the Y axis (horizontal spin) */
        ROTATE_Y,
        /** Continuous rotation around the X axis (vertical tumble) */
        ROTATE_X,
        /** Gentle up-and-down floating motion */
        BOB_UP_DOWN,
        /** Breathing scale pulse (expand/shrink) */
        SCALE_PULSE,
        /** No animation */
        NONE
    }

    private final AnimationType _type;
    private final float _speed;
    private final float _amplitude;
    private final int _interpolationTicks;

    /**
     * @param type              The animation type
     * @param speed             Speed — degrees/tick for rotation, blocks/tick for bob
     * @param amplitude         Amplitude — unused for rotation, height for bob, scale range for pulse
     * @param interpolationTicks How many ticks the client uses to smooth between keyframes
     */
    public ModelAnimation(AnimationType type, float speed, float amplitude, int interpolationTicks) {
        _type = type;
        _speed = speed;
        _amplitude = amplitude;
        _interpolationTicks = Math.max(1, interpolationTicks);
    }

    // ── Preset Factories ─────────────────────────────

    /** Smooth continuous Y rotation (e.g. trophy / item showcase) */
    public static ModelAnimation rotateY(float degreesPerTick) {
        return new ModelAnimation(AnimationType.ROTATE_Y, degreesPerTick, 0, 2);
    }

    /** Gentle floating bob (e.g. hovering item above NPC) */
    public static ModelAnimation bob(float blocksAmplitude) {
        return new ModelAnimation(AnimationType.BOB_UP_DOWN, 0.05f, blocksAmplitude, 20);
    }

    /** Breathing scale pulse (e.g. treasure chest glow) */
    public static ModelAnimation pulse(float scaleRange) {
        return new ModelAnimation(AnimationType.SCALE_PULSE, 0.02f, scaleRange, 20);
    }

    /** No animation. */
    public static ModelAnimation none() {
        return new ModelAnimation(AnimationType.NONE, 0, 0, 1);
    }

    // ── Getters ──────────────────────────────────────

    public AnimationType getType() { return _type; }
    public float getSpeed() { return _speed; }
    public float getAmplitude() { return _amplitude; }
    public int getInterpolationTicks() { return _interpolationTicks; }

    // ── Apply to Entity ──────────────────────────────

    /**
     * Apply one animation keyframe to a Display entity.
     * Called periodically (every {@code _interpolationTicks} ticks) by DisplayModel.
     * The client then interpolates smoothly between the old and new state.
     *
     * @param entity      The display entity to animate
     * @param currentTick The global tick counter (from UpdateEvent)
     * @param baseScale   The original scale of the part (to preserve relative sizing)
     */
    public void apply(Display entity, long currentTick, Vector3f baseScale) {
        if (_type == AnimationType.NONE) return;

        Transformation current = entity.getTransformation();

        switch (_type) {
            case ROTATE_Y: {
                // Ensure no float precision loss on extreme uptime
                float angle = (float) Math.toRadians((_speed * currentTick) % 360.0);
                Quaternionf rot = new Quaternionf().rotateY(angle);
                
                // For Block Displays to rotate around their center and not the corner, 
                // the entity's actual translation must remain updated if it was shifted.
                // We keep current translation to preserve any center-pivot offsets.
                Transformation t = new Transformation(
                        current.getTranslation(),
                        rot,
                        current.getScale(),
                        current.getRightRotation()
                );
                entity.setTransformation(t);
                entity.setInterpolationDuration(_interpolationTicks);
                entity.setInterpolationDelay(0);
                break;
            }

            case ROTATE_X: {
                float angle = (float) Math.toRadians(_speed * currentTick);
                Quaternionf rot = new Quaternionf().rotateX(angle);
                Transformation t = new Transformation(
                        current.getTranslation(),
                        rot,
                        current.getScale(),
                        current.getRightRotation()
                );
                entity.setTransformation(t);
                entity.setInterpolationDuration(_interpolationTicks);
                entity.setInterpolationDelay(0);
                break;
            }

            case BOB_UP_DOWN: {
                float yOffset = (float) (Math.sin(currentTick * _speed) * _amplitude);
                Vector3f newTranslation = new Vector3f(
                        current.getTranslation().x,
                        yOffset,
                        current.getTranslation().z
                );
                Transformation t = new Transformation(
                        newTranslation,
                        current.getLeftRotation(),
                        current.getScale(),
                        current.getRightRotation()
                );
                entity.setTransformation(t);
                entity.setInterpolationDuration(_interpolationTicks);
                entity.setInterpolationDelay(0);
                break;
            }

            case SCALE_PULSE: {
                float pulseFactor = 1.0f + (float) (Math.sin(currentTick * _speed) * _amplitude);
                Vector3f newScale = new Vector3f(
                        baseScale.x * pulseFactor,
                        baseScale.y * pulseFactor,
                        baseScale.z * pulseFactor
                );
                Transformation t = new Transformation(
                        current.getTranslation(),
                        current.getLeftRotation(),
                        newScale,
                        current.getRightRotation()
                );
                entity.setTransformation(t);
                entity.setInterpolationDuration(_interpolationTicks);
                entity.setInterpolationDelay(0);
                break;
            }
        }
    }

    /**
     * @return true if this animation should be ticked (not NONE)
     */
    public boolean isActive() {
        return _type != AnimationType.NONE;
    }
}
