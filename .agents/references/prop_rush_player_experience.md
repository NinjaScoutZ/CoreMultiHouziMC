---
description: Preferred Prop Rush player experience profile derived from repeated user feedback and accepted revisions.
---
# Prop Rush Player Experience Reference

This file captures the accepted taste profile behind recent Prop Rush / HideSeek changes.
Read it before redesigning the scoreboard, bossbar, kit messaging, or lobby presentation.

## The Desired Feel

Prop Rush should feel like a readable arcade hunt with strong identity:

- players instantly know the current objective
- the mode looks like its own game, not a recycled default template
- kit text teaches intent, not just mechanics
- pressure systems feel threatening enough to change behavior
- UI stays compact, clean, and useful under motion

When in doubt, favor clarity, role readability, and mode identity over "more information".

## Scoreboard Taste Profile

The accepted scoreboard direction is:

- top: phase and timer
- mid: team counts and current objective
- bottom: compact points race

What should not come back by default:

- filler status lines that do not drive action
- duplicated status already covered by the bossbar
- generic section spam
- color choices that dilute the Prop Rush theme

The accepted visual tone is:

- blue / aqua branding
- restrained but clear icon use
- compact vertical rhythm
- one purpose per section

## BossBar Taste Profile

The accepted bossbar direction is not:

- phase name only
- decorative flavor text
- generic urgency copy

The accepted bossbar direction is:

- an objective director
- phase-aware, but action-led
- short enough to parse while moving
- color matched to real danger level

Examples of the correct mindset:

- "Morph, hide, and lock in before hunters release"
- "Leave the Danger Zone immediately"
- "Stay inside the border and force space from the enemy team"

## Messaging Taste Profile

Centered text is not sacred.
If it looks misaligned, unstable, or too loud, the preferred fallback is clean chat guidance.

Use center text only when:

- the moment is dramatic enough to deserve it
- the message is short
- the alignment is already proven to look good

Use chat summaries when:

- the message is instructional
- the message repeats across phases
- the centered version feels visually broken

## Kit Taste Profile

The user preference is for kits to feel more playable, not just more documented.

Good kit communication explains:

- the role fantasy
- the main ability window
- the intended gameplay loop
- the likely payoff

Weak kit communication only says:

- what item is in the slot
- what raw action it performs

Future edits should keep asking:

- does this kit now feel more distinct to play?
- would a first-time player know the first smart use?

## Pressure And Danger Profile

Prop Rush pressure systems should visibly matter.

Accepted direction:

- danger systems force movement
- borders punish greed
- escalation is felt, not merely announced

Rejected direction:

- pressure is technically present but easy to ignore
- warning exists without consequence
- chaos looks scary but plays soft

## Lobby Presentation Profile

Lobby kit stands should read like intentional exhibit pieces.

Accepted direction:

- enough spacing to avoid collision
- pedestals that frame the NPC instead of swallowing the walkway
- holograms with breathing room

For postgame presentation, if a GUI is weak or unreliable, a physical podium / NPC scene is aligned with the user's taste.

## Implementation Reminder

When touching Prop Rush or HideSeek presentation in the future:

1. Read `.agent/rules/prop_rush_ui_rules.md`.
2. Read this reference.
3. Compare against the currently accepted runtime in:
   - `HideSeek.java`
   - `GameScoreboard.java`
   - `GameLobbyManager.java`
4. Do not regress to a generic template just because the old pattern already exists in the repo.
