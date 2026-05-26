---
description: UI Design rules for Scoreboard, Tablist, Title, BossBar across Arcade and Lobby
---
# UI Design Rules

## Mode-Specific Override

- For `Prop Rush` / `HideSeek`, read `.agents/rules/prop_rush_ui_rules.md` before changing scoreboard, bossbar, title usage, or lobby presentation.
- When Prop Rush-specific rules conflict with generic UI habits, the Prop Rush rules win.

## Scoreboard UI Protocol (Hypixel Style)
- **Terminology:**
  - **Scoreboard Object** (Section Headers): Must ALWAYS use Small Caps (`UtilText.toSmallCaps()`). E.g., `sᴛᴀᴛᴜs`, `ʟᴏᴀᴅᴏᴜᴛ`, `ʏᴏᴜʀ sᴛᴀᴛs`.
  - **Scoreboard Subject** (Data Lines): Standard normal font. E.g., `🌏 Map: Floating Isles`, `Kills: 0`.
- **Optimization & Diffing:** Scoreboard updates MUST be cached. Only lines that have explicitly changed should be sent to the player (Diffing) to prevent screen flickering and minimize packet overhead.
- **Title Animation:** Scoreboard title MUST use the "Mineplex 2018 Bold Highlight Sweep" animation: Base Gold (`§6§l`), Center highlight White (`§f§l`), Edge fade Yellow (`§e§l`).
- **Layout Requirements:** 
  - Title: Must be the Game Name (Always Uppercase).
  - In-Game (Live): Must show `Kills: 0` immediately upon start.
  - Spacing: Use blank lines to group Subjects under their respective Objects.
- **Body labels:** Bilingual (via `isThai`) with emoji prefixes (🗺, 📡, 👥, ⚔️, 🔹)
- **Data colors:** Labels = `§f`, Values = `§a`. Essence = `§b`/`§a`
- **Footer:** `§e§lwww.houzicore.com` or `§8§m──────` separator + `§8{date} • {server}`
- **Packet Bug Fix:** Apply Bukkit scoreboard (for nametags) → *then* assign packet-based ScoreboardSidebar. Reversing this causes flicker!

## Tablist Header/Footer
- **Header:** Single `§8§m──────` → `§6§l✦  §f§lʜᴏᴜᴢɪᴄᴏʀᴇ  §6§l✦` → bilingual tagline
- **Footer:** Game/Map info → `play.houzicore.com` → single `§8§m──────`
- **No double separators** — 1 on top, 1 on bottom
- **Arcade:** Use `TablistFix.updateTablist(player, clientManager, nameColor, suffixComp, gameName, mapName)`
- **Lobby:** Use `TabHeaderManager` with `player.setPlayerListName("<player_head> {RankTag} §f{Name}")`

## Title & Subtitle (ข้อความกลางจอ)
- **API:** `UtilTextMiddle.display(title, subtitle, fadeIn, stay, fadeOut, player)`
- **กฎเหล็ก:** Title = ช่วงเวลาที่ต้องการให้ผู้เล่นตื่นเต้น แต่ห้ามซ้ำเหตุการณ์เดิม (เช่น Refill ครั้งที่ 2 ไม่ต้อง Title)
- **ข้อจำกัดความยาว:** Title ≤ 20 ตัวอักษร, Subtitle ≤ 35 ตัวอักษร
- **ห้ามใช้ Title แทน Actionbar** สำหรับสถานะชั่วคราว (เช่น Cooldown, Mana)

### Tier 1: บังคับมี (ทุกมินิเกม)

| เหตุการณ์ | Title | Subtitle | เสียง |
|----------|-------|----------|------|
| นับถอยหลัง | `§e§l5` → `§6§l3` → `§c§l2` → `§4§l1` | (ว่าง) | `UI_BUTTON_CLICK` pitch 1.0 |
| เริ่มเกม | `§a§lเริ่มเกม!` | `§7กติกาแบบสั้น 1 บรรทัด` | `ENTITY_ENDER_DRAGON_GROWL` pitch 1.0 |
| จบเกม | `§c§lGAME OVER` | `§e{ผู้ชนะ} §7เป็นผู้ชนะ!` | `ENTITY_PLAYER_LEVELUP` pitch 1.2 |

