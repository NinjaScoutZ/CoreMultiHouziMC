package com.houzicore.arcade;

import java.util.*;

/**
 * Declares the map marker requirements for every minigame in the Arcade.
 * <p>
 * MapParser reads this at parse-time to validate that builders placed
 * all necessary markers before committing. Each requirement carries
 * a human-readable description so the builder knows exactly what to place.
 * <p>
 * <b>Source of truth:</b> These requirements are derived directly from
 * each game's {@code ParseData()} method in the Arcade codebase.
 */
public final class GameMapRequirements {

    private GameMapRequirements() {}

    // ═══════════════════════════════════════════════════════════════
    //  Data types
    // ═══════════════════════════════════════════════════════════════

    /**
     * A single marker requirement.
     *
     * @param color       Wool color or custom loc name (e.g. "RED", "Terminal")
     * @param description Human-readable explanation of what this marker does
     * @param required    true = map will not work without it
     * @param minCount    Minimum number needed (0 = any amount is fine)
     */
    public record MarkerReq(String color, String description, boolean required, int minCount) {
        public static MarkerReq required(String color, String desc) {
            return new MarkerReq(color, desc, true, 1);
        }
        public static MarkerReq required(String color, String desc, int min) {
            return new MarkerReq(color, desc, true, min);
        }
        public static MarkerReq optional(String color, String desc) {
            return new MarkerReq(color, desc, false, 0);
        }
    }

    /**
     * Team spawn requirement.
     *
     * @param color       Wool color (e.g. "RED", "BLUE", "LIGHT_BLUE")
     * @param teamName    Display name (e.g. "Hunters", "Defenders")
     * @param minSpawns   Minimum spawn points needed
     */
    public record TeamReq(String color, String teamName, int minSpawns) {}

