package com.houzicore.shared.common.util;

import java.util.List;
import java.util.Optional;

/**
 * Defensive casting and access utilities.
 * <p>
 * Prevents {@link ClassCastException} and {@link IndexOutOfBoundsException}
 * that frequently occur in game mode code when entities, damagers, or
 * team lists are accessed without sufficient type/bounds checks.
 * <p>
 * <b>Usage:</b>
 * <pre>
 *   // Instead of: Player p = (Player) entity;
 *   SafeCast.player(entity).ifPresent(p -> { ... });
 *
 *   // Instead of: list.get(list.size() - 1)
 *   SafeCast.listGet(list, list.size() - 1).ifPresent(item -> { ... });
 *
 *   // Generic cast:
 *   SafeCast.as(damager.getEntity(), Player.class).ifPresent(killer -> { ... });
 * </pre>
 */
public final class SafeCast {

    private SafeCast() {}

    /**
     * Safely cast an object to the target type.
     *
     * @param obj        the object to cast (may be null)
     * @param targetType the desired class
     * @param <T>        target type
     * @return Optional containing the cast value, or empty if null or wrong type
     */
    public static <T> Optional<T> as(Object obj, Class<T> targetType) {
        if (obj == null || !targetType.isInstance(obj)) {
            return Optional.empty();
        }
        return Optional.of(targetType.cast(obj));
    }

    /**
     * Safely cast an object to {@link org.bukkit.entity.Player}.
     *
     * @param obj the object to cast (typically an Entity or Damager)
     * @return Optional containing the Player, or empty
     */
    public static Optional<org.bukkit.entity.Player> player(Object obj) {
        return as(obj, org.bukkit.entity.Player.class);
    }

    /**
     * Safely cast an object to {@link org.bukkit.entity.LivingEntity}.
     *
     * @param obj the object to cast
     * @return Optional containing the LivingEntity, or empty
     */
    public static Optional<org.bukkit.entity.LivingEntity> living(Object obj) {
        return as(obj, org.bukkit.entity.LivingEntity.class);
    }

    /**
     * Safely access a list element by index.
     *
     * @param list  the list (may be null or empty)
     * @param index the index to access
     * @param <T>   element type
     * @return Optional containing the element, or empty if list is null or index out of bounds
     */
    public static <T> Optional<T> listGet(List<T> list, int index) {
        if (list == null || index < 0 || index >= list.size()) {
            return Optional.empty();
        }
        return Optional.ofNullable(list.get(index));
    }

    /**
     * Safely get the first element of a list.
     *
     * @param list the list (may be null or empty)
     * @param <T>  element type
     * @return Optional containing the first element, or empty
     */
    public static <T> Optional<T> first(List<T> list) {
        return listGet(list, 0);
    }

    /**
     * Safely get the last element of a list.
     *
     * @param list the list (may be null or empty)
     * @param <T>  element type
     * @return Optional containing the last element, or empty
     */
    public static <T> Optional<T> last(List<T> list) {
        if (list == null || list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(list.get(list.size() - 1));
    }

    /**
     * Returns a non-null fallback if the input is null.
     *
     * @param value    the value to check
     * @param fallback the fallback to return if value is null
     * @param <T>      element type
     * @return value if non-null, otherwise fallback
     */
    public static <T> T orElse(T value, T fallback) {
        return value != null ? value : fallback;
    }
}
