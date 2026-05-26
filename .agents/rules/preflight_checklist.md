---
description: Mandatory pre-flight checklist — ระบบกันลืมที่ agent ต้องใช้ทุกครั้งก่อนเริ่มงาน
---
# Mandatory Pre-Flight Checklist

> **ทุกครั้งที่ได้รับงาน agent ต้องสร้าง task file จาก `tasks/TEMPLATE.md` เป็น `tasks/T-xxx.md` และเติม checklist ตาม 4 เฟสนี้ก่อนเขียนโค้ดแม้แต่บรรทัดเดียว**

---

## Phase 0: RESEARCH (อ่าน docs ก่อน!)

- [ ] สร้าง `tasks/T-xxx.md` จาก `tasks/TEMPLATE.md`
- [ ] ถ้า worktree เดิมมีไฟล์นอกงานเยอะ ให้บันทึกไว้ใน `Ignore Existing Worktree Paths`
- [ ] รัน `.agents/scripts/preflight.ps1 -TaskFile tasks/T-xxx.md`
- [ ] อ่าน `/project_reference` — ทำความเข้าใจ architecture + init chain
- [ ] อ่าน reference ที่เกี่ยวข้อง:
  - Lobby feature → `.agents/references/lobby_architecture.md`
  - Arcade feature → `.agents/references/arcade_architecture.md`
  - ระบบที่มีอยู่ → `.agents/references/systems_catalog.md`
  - Event patterns → `.agents/references/event_system.md`
- [ ] อ่าน rules ที่เกี่ยวข้อง:
  - API wrappers → `.agents/rules/core_api_rules.md`
  - UI standards → `.agents/rules/ui_design_rules.md`
  - Bilingual → `.agents/rules/localization_rules.md`
