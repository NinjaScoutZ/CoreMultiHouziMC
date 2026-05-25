package com.houzicore.shared.core.pet.ability;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.cosmetic.CosmeticProgression;
import com.houzicore.shared.core.gadget.CosmeticRarity;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry that resolves which {@link PetAbility} abilities a pet provides,
 * based on its {@link EntityType} and {@link CosmeticRarity} tier.
 * <p>
 * Tier-gated abilities unlock only when the pet's rarity meets the threshold
 * (checked via {@link CosmeticRarity#isAtLeast(CosmeticRarity)}).
 */
public final class PetAbilityRegistry {

    // ── Singleton ──────────────────────────────────────────────────
    private static final PetAbilityRegistry INSTANCE = new PetAbilityRegistry();

    public static PetAbilityRegistry getInstance() {
        return INSTANCE;
    }

    // ── Internal storage ───────────────────────────────────────────
    private final Map<EntityType, List<AbilityEntry>> abilityMap = new HashMap<>();

    private PetAbilityRegistry() {
        registerAll();
    }

    // ── Public API ─────────────────────────────────────────────────

    /**
     * Get all abilities that apply to the given pet, considering its rarity tier.
     *
     * @param petEntityType the EntityType of the pet
     * @param petName       the pet name string used for rarity lookup
     * @return unmodifiable list of unlocked abilities; never null
     */
    public List<PetAbility> getAbilities(EntityType petEntityType, String petName) {
        CosmeticRarity rarity = CosmeticProgression.getPetRarity(petName);
        List<AbilityEntry> entries = abilityMap.get(petEntityType);

        if (entries == null) {
            // Fallback: default "Companion" ability for unlisted pets
            return Collections.singletonList(new CompanionAbility());
        }

        List<PetAbility> result = new ArrayList<>();
        for (AbilityEntry entry : entries) {
            if (entry.minRarity == null || rarity.isAtLeast(entry.minRarity)) {
                result.add(entry.ability);
            }
        }

        // If no abilities matched (shouldn't happen), still provide default
        if (result.isEmpty()) {
            result.add(new CompanionAbility());
        }

        return Collections.unmodifiableList(result);
    }

    // ── Registration ───────────────────────────────────────────────

    private void registerAll() {
        // ─── Wolf ──────────────────────────────────────────────
        register(EntityType.WOLF, null, new PetAbility() {
            @Override public String getName() { return "Pack Leader"; }
            @Override public String getNameTh() { return "ผู้นำฝูง"; }
            @Override public List<String> getDescription(Player player) {
                return List.of(
                    C.cGray + "Your wolf's leadership grants",
                    C.cGray + "you " + C.cGreen + "+5%" + C.cGray + " bonus coins."
                );
            }
            @Override public List<String> getDescriptionTh(Player player) {
                return List.of(
                    C.cGray + "ความเป็นผู้นำของหมาป่า",
                    C.cGray + "มอบ " + C.cGreen + "+5%" + C.cGray + " โบนัสเหรียญ"
                );
            }
            @Override public PetAbilityType getType() { return PetAbilityType.PASSIVE; }
        });

        register(EntityType.WOLF, CosmeticRarity.EPIC, new PetAbility() {
            @Override public String getName() { return "Hunter's Instinct"; }
            @Override public String getNameTh() { return "สัญชาตญาณนักล่า"; }
            @Override public List<String> getDescription(Player player) {
                return List.of(
                    C.cGray + "Your wolf's hunting instinct",
                    C.cGray + "grants " + C.cAqua + "+3%" + C.cGray + " bonus XP."
                );
            }
            @Override public List<String> getDescriptionTh(Player player) {
                return List.of(
                    C.cGray + "สัญชาตญาณนักล่าของหมาป่า",
                    C.cGray + "มอบ " + C.cAqua + "+3%" + C.cGray + " โบนัส XP"
                );
            }
            @Override public PetAbilityType getType() { return PetAbilityType.PASSIVE; }
        });

        // ─── Cat ───────────────────────────────────────────────
        register(EntityType.CAT, null, new PetAbility() {
            @Override public String getName() { return "Lucky Paws"; }
            @Override public String getNameTh() { return "อุ้งเท้านำโชค"; }
            @Override public List<String> getDescription(Player player) {
                return List.of(
                    C.cGray + "Your cat's lucky paws grant",
                    C.cGray + "you " + C.cGreen + "+3%" + C.cGray + " bonus coins."
                );
            }
            @Override public List<String> getDescriptionTh(Player player) {
                return List.of(
                    C.cGray + "อุ้งเท้านำโชคของแมว",
                    C.cGray + "มอบ " + C.cGreen + "+3%" + C.cGray + " โบนัสเหรียญ"
                );
            }
            @Override public PetAbilityType getType() { return PetAbilityType.PASSIVE; }
        });

        register(EntityType.CAT, CosmeticRarity.LEGENDARY, new PetAbility() {
            @Override public String getName() { return "Nine Lives"; }
            @Override public String getNameTh() { return "เก้าชีวิต"; }
            @Override public List<String> getDescription(Player player) {
                return List.of(
                    C.cGray + "Your cat protects your fortune!",
                    C.cGray + "Keep " + C.cGold + "50%" + C.cGray + " coins on loss."
                );
            }
            @Override public List<String> getDescriptionTh(Player player) {
                return List.of(
                    C.cGray + "แมวปกป้องทรัพย์สมบัติของคุณ!",
                    C.cGray + "เก็บ " + C.cGold + "50%" + C.cGray + " เหรียญเมื่อแพ้"
                );
            }
            @Override public PetAbilityType getType() { return PetAbilityType.PASSIVE; }
        });

        // ─── Widder (WITHER) ───────────────────────────────────
        register(EntityType.WITHER, null, new PetAbility() {
            @Override public String getName() { return "Dark Aura"; }
            @Override public String getNameTh() { return "ออร่ามืด"; }
            @Override public List<String> getDescription(Player player) {
                return List.of(
                    C.cGray + "The Widder's dark energy",
                    C.cGray + "grants " + C.cGreen + "+10%" + C.cGray + " bonus coins."
                );
            }
            @Override public List<String> getDescriptionTh(Player player) {
                return List.of(
                    C.cGray + "พลังมืดของ Widder",
                    C.cGray + "มอบ " + C.cGreen + "+10%" + C.cGray + " โบนัสเหรียญ"
                );
            }
            @Override public PetAbilityType getType() { return PetAbilityType.PASSIVE; }
        });

        register(EntityType.WITHER, CosmeticRarity.LEGENDARY, new PetAbility() {
            @Override public String getName() { return "Wither's Blessing"; }
            @Override public String getNameTh() { return "พรวิเธอร์"; }
            @Override public List<String> getDescription(Player player) {
                return List.of(
                    C.cGray + "The Widder blesses you with",
                    C.cGray + "an extra " + C.cAqua + "+5%" + C.cGray + " bonus XP."
                );
            }
            @Override public List<String> getDescriptionTh(Player player) {
                return List.of(
                    C.cGray + "Widder อวยพรคุณด้วย",
                    C.cGray + "โบนัส " + C.cAqua + "+5%" + C.cGray + " XP เพิ่มเติม"
                );
            }
            @Override public PetAbilityType getType() { return PetAbilityType.PASSIVE; }
        });

        // ─── Fox ───────────────────────────────────────────────
        register(EntityType.FOX, null, new PetAbility() {
            @Override public String getName() { return "Cunning"; }
            @Override public String getNameTh() { return "เจ้าเล่ห์"; }
            @Override public List<String> getDescription(Player player) {
                return List.of(
                    C.cGray + "Your fox's cunning nature",
                    C.cGray + "grants " + C.cGreen + "+4%" + C.cGray + " bonus coins."
                );
            }
            @Override public List<String> getDescriptionTh(Player player) {
                return List.of(
                    C.cGray + "ความเจ้าเล่ห์ของสุนัขจิ้งจอก",
                    C.cGray + "มอบ " + C.cGreen + "+4%" + C.cGray + " โบนัสเหรียญ"
                );
            }
            @Override public PetAbilityType getType() { return PetAbilityType.PASSIVE; }
        });

        // ─── Parrot ────────────────────────────────────────────
        register(EntityType.PARROT, null, new PetAbility() {
            @Override public String getName() { return "Echo"; }
            @Override public String getNameTh() { return "เสียงสะท้อน"; }
            @Override public List<String> getDescription(Player player) {
                return List.of(
                    C.cGray + "Your parrot echoes wisdom,",
                    C.cGray + "granting " + C.cAqua + "+5%" + C.cGray + " bonus XP."
                );
            }
            @Override public List<String> getDescriptionTh(Player player) {
                return List.of(
                    C.cGray + "นกแก้วสะท้อนปัญญา",
                    C.cGray + "มอบ " + C.cAqua + "+5%" + C.cGray + " โบนัส XP"
                );
            }
            @Override public PetAbilityType getType() { return PetAbilityType.PASSIVE; }
        });

        // ─── Axolotl ──────────────────────────────────────────
        register(EntityType.AXOLOTL, null, new PetAbility() {
            @Override public String getName() { return "Regeneration"; }
            @Override public String getNameTh() { return "การฟื้นฟู"; }
            @Override public List<String> getDescription(Player player) {
                return List.of(
                    C.cGray + "Your axolotl's regenerative power",
                    C.cGray + "grants " + C.cGreen + "+8%" + C.cGray + " bonus coins."
                );
            }
            @Override public List<String> getDescriptionTh(Player player) {
                return List.of(
                    C.cGray + "พลังฟื้นฟูของแอกซ์โลเติล",
                    C.cGray + "มอบ " + C.cGreen + "+8%" + C.cGray + " โบนัสเหรียญ"
                );
            }
            @Override public PetAbilityType getType() { return PetAbilityType.PASSIVE; }
        });

        register(EntityType.AXOLOTL, CosmeticRarity.MYTHIC, new PetAbility() {
            @Override public String getName() { return "Aqua Shield"; }
            @Override public String getNameTh() { return "โล่น้ำ"; }
            @Override public List<String> getDescription(Player player) {
                return List.of(
                    C.cGray + "Your axolotl's aquatic shield",
                    C.cGray + "grants " + C.cAqua + "+3%" + C.cGray + " bonus XP."
                );
            }
            @Override public List<String> getDescriptionTh(Player player) {
                return List.of(
                    C.cGray + "โล่น้ำของแอกซ์โลเติล",
                    C.cGray + "มอบ " + C.cAqua + "+3%" + C.cGray + " โบนัส XP"
                );
            }
            @Override public PetAbilityType getType() { return PetAbilityType.PASSIVE; }
        });
    }

    /**
     * Register an ability for a specific pet type with an optional minimum rarity gate.
     *
     * @param entityType the pet entity type
     * @param minRarity  minimum {@link CosmeticRarity} required, or {@code null} for always active
     * @param ability    the ability to register
     */
    private void register(EntityType entityType, CosmeticRarity minRarity, PetAbility ability) {
        abilityMap.computeIfAbsent(entityType, k -> new ArrayList<>())
                  .add(new AbilityEntry(ability, minRarity));
    }

    // ── Inner types ────────────────────────────────────────────────

    /**
     * Pairs a {@link PetAbility} with its optional rarity gate.
     */
    private static final class AbilityEntry {
        final PetAbility ability;
        final CosmeticRarity minRarity;

        AbilityEntry(PetAbility ability, CosmeticRarity minRarity) {
            this.ability = ability;
            this.minRarity = minRarity;
        }
    }

    /**
     * Default ability for any pet that doesn't have a specific registration.
     * Provides a small +2% coin bonus.
     */
    private static final class CompanionAbility implements PetAbility {
        @Override public String getName() { return "Companion"; }
        @Override public String getNameTh() { return "สหาย"; }

        @Override public List<String> getDescription(Player player) {
            return List.of(
                C.cGray + "Your loyal companion grants",
                C.cGray + "you " + C.cGreen + "+2%" + C.cGray + " bonus coins."
            );
        }

        @Override public List<String> getDescriptionTh(Player player) {
            return List.of(
                C.cGray + "สหายผู้ซื่อสัตย์ของคุณ",
                C.cGray + "มอบ " + C.cGreen + "+2%" + C.cGray + " โบนัสเหรียญ"
            );
        }

        @Override public PetAbilityType getType() { return PetAbilityType.PASSIVE; }
    }
}
