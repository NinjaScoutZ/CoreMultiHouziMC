---
description: Durable presentation rules for Prop Rush and HideSeek UI, distilled from accepted user feedback and iterations.
---
# Prop Rush UI Rules

These rules override generic UI habits when the work touches Prop Rush or HideSeek presentation.

## Core Goal

- Optimize for readable decisions first, atmosphere second.
- If a flashy presentation fights comprehension, remove the flourish.
- Treat Prop Rush as a guided arcade hunt, not as a generic scoreboard-heavy minigame.

## Visual Identity

- `PROP RUSH` is an aqua / blue mode, not a gold / orange mode.
- Mode headers, bossbar prefixes, and scoreboard branding should lean aqua first.
- Hunters and props must be readable at a glance through role-specific iconography.
- Prefer real heads / sprites when they truly exist and render cleanly; otherwise use simple honest fallbacks.

## Scoreboard Shape

- The scoreboard is a compact mission board, not a running changelog.
- Keep only the sections that drive player action:
  - phase / timer
  - role counts
  - objective block
  - top points / leaderboard
- Remove filler sections such as `Situation` when they do not change the next player decision.
- Objective lines must be action phrases, not vague status summaries.
- Use component lines only when they add real clarity, such as role heads or prop sprites.

## BossBar Role

- The bossbar must answer: "What should I do right now?"
- Phase naming is secondary to objective direction.
- Good bossbar text tells players the live action, current pressure, and the time context.
- Default color direction:
  - blue / aqua for normal guidance
  - yellow for panic / imminent danger
  - red for chaos / lethal pressure

## Title And Chat Usage

- If centered text alignment is unreliable, fall back to left-aligned chat summaries.
- Use center-screen text only for short, high-impact beats that benefit from a theatrical callout.
- Do not spam center text for routine timers or generic reminders.
- Countdowns, phase transitions, and gameplay guidance should prefer stable readability over spectacle.

## Kit Communication

- Every kit must communicate a gameplay loop, not only an item name.
- Item lore and runtime copy should answer:
  - what the ability does
  - when to use it
  - why it matters
- A player should understand the kit fantasy and the first correct play without opening source code.

## Pressure Systems

- Danger Zone, border pressure, and late-round escalation must feel real.
- If a pressure system is technically active but practically ignorable, it is under-tuned.
- Visibility, urgency, and punishment should all support the same pressure fantasy.

## Lobby And World Presentation

- Kit NPC pedestals need breathing room; overlapping platforms and holograms are a regression.
- Physical world presentation is preferred over weak or broken GUI presentation for showcase moments.
- If an end-of-round screen is not working, a world-anchored podium / NPC scene is a valid preferred direction.

## Avoid

- Decorative sections with no gameplay value.
- Overlong separators that wrap to two lines.
- Generic "AI average" layouts that ignore the mode theme.
- Reverting to old defaults just because they are easier to implement.