### Tier 2: ตามสถานการณ์ (มีได้ถ้าเกิดขึ้นจริง)

| เหตุการณ์ | Title | Subtitle | เสียง | กฎพิเศษ |
|----------|-------|----------|------|--------|
| First Blood | `§c§lFirst Blood!` | `§e{ผู้ฆ่า} §7→ §c{ผู้ตาย}` | `ENTITY_WITHER_SPAWN` pitch 1.5 | แสดงแค่ครั้งเดียวต่อเกม |
| Chest Refill | `§e§l📦 CHEST REFILL` | `§7หีบทั้งหมดถูกเติมใหม่!` | `BLOCK_CHEST_OPEN` pitch 1.2 | Title เฉพาะครั้งแรก ครั้งถัดไปใช้ Chat เท่านั้น |
| Map Event | `§c§l☠ MAP CRUMBLE ☠` | `§7เกาะกำลังถล่ม!` | `ENTITY_ENDER_DRAGON_GROWL` pitch 1.0 | Title ครั้งแรกเท่านั้น BossBar เปลี่ยนสีรับช่วงต่อ |
| Milestone | `§e§l5 ᴘʟᴀʏᴇʀs ʟᴇғᴛ` | (ว่าง) | `BLOCK_NOTE_BLOCK_PLING` pitch 1.5 | แสดงตอนเหลือ 5, 3, 1 คน เท่านั้น |
| Kill Streak | `§6§lTriple Kill!` | (ว่าง) | `ENTITY_PLAYER_LEVELUP` pitch 1.5 | แสดงเฉพาะคนฆ่า ไม่ broadcast |

## Actionbar (ข้อความบนหลอดเลือด)
- **API:** `UtilTextBottom.display(text, player)`
- **กฎเหล็ก:** Actionbar = ข้อมูลส่วนตัวชั่วคราว ห้ามส่งให้ทุกคน ห้ามใช้เป็นข้อความถาวร
- **กรณีที่ใช้ได้:**

| สถานการณ์ | ตัวอย่าง |
|----------|---------|
| สกิล Cooldown | `§cคูลดาวน์: §e3.5 วินาที` |
| การพรางตัว | `§aกำลังพรางตัวเป็น §eกล่องไม้โอ๊ค` |
| พลังพิเศษ | `§bSprint Boost §7- §a3 วินาที` |
| ห้ามทำ | `§cมานาไม่เพียงพอ!` |
| คำเตือนแมพ | `§c⚠ คำเตือน: ทรายแดงไม่เสถียร!` |

- **ห้าม:** ใช้แสดงข้อมูลที่ควรอยู่ใน Scoreboard (เช่น Kills, เวลาเหลือ)

## BossBar (แถบด้านบนจอ)
- **API:** `UtilTextTop.displayProgress(text, progress, players)`
- **กฎเหล็ก:** MUST cancel old BossBar ก่อนเสก BossBar ใหม่ทุกครั้ง (ไม่งั้นจะกะพริบ)
- **ช่วง Recruit/Prepare:** ไม่ใช้ BossBar (ปล่อยว่าง)
- **ช่วง Live:** ใช้เป็น "นาฬิกาจับเวลา" เท่านั้น, อัปเดตทุก `UpdateType.FASTER` (500ms)
- **ช่วง End:** ลบออกทันที
- **สีตามสถานะ:**

| สถานะ | สี BossBar | ตัวอย่าง |
|-------|-----------|---------|
| ปกติ | `YELLOW` | เวลาที่เหลือปกติ |
| เร่งด่วน (<60s) | `RED` | ข้อความเปลี่ยนจาก `§e` เป็น `§c` |
| ปลอดภัย | `GREEN` | ยึดจุดสำเร็จ |
| เตือนภัย | `PURPLE` | แผนที่กำลังถล่ม |

## Announce / Chat (ข้อความในแชท)
- **กฎเหล็ก:** ข้อความจากระบบต้องมี Prefix เสมอ → ใช้ `F.main("ModuleName", "Message")`
- **ห้ามประกาศเรื่องเล็กน้อย** ในแชท (เช่น "ผู้เล่นเก็บไอเทมได้") → ใช้ Sound + Actionbar แทน

