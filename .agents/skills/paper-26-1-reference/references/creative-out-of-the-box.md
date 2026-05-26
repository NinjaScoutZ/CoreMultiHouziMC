# Creative & Out-of-the-Box Minigame Designs (Paper 26.1)

This guide provides high-concept, creative, and outside-the-box design patterns for future minigames, lobby activities, and player interactions in the Paper 26.1 ecosystem. 

---

## 1. Dialog UI: Beyond Simple Menus

Instead of relying on classic inventory Chest GUIs or cluttered chat messages, developers can use the modern Dialog UI API to build immersive, client-rendered interfaces.

### A. Pre-Spawn Welcome & Character Selection (Configuration Phase)
*   **The Concept:** Before a player officially joins the world (during the network configuration phase), display an interactive Dialog listing available server channels, guild invites, or faction selection.
*   **Outside-the-box Mechanic:** **"The Sorting Hat Dialog"**. Prompt players with a text input field asking for a secret word, a number slider selecting their desired magic tier, and a boolean toggle for "Hardcore Mode". The player’s choices are parsed via `DialogResponseView` to immediately spawn them with corresponding custom gear, traits, or in a specific world region.
*   **API Pattern:** Trigger dialog presentation immediately inside connection lifecycle hooks.

### B. Custom Minigame Match Settings Panel
*   **The Concept:** A client-rendered settings wizard for game operators before starting an Arcade match.
*   **Outside-the-box Mechanic:** Instead of using chat commands (e.g., `/settings setSpeed 2.0`), open a `MultiActionType` dialog with:
    1.  `NumberRangeDialogInput` slider to scale player walk speed from `0.5x` to `3.0x`.
    2.  `BooleanDialogInput` toggle to enable or disable skill cooldowns.
    3.  `TextDialogInput` text field to enter the custom match name.
*   **UX Sound Effect:** Play a clean note-block click when options change and a high-pitched trumpet fanfare on form submission.

---

## 2. Permanent Baby Mob Companions (Golden Dandelion & AgeLock)

The Baby Mob Overhaul coupled with the Golden Dandelion's age-locking API enables complex pet, companion, and puzzle mechanics.

### A. "Peter Pan Pets" & Bounding Box Puzzles
*   **The Concept:** A co-op puzzle adventure game where players must guide farm animals through low-ceiling tunnels and narrow shafts.
*   **Outside-the-box Mechanic:** Baby animals have tiny bounding boxes. Adults cannot fit through the narrow gaps. Players must feed their companions Golden Dandelions to freeze them in baby form, allowing them to crawl through tunnels to trigger weight plates on the other side.
*   **Twist:** If a player mismanages their dandelions, the animal resumes aging, grows into an adult, and gets wedged in the tunnel, trapping the team!

### B. Strider Thermoregulation Relays
*   **Concept:** A lava-crossing obstacle course where players must transport baby striders.
*   **Outside-the-box Mechanic:** Striders turn cold and blue in the air. In 26.1, a baby strider inherits warmth from the strider it is currently riding. Players must construct "Strider Towers" (stacking multiple baby striders on top of adults) to cross frozen air chambers. Players use wind charges or fishing rods to transfer babies from one adult to another to keep the chain warm and red.

### C. The Sound-Variant Chorus (Vocal Personalities)
*   **Concept:** A collection minigame based on the new randomized adult sound variants for cows, pigs, sheep, and cats.
*   **Outside-the-box Mechanic:** When baby animals grow up, they are randomly assigned one of three vocal personalities (including the rare nostalgic legacy sound). Players breed animals, listen to their vocal pitches, and attempt to assemble a complete "vocal choir" of all variants in their pastures to unlock special loot chests.

---

## 3. Oxidizable Copper Trumpet Orchestras

Note Blocks placed on copper blocks play trumpet sounds, with timbre shifting based on the copper's oxidation level.

### A. Dynamic Brass Rhythm Games
*   **Concept:** A musical rhythm game where players stand before a wall of Note Blocks on copper.
*   **Outside-the-box Mechanic:** The game demands specific melodies with varying brass tones (Clean, Exposed, Weathered, Oxidized). Players are given a **Golden Axe** (to scrape off oxidation/rust) and **Honeycomb** (to wax and lock states). As the music plays, players must run back and forth, scraping or waxing the copper blocks beneath the note blocks to change their oxidation states on the fly before hitting them to play the target pitches.

### B. Copper Corrosion Alarm Systems
*   **Concept:** A stealth or puzzle adventure map where oxidation represents timer progression.
*   **Outside-the-box Mechanic:** Redstone clocks slowly oxidize copper blocks under Note Blocks. A sentinel mob patrols the area. The sentinel ignores bright, brassy trumpet tones but immediately attacks if it hears the raspy, oxidized trumpet tone. Players must constantly scrape the copper blocks to maintain a high-pitch clear tone to keep the guard pacified.

---

## 4. Prehistoric Snifflet Archeology

The Snifflet (baby sniffer) features a distinct yellow snout, six legs, and digs up ancient seeds.

### A. "Snout Scout" Treasure Hunts
*   **Concept:** A competitive digging arena where players manage a pack of baby Snifflets.
*   **Outside-the-box Mechanic:** The arena floor is made of moss/dirt. Players leash Snifflets to lead them around. Snifflets sniff out hidden items. Players use custom foods (like Pitcher Pods) to boost their Snifflet's dig speed, competing to dig up rare keys to unlock the central treasure vault.

---

## 5. Craftable Name Tag Quests

Crafting Name Tags now requires only 1 Paper and 1 Nugget, enabling cheap, rapid name-tag mechanics.

### A. "Name-Tag Whisperer"
*   **Concept:** A chaotic PvE survival arena.
*   **Outside-the-box Mechanic:** Fast-moving baby zombies are invading. They are invulnerable to physical damage. Players must gather paper and metal nuggets dropping from the ceiling, craft Name Tags at speed, rename them in a zero-cost anvil to match specific "Pacification Words" displayed above the zombies, and tag them. Once named correctly, the zombie immediately becomes passive and acts as a shield for the player.
