---
description: Debugging flowchart — เจอ error แบบนี้ ทำอะไรเป็นขั้นตอน
---
# Debugging Flowchart

> เจอ error → ดูหัวข้อที่ตรง → ทำตาม step

---

## 🔴 Compilation Errors

### `cannot find symbol`
1. **Missing import?** → เพิ่ม import statement ที่หัวไฟล์
2. **Class ไม่มีจริง?** → verify ว่า class อยู่ใน Shared ด้วย `dir /s /b ClassName.java`
3. **Shared ไม่ได้ build?** → run `cmd /c cd /d E:\Houzicore\Code\Shared && mvn clean install -q & ::`
4. **Method ไม่มีจริง?** → เปิดไฟล์ source ของ class นั้นเช็ค — อย่า hallucinate method names

### `package does not exist`
1. Shared build ด้วย `mvn install` หรือยัง? (`package` ไม่พอ — ต้อง `install` ลง local repo)
2. pom.xml ของ Lobby/Arcade มี `<dependency>` ของ Shared ชี้ version ถูกต้องหรือเปล่า?

### `method is already defined`
1. มี `@EventHandler` ซ้ำ 2 ตัวที่รับ event เดียวกัน → ลบตัวที่ซ้ำ
2. Copy-paste แล้วลืมเปลี่ยนชื่อ method

---

## 🔴 Runtime Crashes

### `NoClassDefFoundError` / `ClassNotFoundException`
**สาเหตุ 99%: external lib ไม่ถูก shade เข้า JAR**
1. เปิด pom.xml → เช็คว่ามี `maven-shade-plugin` หรือยัง
2. เช็คว่า `<artifactSet><includes>` มี lib ที่หาไม่เจอ
3. Verify: `cmd /c jar tf target\*.jar | findstr CLASS_NAME & ::`
4. ถ้าไม่เจอ → เพิ่ม shade config (ดู `build_deploy.md` section Dependency Shading Rules)

### `NullPointerException` ตอน startup
**ลำดับ init ผิด:**
1. `ScoreboardManager.getInstance()` → null ตอน startup → always null-check
2. `WorldData` / `Config` → null ก่อน game โหลดเสร็จ → check `GetState()`
3. Manager ที่ depend อีก manager → สร้างตาม order ใน Hub.java

### `ClassCastException`
1. Missing `if (entity instanceof Player)` ก่อน cast
2. `Material.ordinal()` → triggers CraftLegacy → ใช้ `Material.COMPASS` ตรงๆ

### Server hangs (Watchdog)
1. Legacy `ItemStack.getData()` → CraftLegacy loading → ใช้ Material name ตรงๆ
2. Infinite loop ใน `@EventHandler UpdateEvent` → check condition
3. Blocking DB call on main thread → ย้ายไป async

---

## 🟡 Silent Failures (ไม่ crash แต่ไม่ work)

### Plugin โหลดแต่ feature ไม่ทำงาน
1. Manager registered ใน Hub.java / Arcade.java หรือยัง?
2. Command ลง `addCommands()` หรือยัง?
3. `@EventHandler` มี `implements Listener` หรือเปล่า? (MiniPlugin จัดการให้แล้ว)

### JAR ขนาด 1KB (ไฟล์เสีย)
**สาเหตุ: ใช้ wildcard `*.jar` ใน Windows copy**
```batch
# ❌ copy /Y target\houzicore-shared-*.jar → ได้ 1KB corrupted file
# ✅ copy /Y target\houzicore-shared-1.21.11.jar → ใช้ชื่อเต็ม
```

### GUI ไม่เปิด / เปิดแล้วปิดทันที
1. `player.closeInventory()` แล้ว `player.openInventory()` ใน tick เดียวกัน
2. Fix: defer `openInventory()` ด้วย `runTask()` (1-tick delay)

### Scoreboard ไม่แสดง / กะพริบ
1. FastBoard vs Bukkit Scoreboard conflict → ห้ามมี Bukkit Objective บน SIDEBAR ตอนใช้ FastBoard
2. Init order ผิด → สร้าง Bukkit scoreboard ก่อน → apply → แล้วค่อยสร้าง FastBoard
3. FastBoard version เก่า → ใช้ 2.1.5+ เสมอ

### BossBar กะพริบ
1. Scheduled removal overlap → cancel old removal task ก่อน schedule ใหม่

### DB save ไม่ทำงาน
1. Table/column ไม่ตรงกับ Java model → เช็ค `houzicore_schema.sql`
2. SQL reserved keyword ไม่ backtick → `` `rank` ``, `` `order` ``
3. Redis ปิดอยู่ → `getCachedClientAccountId()` return null → use fallback

### Chat ไม่แสดง / ซ้ำ
1. ทั้ง Chat.java AND HouziExtension cancel `AsyncChatEvent` → ให้แค่ตัวเดียว cancel
2. Rule: Chat.java → `setMessage()` only (no cancel) → HouziExtension → format + cancel

---

## 🟢 Diagnostic Commands

### เช็ค console errors
```
cmd /c cd /d E:\Houzicore\server && type logs\latest.log | findstr /i "error exception warn" & ::
```

### เช็คว่า lib shade เข้า JAR แล้ว
```
cmd /c jar tf target\houzicore-arcade-1.21.11.jar | findstr fastboard & ::
```

### เช็ค DB table
```sql
USE account; SHOW TABLES;
SELECT * FROM accounts WHERE name = 'PlayerName';
```

### เช็ค player inventory
```sql
SELECT ai.*, i.name as itemName, ic.name as category 
FROM accountInventory ai 
JOIN items i ON ai.itemId = i.id 
JOIN itemCategories ic ON i.categoryId = ic.id 
WHERE ai.accountId = 1;
```