### ตารางข้อความในแชท (บังคับ)

| เหตุการณ์ | รูปแบบ | ทำพร้อมกัน |
|----------|-------|-----------|
| ผู้เล่นตาย (ถูกฆ่า) | `F.main("Game", "§e{ชื่อผู้ตาย} §7ถูกกำจัดโดย §c{ชื่อผู้ฆ่า}")` | + เสียง ไม่ต้อง Title |
| ผู้เล่นตาย (ตกเวหา) | `F.main("Game", "§e{ชื่อ} §7ร่วงลงเวหา")` | + เสียง ไม่ต้อง Title |
| First Blood | `F.main("Game", "§c§l⚔ First Blood! §e{ชื่อผู้ฆ่า} §7→ §c{ชื่อผู้ตาย}")` | + Title Tier 2 |
| Chest Refill | `F.main("Game", "§e§l📦 หีบทั้งหมดถูกเติมใหม่! §7({ครั้งที่}/{สูงสุด})")` | + Title (ครั้งแรก) หรือ Chat อย่างเดียว (ครั้งถัดไป) |
| Map Crumble เริ่ม | `F.main("Game", "§c§l⚠ แผนที่กำลังถล่ม!")` | + Title (ครั้งแรก) + BossBar เปลี่ยนสี |
| Map Crumble กำลังถล่ม | ไม่ต้อง Chat ซ้ำ | BossBar สี PURPLE + เสียง ambient |
| Milestone (เหลือ N คน) | `F.main("Game", "§eเหลือผู้เล่น §c§l{N} §eคน!")` | + Title Tier 2 |
| ประกาศผู้ชนะ | เส้นคั่น `§8─────` + `F.main("Arcade", "§e{ชื่อ} §7เป็นผู้ชนะ!")` + เส้นคั่น | + Title + เสียง `UI_TOAST_CHALLENGE_COMPLETE` |

### ข้อความที่ห้ามประกาศใน Chat (ใช้ช่องทางอื่นแทน)

| เรื่อง | ทำไมถึงห้าม | ใช้อะไรแทน |
|-------|------------|-----------|
| เก็บไอเทม | สแปมเยอะเกิน | ไม่ต้องแจ้งเลย |
| สกิล Cooldown | เรื่องส่วนตัว | Actionbar |
| เปลี่ยน Kit | ไม่สำคัญ | เสียงเท่านั้น |
| เตือนแมพเฉพาะ (เช่น ทราย) | ไม่ใช่ Global event | Actionbar ส่วนตัว |

## Text Formatting (การจัดรูปแบบข้อความ)
- **System Announce:** `F.main("Module", "Message")` → Auto Gradient prefix + Small Caps module name
- **Small Caps:** `UtilText.toSmallCaps("TEXT")` → `ᴛᴇxᴛ` — บังคับใช้กับ Scoreboard Object เท่านั้น
- **Hex Color:** `<#FF6B35>ข้อความ</#>` (ผ่าน `HouziColorParser.parse()`)
- **Gradient:** `<GRADIENT:#ff0000,#0000ff>ข้อความ</GRADIENT>`
- **Rainbow:** `<rainbow>ข้อความ</rainbow>`
- **Separator Line:** `§8§m──────────────────────────────────` (33 chars)

## GUI Inventory Standards (Hypixel Style)
- **Auto-Refreshing Menus:** Complex dynamic GUI menus (such as player stats, live minigame selectors, or active quests) should implement an auto-refreshing mechanism.
- **Async Loading:** GUI data should be fetched and formatted on a separate background thread to prevent the main thread from lagging when opening menus.
- **Item Lore Design:** 
  - Standard descriptive text uses Gray (`§7`).
  - Important keywords, variables, or highlights use a contrasting vibrant color (e.g., `§e`, `§a`).
  - Empty lines should be utilized to space out lore for readability.
  - **Action Label (CTA):** The absolute bottom line of an interactive item's lore must ALWAYS be a distinct Action Label (e.g., `<yellow>Click to view!`).