- [ ] อ่าน API Reference สะสม (Paper 26.1):
  - [paper-26-1-reference](file:///e:/Houzicore/.agents/skills/paper-26-1-reference/SKILL.md) — อ่านตามลำดับขั้นความรู้ (Tiers 0–5) อิงตามประเภทของงานที่ได้รับมอบหมาย

---

## Phase 1: BEFORE CODING (วิเคราะห์ก่อนโค้ด)

### 1.1 Feature ซ้ำไหม?
- [ ] อ่าน `.agents/references/plugin_features.md` — feature นี้มีอยู่แล้วหรือยัง?
- [ ] ถ้ามีแล้ว → ใช้หรือ extend ไม่ใช่เขียนใหม่

### 1.2 ใช้ API ถูกตัวไหม?
- [ ] อ่าน `.agents/rules/core_api_rules.md` — ห้ามใช้ raw Bukkit ถ้ามี wrapper
- [ ] particle → `UtilParticle` / inventory → `ShopBase` / damage → `CombatManager`
- [ ] เช็ค [paper-26-1-reference](file:///e:/Houzicore/.agents/skills/paper-26-1-reference/SKILL.md) — หากจำเป็นต้องเขียน Command แบบ Brigadier, ใช้ Persistent Data Container (PDC), สร้าง Dialog UI ฟอร์มใหม่ หรือใช้ Display Entities ในการตกแต่ง ให้ใช้ API รูปแบบใหม่ของ 26.1 แทนรูปแบบ legacy เสมอ

### 1.3 Ripple Analysis (ผลกระทบ)
- [ ] List ไฟล์ทั้งหมดที่จะถูกกระทบ
- [ ] เปลี่ยน method signature? → find callers ทั้งหมด
- [ ] เปลี่ยน Shared? → ทั้ง Lobby + Arcade ได้ผลกระทบ!
- [ ] เปลี่ยน Event class? → find ALL `@EventHandler` listeners
- [ ] Rename class? → find ALL `import`, `instanceof`, reflection calls
- [ ] ใช้ Reflection? (เช่น `GameCreationManager.CreateGame()`) → verify constructor signature ด้วยตา ไม่ใช่แค่ compile
- [ ] **ถ้าไม่แน่ใจผลกระทบ → ถามผู้ใช้ก่อน** (ห้ามเดา infrastructure logic)

### 1.4 ต้องเพิ่มอะไรบ้าง?
- [ ] DB table/column? → เช็ค `E:\Houzicore\houzicore_schema.sql`
- [ ] Language keys? → เลือก Tier (อ่าน `.agents/rules/localization_rules.md`)
  - New/reworked minigame → source-backed `Code\Shared\src\main\resources\messages\en\<game>.yml` + `messages\th\<game>.yml`
  - Hub/static or shared systems → YAML keys ใน `messages_en.yml` + `messages_th.yml` หรือ catalog ที่เหมาะสม
  - inline `isThai` ternary ใช้ได้เฉพาะ dynamic glue, debug, admin, หรือ fallback แคบๆ ไม่ใช่ source of truth ของ minigame UX
- [ ] ถ้าเป็น new/reworked minigame → define wording bundle ตั้งแต่ก่อนเขียนโค้ด:
  - kit/perk names + summaries
  - item display names + lore
  - scoreboard labels
  - bossbar / objective / countdown / runtime notices
- [ ] ถ้าเป็น new/reworked minigame → ระบุ verification plan ตั้งแต่ต้น:
  - build proof
  - EN smoke
  - TH smoke
  - อย่างน้อยหนึ่ง flow ต่อ role หลัก
- [ ] Register Manager? → `Code\Lobby\src\...\hub\Hub.java` หรือ `Code\Arcade\src\...\Arcade.java`
- [ ] External dependency? → ต้อง shade ด้วย! (ดู `build_deploy.md`)

---

## Phase 2: DURING CODING (เช็คทุกไฟล์ที่แก้)

### 2.1 Compilation Safety
- [ ] `import` ครบทุกตัวที่ใช้?
- [ ] Command → ลง `addCommands()` ใน Manager + import class แล้ว?
- [ ] Manager → registered ใน `Hub.java` (Lobby) หรือ `Arcade.java` (Arcade)?
- [ ] ไม่มี method ที่เรียกแล้วไม่มีอยู่จริง? (verify source ก่อน)

### 2.2 Bilingual
- [ ] ทุก user-facing text มี EN/TH?
- [ ] New/reworked minigame ใช้ source-backed EN/TH catalogs เป็นหลัก ไม่ใช่ inline literals กระจาย
- [ ] inline `isThai` ternary ใช้เฉพาะ dynamic glue ที่แคบและไม่กลายเป็น source of truth ของ mode
- [ ] Hub/shared/static surfaces ใช้ `LangManager.get().get(player, "key")` หรือ catalog ที่เป็นทางการ

### 2.3 GUI / Shop
- [ ] ใช้ `ShopBase` + `ShopPageBase` ไม่ใช่ raw inventory?
- [ ] Blue glass border fill ทุก empty slot?
- [ ] `addButton()` lambda ไม่ใช่ `InventoryClickEvent`?
- [ ] 1-tick delay ระหว่าง close → open inventory?

### 2.4 UX Quality
- [ ] Particles: `UtilParticle.PlayParticle()` ไม่ใช่ raw `spawnParticle()`
- [ ] Sounds: ≤3 layers, delay 1-3 tick, pitch guide
- [ ] Lore: 45 chars max per line, poetic style, `§8───` separator
- [ ] BossBar: cancel old bar before creating new one

### 2.5 Resource Cleanup (ป้องกัน memory leak!)
ทุกครั้งที่สร้าง per-player resource ต้องมี cleanup ตอน `PlayerQuitEvent`:
- [ ] FastBoard → `board.delete()` ตอน quit
- [ ] BossBar → `bar.removePlayer()` ตอน quit
- [ ] HashMap/Map ที่ key เป็น Player/UUID → `remove()` ตอน quit
- [ ] Scheduler tasks ที่ผูกกับ player → cancel ตอน quit
- [ ] Disguises → `undisguise()` ตอน quit
- [ ] Custom entities (NPC, Hologram) ที่สร้างให้ player → remove ตอน quit

### 2.6 Event Patterns
- [ ] Timed logic ใช้ `@EventHandler UpdateEvent` ไม่ใช่ `BukkitRunnable`
- [ ] Damage ใช้ `CustomDamageEvent` ไม่ใช่ `EntityDamageEvent`
- [ ] อ่าน `.agents/references/event_system.md` สำหรับ UpdateType ที่เหมาะสม
  - BossBar = `FASTER` (500ms)
  - Scoreboard = `FAST` (1s)
  - Heavy logic = `SLOW` (10s) หรือ `MIN_01` (1m)

---

## Phase 3: BEFORE BUILD & DEPLOY (ก่อนคอมไพล์และ deploy)

### 3.1 Changelog
- [ ] อัพเดท `Code\Shared\src\...\core\chat\command\UpdateVCommand.java`
  - `LATEST_VERSION_DATE` = วันนี้
  - `LATEST_UPDATE_LOG` = สรุปภาษาไทยสั้นๆ ว่าเปลี่ยนอะไร

### 3.2 Build Chain (ลำดับสำคัญ!)
- [ ] Build Shared FIRST: `mvn clean install` (ไม่ใช่ package!)
- [ ] Build Lobby/Arcade: `mvn clean package`
- [ ] ถ้ามี external lib → เช็ค `maven-shade-plugin` config:
  - `<artifactSet><includes>` ระบุเฉพาะ lib ใหม่
  - `<relocations>` ย้าย package namespace

### 3.3 Deploy (Granular!)
- [ ] แก้ Shared → deploy ไปทั้ง `servers\Lobby\plugins\` AND `servers\Arcade1\plugins\`
- [ ] แก้ Lobby เท่านั้น → deploy ไป `servers\Lobby\plugins\` เท่านั้น
- [ ] แก้ Arcade เท่านั้น → deploy ไป `servers\Arcade1\plugins\` เท่านั้น
- [ ] ใช้ `build_plugins.bat` สำหรับ full deploy

### 3.4 Code Quality
- [ ] ไม่มี `// TODO` ค้างที่ไม่ได้ทำ
- [ ] ไม่มี unused import
- [ ] ไม่มี orphaned files

### 3.5 Verify
- [ ] รัน Unit Tests ใน Shared และ Arcade: `mvn test` (เช็คให้ผ่าน 100% เพื่อตรวจสอบความถูกต้องของ Localization Guardrails และโค้ดส่วนอื่นๆ)
- [ ] Compile สำเร็จ (BUILD SUCCESS)
- [ ] Restart server → ทดสอบ in-game
- [ ] เช็ค console: `type logs\latest.log | findstr /i "error exception"`
- [ ] รัน `.agents/scripts/postflight.ps1 -TaskFile tasks/T-xxx.md`

### 3.6 Knowledge Handoff
- [ ] ถ้างานสร้าง pattern ใหม่หรือปิด wave สำคัญ → อัปเดต `docs/structural_update_*.md`
- [ ] ถ้าเป็น new/reworked minigame → บันทึก source of truth ของ runtime owner + content roster ให้ชัด
- [ ] อัปเดต task ให้แยก `implementation shipped` ออกจาก `live verification pending`
- [ ] อย่าปล่อย task ค้างในสภาพที่อ่านแล้วแยกไม่ออกว่า "โค้ดยังไม่เสร็จ" หรือ "เสร็จแล้วแต่ยังไม่ได้ smoke test"