    /**
     * Full requirement set for a game type.
     *
     * @param gameDescription Short description of the game mode
     * @param teams           Required team spawns (gold pressure plates)
     * @param dataLocs        Required/optional data locations (iron pressure plates)
     * @param customLocs      Required/optional custom locations (signs on sponge)
     */
    public record GameReqs(
            String gameDescription,
            List<TeamReq> teams,
            List<MarkerReq> dataLocs,
            List<MarkerReq> customLocs
    ) {
        public static GameReqs of(String desc, List<TeamReq> teams,
                                  List<MarkerReq> data, List<MarkerReq> custom) {
            return new GameReqs(desc, teams, data, custom);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Registry
    // ═══════════════════════════════════════════════════════════════

    private static final Map<GameType, GameReqs> REGISTRY = new EnumMap<>(GameType.class);

    static {
        // ── Prop Rush / HideSeek ──
        register(GameType.PropRush, GameReqs.of(
                "Hiders vs Hunters — แปลงร่างเป็นบล็อคหนี",
                List.of(
                        new TeamReq("LIGHT_BLUE", "Hiders (ฝ่ายซ่อน)", 2),
                        new TeamReq("RED", "Hunters (ฝ่ายหา)", 2)
                ),
                List.of(
                        MarkerReq.required("BLACK", "กรงขังฝ่ายหา — ตอนเริ่มเกมจะเสกเป็น OAK_FENCE แล้วลบตอนปล่อยตัว"),
                        MarkerReq.optional("WHITE", "จุด Prop Toss — ตำแหน่งที่ Hider สามารถโยนบล็อคล่อ"),
                        MarkerReq.optional("PINK", "จุดไอเทม Power-Up"),
                        MarkerReq.optional("YELLOW", "จุดเกิด Meow — NPC แมวให้ Hider ปลดล็อค"),
                        MarkerReq.optional("BROWN", "จุดเกิด Item Shop"),
                        MarkerReq.optional("PURPLE", "จุดแฮ็ก Terminal — Hider ใช้แฮ็คเพื่อได้บัฟ"),
                        MarkerReq.optional("LIME", "จุดยึดแท่น Capture Point — โซนที่ Hider ยืนเพื่อจับ")
                ),
                List.of(
                        MarkerReq.optional("Terminal", "จุดแฮ็ก Terminal (ทางเลือกจาก PURPLE)")
                )
        ));

        // ── Castle Siege ──
        register(GameType.CastleSiege, GameReqs.of(
                "Defenders vs Undead — ปกป้องกษัตริย์จากกองทัพซอมบี้",
                List.of(
                        new TeamReq("LIGHT_BLUE", "Defenders (ฝ่ายป้องกัน)", 4),
                        new TeamReq("RED", "Undead (ฝ่ายรุกราน)", 4)
                ),
                List.of(
                        MarkerReq.required("RED", "จุดเกิดฝูง Undead และจุดวาง TNT"),
                        MarkerReq.required("BLACK", "กำแพงเปราะบาง — จุดที่โดน TNT ทำลายได้"),
                        MarkerReq.required("YELLOW", "จุดเกิดกษัตริย์ King — ระบบสุ่มเลือก 1 จุด"),
                        MarkerReq.required("GREEN", "จุดเกิดชาวบ้าน Peasant"),
                        MarkerReq.required("BROWN", "จุดเกิดม้าศึก War Horse"),
                        MarkerReq.required("PINK", "จุดวาง NPC Kit สำหรับฝ่าย Undead (สูงสุด 3 ตัว)")
                ),
                List.of()
        ));

        // ── Skywars ──
        register(GameType.Skywars, GameReqs.of(
                "FFA — เกาะลอยฟ้า PvP แบบ Battle Royale",
                List.of(
                        new TeamReq("YELLOW", "ผู้เล่น (Solo Spawns)", 8)
                ),
                List.of(
                        MarkerReq.required("RED", "จุดเกิด Void Phantom คุมเกาะกลาง"),
                        MarkerReq.required("YELLOW", "หีบสมบัติเกาะกลาง"),
                        MarkerReq.required("BROWN", "หีบสมบัติบนเกาะผู้เล่น"),
                        MarkerReq.optional("LIME", "จุดวาง Raid Bell")
                ),
                List.of(
                        MarkerReq.optional("56", "พื้นที่ marker สำหรับสุ่ม Ore Node แบบจำกัดจำนวน"),
                        MarkerReq.optional("19", "ทรายหรือบล็อคค้ำ — จะถูกลบเป็น AIR ตอนเริ่มเกม"),
                        MarkerReq.optional("30", "หยากไย่ Cobweb — ระบบจะสุ่มลบบางส่วน")
                )
        ));

        // ── SkywarsTeams ──
        register(GameType.SkywarsTeams, REGISTRY.get(GameType.Skywars)); // shares same map

        // ── MineStrike ──
        register(GameType.MineStrike, GameReqs.of(
                "SWAT vs Bombers — ยิงปะทะแบบ CS:GO",
                List.of(
                        new TeamReq("LIGHT_BLUE", "SWAT (ฝ่ายตำรวจ)", 4),
                        new TeamReq("RED", "Bombers (ฝ่ายระเบิด)", 4)
                ),
                List.of(
                        MarkerReq.required("RED", "จุดวางระเบิด Bomb Sites", 2)
                ),
                List.of()
        ));

        // ── Arena ──
        register(GameType.Arena, GameReqs.of(
                "1v1 Tournament — ประลองรอบต่อรอบ",
                List.of(
                        new TeamReq("YELLOW", "ผู้เล่น (Tournament Pool)", 8)
                ),
                List.of(
                        MarkerReq.required("RED", "ลานประลองรอบชิงชนะเลิศ Finals"),
                        MarkerReq.required("ORANGE", "ลานประลองรอบรองชนะเลิศ Semi-finals"),
                        MarkerReq.required("YELLOW", "ลานประลองรอบก่อนรอง Quarter-finals"),
                        MarkerReq.required("GREEN", "ลานประลองรอบแรก First Round")
                ),
                List.of()
        ));

        // ── Search & Destroy ──
        register(GameType.SearchAndDestroy, GameReqs.of(
                "Team — ค้นหาและทำลายระเบิดของฝ่ายตรงข้าม",
                List.of(
                        new TeamReq("LIGHT_BLUE", "ทีม 1", 3),
                        new TeamReq("RED", "ทีม 2", 3)
                ),
                List.of(
                        MarkerReq.required("BLUE", "จุดติดตั้งระเบิดทีมฟ้า"),
                        MarkerReq.required("RED", "จุดติดตั้งระเบิดทีมแดง")
                ),
                List.of()
        ));

        // ── Cards ──
        register(GameType.Cards, GameReqs.of(
                "Cards Against Humanity — เกมไพ่ตลก",
                List.of(
                        new TeamReq("YELLOW", "ผู้เล่น (Spawns)", 4)
                ),
                List.of(
                        MarkerReq.required("RED", "กรอบแสดงการ์ด Frame Set 1"),
                        MarkerReq.required("ORANGE", "กรอบแสดงการ์ด Frame Set 2"),
                        MarkerReq.required("GREEN", "กรอบแสดงการ์ด Frame Set 3"),
                        MarkerReq.required("YELLOW", "จุดเกิดผู้เล่นในห้องโหวตคะแนน"),
                        MarkerReq.required("PINK", "กรอบแสดงคำถามหลัก Question Frame")
                ),
                List.of()
        ));

        // ── Tug of War ──
        register(GameType.Tug, GameReqs.of(
                "Team — แย่งสัตว์ ชักเย่อ",
                List.of(
                        new TeamReq("RED", "ทีมแดง", 3),
                        new TeamReq("LIGHT_BLUE", "ทีมฟ้า", 3)
                ),
                List.of(
                        MarkerReq.required("RED", "จุดเกิดฝูงหมู Pig ของทีมแดง"),
                        MarkerReq.required("PINK", "พลังชีวิตทีมแดง — จะถูกเปลี่ยนเป็นข้าวสาลี"),
                        MarkerReq.required("BLUE", "จุดเกิดฝูงแกะ Sheep ของทีมฟ้า"),
                        MarkerReq.required("LIGHT_BLUE", "พลังชีวิตทีมฟ้า — จะถูกเปลี่ยนเป็นแครอท")
                ),
                List.of()
        ));

        // ── Sneaky Assassins ──
        register(GameType.SneakyAssassins, GameReqs.of(
                "Solo — แทรกซึมและลอบสังหาร",
                List.of(
                        new TeamReq("YELLOW", "ผู้เล่น (Spawns)", 4)
                ),
                List.of(
                        MarkerReq.required("RED", "จุดเกิดไอเทม Power-Up")
                ),
                List.of(
                        MarkerReq.optional("DISGUISE_TYPE", "ประเภท Mob ที่แปลงร่าง — ค่าเริ่มต้น Villager")
                )
        ));

        // ── Hole in the Wall ──
        register(GameType.HoleInTheWall, GameReqs.of(
                "Solo — หลบกำแพง ลอดช่อง",
                List.of(
                        new TeamReq("YELLOW", "ผู้เล่น (Spawns)", 4)
                ),
                List.of(),
                List.of(
                        MarkerReq.required("TNT", "ขอบเขตลานเล่น — ใช้ 2 จุดกำหนดมุมทแยง", 2)
                )
        ));

        // ── Primal Games / Survival Games ──
        register(GameType.SurvivalPrimalGame, GameReqs.of(
                "Solo Battle Royale — เก็บของ ล่า รอดตาย",
                List.of(
                        new TeamReq("YELLOW", "ผู้เล่น (Spawns)", 12)
                ),
                List.of(
                        MarkerReq.optional("WHITE", "จุดตก Supply Drop")
                ),
                List.of(
                        MarkerReq.required("54", "จุดวางหีบสมบัติ/โต๊ะคราฟ/โต๊ะเอนชานต์ — ระบบสุ่ม", 10)
                )
        ));
        register(GameType.SurvivalPrimalGameTeams, REGISTRY.get(GameType.SurvivalPrimalGame));

        // ── Wither Assault ──
        register(GameType.WitherAssault, GameReqs.of(
                "Team — Humans vs Withers",
                List.of(
                        new TeamReq("LIGHT_BLUE", "Humans", 4),
                        new TeamReq("RED", "Withers", 2)
                ),
                List.of(
                        MarkerReq.required("RED", "จุดกำหนดเพดานบิน Y-Limit ของฝั่ง Wither")
                ),
                List.of()
        ));

        // ── Simple team games (team spawns only) ──
        registerTeamOnly(GameType.Horse, "Team — ม้าชาร์จ",
                new TeamReq("RED", "ทีมแดง", 4), new TeamReq("LIGHT_BLUE", "ทีมฟ้า", 4));

        registerTeamOnly(GameType.SnowFight, "Team — สงครามก้อนหิมะ",
                new TeamReq("RED", "ทีมแดง", 4), new TeamReq("LIGHT_BLUE", "ทีมฟ้า", 4));

        registerTeamOnly(GameType.Lobbers, "Team — ขว้างระเบิด TNT",
                new TeamReq("RED", "ทีมแดง", 4), new TeamReq("LIGHT_BLUE", "ทีมฟ้า", 4));

        registerTeamOnly(GameType.SquidShooter, "Team — ยิงหมึก",
                new TeamReq("RED", "ทีมแดง", 2), new TeamReq("LIGHT_BLUE", "ทีมฟ้า", 2));

        registerTeamOnly(GameType.DragonRiders, "Team — ขี่มังกร",
                new TeamReq("RED", "ทีมแดง", 2), new TeamReq("LIGHT_BLUE", "ทีมฟ้า", 2));

        // ── Simple solo games ──
        registerSoloOnly(GameType.Barbarians, "Solo — บาร์บาเรียนส์ สู้แหลก");
        registerSoloOnly(GameType.Evolution, "Solo — วิวัฒนาการ เปลี่ยนร่างเลเวลอัพ");
        registerSoloOnly(GameType.Stacker, "Solo — อุ้มสัตว์โยน");
        registerSoloOnly(GameType.Wizards, "Solo — พ่อมดดวล เวทมนตร์");
        registerSoloOnly(GameType.MineWare, "Solo — มินิเกมสุ่ม (MineWare)");

        // ══════════════════════════════════════════════════════
        //  Bedwars — ปกป้องเตียง ทำลายเตียงศัตรู
        // ══════════════════════════════════════════════════════

        register(GameType.Bedwars, GameReqs.of(
                "Team (2-4 ทีม) — ปกป้องเตียงของทีม ทำลายเตียงศัตรู ทีมสุดท้ายที่รอดชนะ",
                List.of(
                        // แต่ละทีมต้องมี spawn อย่างน้อย 2 จุด — ใช้สีขนแกะตามสีทีม
                        // ระบบรองรับได้สูงสุด 8 ทีม: RED, BLUE, GREEN, YELLOW, CYAN, PINK, ORANGE, GRAY
                        new TeamReq("RED", "ทีมแดง Red", 2),
                        new TeamReq("BLUE", "ทีมฟ้า Blue", 2),
                        new TeamReq("GREEN", "ทีมเขียว Green", 2),
                        new TeamReq("YELLOW", "ทีมเหลือง Yellow", 2)
                ),
                List.of(
                        // ── Data Locs (Iron Pressure Plate + Wool) ──
                        MarkerReq.required("BLACK", "ตำแหน่ง Hologram ขอบเกาะ — แนะนำ 1 ต่อทีม ใกล้สะพาน/ทางเข้าเกาะ " +
                                "(ระบบ findClosest หาตัวที่ใกล้ team average spawn ที่สุด)", 2),

                        MarkerReq.required("GRAY", "ตำแหน่ง Hologram ร้านค้า — แนะนำ 1 ต่อทีม ตรงที่จะวาง NPC Villager " +
                                "(ระบบ findClosest หาตัวที่ใกล้ team average spawn ที่สุด)", 2),

                        MarkerReq.optional("SILVER", "จุดเกิด Capture Point ขอบนอก (Outer) — backward compat " +
                                "ระบบจะแปลงเป็น CUSTOM 'POINT Outer-N GREEN' อัตโนมัติ"),

                        MarkerReq.optional("WHITE", "จุดเกิด Capture Point กลางแผนที่ (Center) — backward compat " +
                                "ระบบจะแปลงเป็น CUSTOM 'POINT Center GOLD' อัตโนมัติ"),

                        MarkerReq.optional("BROWN", "จุดเกิดค้างคาว (Bat) — จุดสุ่มเกิด Bat NPC ตอน Live"),

                        // ── Bed Positions ──
                        // ⚠ ระบบอ่านตำแหน่งเตียงจาก GetDataLocs(TEAM_NAME) เช่น GetDataLocs("RED")
                        // ใช้ Iron Pressure Plate + Wool สีทีม (สีเดียวกับ team spawn แต่เป็น IRON ไม่ใช่ GOLD)
                        // ♦ วางบล็อคเตียง (สีทีม) ไว้ที่จุดนี้ เช่น RED_BED, BLUE_BED
                        MarkerReq.required("RED", "ตำแหน่งเตียงทีมแดง — วาง Red Bed ที่จุดนี้ " +
                                "(Iron Plate + Red Wool) ♦ ระบบจะตรวจ Bed block data ตอนเริ่มเกม"),
                        MarkerReq.required("BLUE", "ตำแหน่งเตียงทีมฟ้า — วาง Blue Bed ที่จุดนี้"),
                        MarkerReq.required("GREEN", "ตำแหน่งเตียงทีมเขียว — วาง Green Bed ที่จุดนี้"),
                        MarkerReq.required("YELLOW", "ตำแหน่งเตียงทีมเหลือง — วาง Yellow Bed ที่จุดนี้")
                ),
                List.of(
                        // ── Custom Locs (Sign on Sponge) ──
                        // ทุกทีมต้องมี data loc ระบุตำแหน่งเตียง
                        // ใช้ชื่อทีม UPPERCASE เป็น Data Loc สี เช่น "RED", "BLUE" (ไม่ใช่ Custom Loc)
                        // — แต่เตียงจริงต้องวางบล็อคเตียงไว้ตำแหน่งนั้น

                        // Generator ต่อทีม
                        MarkerReq.required("GEN RED", "ตำแหน่ง Resource Generator ทีมแดง — " +
                                "ไอเทม Iron/Emerald/Diamond จะตกที่จุดนี้ ♦ ต้องมี 1 ต่อทีม"),
                        MarkerReq.required("GEN BLUE", "ตำแหน่ง Resource Generator ทีมฟ้า"),
                        MarkerReq.required("GEN GREEN", "ตำแหน่ง Resource Generator ทีมเขียว"),
                        MarkerReq.required("GEN YELLOW", "ตำแหน่ง Resource Generator ทีมเหลือง"),

                        // Shop NPCs ต่อทีม — 3 ร้านต่อทีม (BRICK, EMERALD, STAR)
                        MarkerReq.required("SHOP RED BRICK", "ตำแหน่งวาง NPC ร้านค้า Iron — ทีมแดง"),
                        MarkerReq.required("SHOP RED EMERALD", "ตำแหน่งวาง NPC ร้านค้า Emerald — ทีมแดง"),
                        MarkerReq.required("SHOP RED STAR", "ตำแหน่งวาง NPC ร้านค้า Diamond — ทีมแดง"),

                        MarkerReq.required("SHOP BLUE BRICK", "ตำแหน่งวาง NPC ร้านค้า Iron — ทีมฟ้า"),
                        MarkerReq.required("SHOP BLUE EMERALD", "ตำแหน่งวาง NPC ร้านค้า Emerald — ทีมฟ้า"),
                        MarkerReq.required("SHOP BLUE STAR", "ตำแหน่งวาง NPC ร้านค้า Diamond — ทีมฟ้า"),

                        MarkerReq.required("SHOP GREEN BRICK", "ตำแหน่งวาง NPC ร้านค้า Iron — ทีมเขียว"),
                        MarkerReq.required("SHOP GREEN EMERALD", "ตำแหน่งวาง NPC ร้านค้า Emerald — ทีมเขียว"),
                        MarkerReq.required("SHOP GREEN STAR", "ตำแหน่งวาง NPC ร้านค้า Diamond — ทีมเขียว"),

                        MarkerReq.required("SHOP YELLOW BRICK", "ตำแหน่งวาง NPC ร้านค้า Iron — ทีมเหลือง"),
                        MarkerReq.required("SHOP YELLOW EMERALD", "ตำแหน่งวาง NPC ร้านค้า Emerald — ทีมเหลือง"),
                        MarkerReq.required("SHOP YELLOW STAR", "ตำแหน่งวาง NPC ร้านค้า Diamond — ทีมเหลือง"),

                        // Capture Points
                        MarkerReq.optional("POINT Center GOLD", "จุดยึด Beacon กลาง — ให้ Diamond กับทีมที่ครอง " +
                                "(วาง Beacon จริงที่จุดนี้ ระบบจะจัดการ particle/capture zone เอง)"),

                        MarkerReq.optional("POINT Outer-1 GREEN", "จุดยึด Beacon ขอบนอก #1 — ให้ Emerald กับทีมที่ครอง"),
                        MarkerReq.optional("POINT Outer-2 GREEN", "จุดยึด Beacon ขอบนอก #2 — ให้ Emerald กับทีมที่ครอง"),

                        // Island Reset Corners
                        MarkerReq.optional("CUSTOM_ISLAND", "มุมทแยงเกาะ — ใช้คู่กำหนดขอบเกาะสำหรับ Island Reset " +
                                "(ต้อง 2 จุด ถ้าจะใช้ฟีเจอร์ Island Module)")
                )
        ));

        // ══════════════════════════════════════════════════════
        //  Speed Builders — จดจำสิ่งก่อสร้างและสร้างเลียนแบบ
        // ══════════════════════════════════════════════════════

        register(GameType.SpeedBuilders, GameReqs.of(
                "Solo (สูงสุด 8 คน) — ดูต้นแบบ จดจำ สร้างเลียนแบบให้เหมือน Houmi ตัดสินทำลายคนที่แย่ที่สุด",
                List.of(
                        new TeamReq("YELLOW", "ผู้เล่น (Solo Spawns)", 4)
                ),
                List.of(
                        // ── Data Locs (Iron Pressure Plate + Wool) ──
                        MarkerReq.required("RED", "จุดกลางสิ่งก่อสร้างต้นแบบ (Build Middle) — " +
                                "ตำแหน่งเดียวที่ Houmi จะลอยเหนือ และระบบจะวางสิ่งก่อสร้างตัวอย่าง " +
                                "♦ ต้องมีจุดเดียว ♦ พื้นที่ 7×7×7 จากจุดนี้จะถูกใช้"),

                        MarkerReq.required("YELLOW", "จุดศูนย์กลางแปลงก่อสร้างของผู้เล่น — " +
                                "ต้องมีจำนวนเท่ากับหรือมากกว่าจำนวนผู้เล่นสูงสุด (แนะนำ 8) " +
                                "♦ แต่ละจุดต้องมีพื้นที่ว่าง 7×7×7 ไม่ชนกัน " +
                                "♦ ระบบจะทำลายแปลงที่ไม่มีผู้เล่นใช้เป็นระเบิด", 8)
                ),
                List.of(
                        // ── Custom Locs (Sign on Sponge) ──
                        // สิ่งก่อสร้างต้นแบบ — แต่ละ Custom Loc คือ 1 บิลด์
                        // ชื่อบน Sign = ชื่อสิ่งก่อสร้างที่แสดงให้ผู้เล่นเห็น (รองรับ color code &)
                        // ที่จุด Sponge = ศูนย์กลางสิ่งก่อสร้างต้นแบบ ขนาด 7×7×7
                        // ♦ สร้าง Build ไว้ในพื้นที่ 7×7×7 เหนือจุด Sponge (Y+0 ถึง Y+6)
                        // ♦ Mob ที่อยู่ในพื้นที่จะถูกบันทึกเป็นส่วนหนึ่งของ Build
                        // ♦ พื้น (Y-1) ก็จะถูกบันทึกเป็น ground pattern
                        MarkerReq.required("(ชื่อ Build ใดก็ได้)", "สิ่งก่อสร้างต้นแบบ — " +
                                "เขียนชื่อ Build บน Sign วางบน Sponge ในพื้นที่ 7×7×7 " +
                                "ระบบจะ scan ทุก Custom Loc ที่ไม่ใช่ marker สีมาตรฐาน " +
                                "♦ ต้องมีอย่างน้อย 3 Builds เพื่อให้เกมสนุก", 3)
                )
        ));

        // ══════════════════════════════════════════════════════
        //  Super Smash Mobs — Mob PvP ตกแล้วตาย
        // ══════════════════════════════════════════════════════

        register(GameType.SuperSmash, GameReqs.of(
                "Solo FFA (สูงสุด 8 คน) — เลือก Mob Kit ต่อยกันตกจากแผนที่ พลังชีวิตดาเมจสะสม",
                List.of(
                        new TeamReq("YELLOW", "ผู้เล่น (Solo Spawns)", 4)
                ),
                List.of(
                        // ── Data Locs (Iron Pressure Plate + Wool) ──
                        MarkerReq.required("RED", "จุดเกิด Smash Crystal (Power-Up) — " +
                                "ไอเทม Nether Star จะตกที่จุดนี้ ผู้เล่นเก็บแล้วได้ Lightning Strike " +
                                "♦ แนะนำ 3-5 จุด กระจายทั่วแผนที่", 2)
                ),
                List.of()
        ));
        register(GameType.SmashTeams, REGISTRY.get(GameType.SuperSmash)); // shares same map
    }

    // ═══════════════════════════════════════════════════════════════
    //  Public API
    // ═══════════════════════════════════════════════════════════════

    /**
     * Get the map marker requirements for the given game type.
     *
     * @return requirements, or null if the game has no specific requirements
     */
    public static GameReqs getRequirements(GameType type) {
        return REGISTRY.get(type);
    }

    /** Check if the given game type has registered requirements. */
    public static boolean hasRequirements(GameType type) {
        return REGISTRY.containsKey(type);
    }

    /** Get all registered game types. */
    public static Set<GameType> getRegisteredTypes() {
        return Collections.unmodifiableSet(REGISTRY.keySet());
    }

    // ═══════════════════════════════════════════════════════════════
    //  Registration helpers
    // ═══════════════════════════════════════════════════════════════

    private static void register(GameType type, GameReqs reqs) {
        REGISTRY.put(type, reqs);
    }

    private static void registerTeamOnly(GameType type, String desc, TeamReq... teams) {
        REGISTRY.put(type, GameReqs.of(desc, List.of(teams), List.of(), List.of()));
    }

    private static void registerSoloOnly(GameType type, String desc) {
        REGISTRY.put(type, GameReqs.of(desc,
                List.of(new TeamReq("YELLOW", "ผู้เล่น (Solo)", 4)),
                List.of(), List.of()));
    }
}
