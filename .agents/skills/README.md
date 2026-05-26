# HouziCore Project Skills

Project-local skills live under:

- `.agents/skills/`

Antigravity should inspect this directory when the task clearly matches a specialized workflow.

## Current Skills

### 🏗️ Feature Development
- `add-arcade-game`
  - use when creating a new Arcade minigame
- `add-lobby-module`
  - use when adding a new Lobby module or feature surface
- `create-shop-gui`
  - use when building or extending shop/menu GUI flows

### 🔧 Build & Operations
- `houzicore-build`
  - use when the task is primarily about build, package, or deploy verification
- `houzicore-task-guardrails`
  - use when scoping, verifying, or editing task contracts and guardrail files
- `houzicore-ui-runtime`
  - use when touching actionbar, scoreboard, waiting lobby UI, or conflicting HUD ownership
- `minecraft-worldedit-ops`
  - use when performing safe WorldEdit operations, schematic loading, or spawn layout changes

### 📚 API Knowledge & Reference
- `minecraft-paper-plugin-dev`
  - use when developing, reviewing, or debugging Paper/Spigot/Bukkit plugins; covers Gradle setup, plugin.yml, version migration, NMS access rules
- `paper-26-1-reference` ← **PRIMARY API REFERENCE**
  - Comprehensive technical reference for Paper 26.1.2 (Tiny Takeover)
  - **14 reference guides, ~438 KB, ~100 code examples**
  - Covers: all APIs (Kyori Adventure, Schedulers, PDC, Commands, Scoreboards, Inventories, Entities, Display Entities, Particles, Potions, Enchantments, Recipes, Structures), Dialog UI, all mobs (42 baby + 52 monsters), items, and creative design patterns
  - **Reading order:** Tiered knowledge system (Tier 0–5) with decision flowchart built into SKILL.md
- `minecraft-commands-scripting`
  - use when working with command blocks, NBT path scripting, `/execute` chains, or scoreboards for testing

### ⚡ Development & Workflow Best Practices
- `java-performance-tuning`
  - use when profiling memory leaks, optimizing tight ticking loops, and designing low-latency Java operations
- `git-workflow-pro`
  - use when branching, structuring commits, running pre-commit checks, and merging conflicts
- `minecraft-testing`
  - use when designing or implementing unit/mock tests (using JUnit 5, MockBukkit) or writing CI test workflows

## Skill Priority & Layering

When multiple skills apply, read them in this order:

1. **paper-26-1-reference** — for "what API exists and how to use it" (knowledge layer)
2. **minecraft-paper-plugin-dev** — for "how to set up a project, migrate versions, access NMS" (infrastructure layer)
3. **Feature-specific skill** (`add-arcade-game`, `add-lobby-module`, `create-shop-gui`) — for "how to do it in HouziCore's codebase" (implementation layer)
4. **Operations skill** (`houzicore-build`, `houzicore-task-guardrails`, `houzicore-ui-runtime`) — for "how to verify and deploy" (quality layer)

Example: Creating a new Arcade minigame that uses Display Entities and Particles:
1. Read `paper-26-1-reference` → Tier 2A (entity-attributes-display) + Tier 3A (particles)
2. Read `add-arcade-game` → HouziCore game structure, GameType registration, ParseData
3. Read `houzicore-build` → compile, deploy, restart

## Imported Skill Policy

Some project-local skills are HouziCore adaptations of ideas curated from external skill packs.

Use the local skill files here as the source of truth.
Do not switch to foreign slash commands, prompt chains, or artifact naming just because an imported idea came from elsewhere.

## Skill Rule

If a task clearly matches one of these skills, Antigravity should open that skill before coding.

If multiple skills match, read the narrowest relevant skills first, then continue with the task contract.
