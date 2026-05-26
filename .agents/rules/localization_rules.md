---
description: Localization rules — when to use YAML keys vs inline Java ternary for Thai/English
---
# Localization Rules (Thai / English)

## Iron Rule: New Minigames And Minigame Reworks

Every time a new minigame is created, or an existing minigame is reworked in any meaningful player-facing way, that work must ship with bilingual localization from the start.

This is mandatory.

Do not ship player-facing in-game text in one language first with the idea of "adding Thai later" or "adding English later".

Required baseline:

- all player-facing text must exist in both English and Thai
- the text must live in source-backed localization files, not only inline literals
- the minigame must have a dedicated localization surface like:
  - `Code/Shared/src/main/resources/messages/en/<game>.yml`
  - `Code/Shared/src/main/resources/messages/th/<game>.yml`
- runtime code must render from that bilingual source for any new or reworked minigame flow

This rule applies to:

- kit names
- kit descriptions
- perk / ability names
- perk / ability descriptions
- item display names
- item lore
- scoreboard labels
- bossbar text
- objective text
- countdowns
- notices
- onboarding summaries
- menu copy
- other player-facing in-game strings

If the work adds new text and that text is visible to players, it must have both languages in the same delivery wave.

## Required Minigame Delivery Pack

For any new minigame or meaningful minigame rework, localization is not a side task.

The task must define and ship all of these together:

- source-backed EN / TH files for the mode
- kit and perk names
- kit and perk summary text
- item display names and lore
- scoreboard labels
- bossbar and objective text
- countdown and runtime notices
- menu and onboarding copy
- verification notes that distinguish shipped implementation from pending smoke tests

If one of these surfaces is still inline-only, the mode is not done.

## Required Wording Shapes For Minigames

Use these patterns when authoring new mode copy:

- names:
  - short and stable
  - usually 1 to 3 words
- kit summaries:
  - fantasy first
  - then the core action
  - then the payoff or limitation
- item lore:
  - effect
  - trigger or input
  - cooldown, charge, or timing rule
  - limitation, risk, or counterplay when relevant
- scoreboard labels:
  - short nouns or short state labels
  - not full explanation sentences
- bossbar and objective lines:
  - objective-first
  - tell the player what to do now
- runtime notices:
  - short event-first lines
  - readable under combat pressure
- global holograms or world labels:
  - short safe labels only
  - not the primary place for important localized explanation

## Two-Tier Strategy

### Tier 1: YAML-based (Core / Static Systems)
For large static configs (Hub items, settings UI, cosmetics, treasure, party messages):
```java
String text = LangManager.get().get(player, "key");
String text = LangManager.get().get(player, "key", arg0, arg1); // with args
```
Register keys in `messages_en.yml` and `messages_th.yml` (Shared resources).
Thai fallback defaults are in `DefaultLangTh.java`.

### Tier 2: Inline Java (Dynamic Glue Only)
For small dynamic branches that are genuinely temporary, highly contextual, debug-only, admin-only, or not part of a minigame content surface, inline Java may still be used:
```java
boolean isThai = LangManager.get().isThai(player);
player.sendMessage(isThai ? "§cถูกกำจัดแล้ว!" : "§cEliminated!");
```

Important override:

- this inline rule is not the default for new minigames or reworked minigames
- for new or reworked minigame UX, use bilingual source-backed localization files first
- inline ternaries are allowed only for narrow dynamic glue around already-localized content, not as the long-term source of truth for the mode

## Quick Decision Table

| Use YAML | Use Inline |
|---|---|
| Hub hotbar items, menu titles | Tiny temporary dynamic glue only |
| Cosmetic names and lores | Emergency fallback branches only |
| Settings/Preferences page | Debug-only or admin-only text |
| Party/Friend system messages | Extremely short ad-hoc branches |
| Treasure chest descriptions | Narrow runtime substitutions around localized content |
| AFK kick/warning messages | Game countdown/fight titles |

### Minigame Exception Rule

For minigames, especially any new mode or any rework lane:

- do not use the table above as permission to keep kit text, lore, scoreboard labels, bossbar labels, or gameplay notices inline
- the accepted baseline is bilingual source-backed files for the mode
- if a minigame does not yet have that structure, creating it is part of the task, not optional cleanup
- if live smoke tests are not run yet, mark that as verification debt in the task file instead of pretending the copy migration is unfinished

## Text Formatting (`HouziColorParser`)

- **Hex:** `<#RRGGBB>Text</#>` or `&#RRGGBB`
- **Gradients:** `<GRADIENT:#ff0000,#0000ff>Text</GRADIENT>`
- **Rainbow:** `<rainbow>Text</rainbow>`
- **Small Caps:** `UtilText.toSmallCaps("TEXT")` → `Tᴇxᴛ`
- **Module Prefix:** `F.main("Module", "Message")` → auto gradient + small caps

## Next-Gen Standards (Hypixel Style I18n)
For deeply complex narrative systems, Quests, or future RPG modes, the following standards (inspired by Hypixel SkyBlock) should be considered:
- **Strict Separation of Concerns:** Move 100% of text out of Java classes into external `.properties` or structured `.yml` files. Java classes should only pass `TranslationKeys`.
- **MiniMessage Format:** For future core text parsing, transition towards the `Kyori Adventure MiniMessage` format (e.g., `<white>Kills: <green><arg:0>`) which handles gradients, hover events, and click events seamlessly.
- **Dialogue Sets:** Group narrative dialogues logically by ID (e.g., `npcs_hub.farmer.dialogue.hello.1`) to allow NPCs to iteratively play sequences of text.
