# Kyori Adventure API Reference Guide
## Rich Text, MiniMessage, Titles, BossBars, and Sound API in Paper 26.1

Paper 26.1 natively integrates **Kyori Adventure** (`net.kyori.adventure.*`) as the standard chat and rich text system, fully deprecating the legacy BungeeCord Chat API (e.g., `net.md_5.bungee.api.chat`). 

This manual details the Kyori components, serialization pipelines, audience deliveries, dynamic UIs, and sound integrations for Paper 26.1 developers.

---

## HouziCore Integration Rules (CRITICAL)

> [!IMPORTANT]
> **HouziCore Standard:** Kyori Adventure is natively integrated in Paper 26.1. Keep these integration rules in mind to fit within the HouziCore design system:
> 
> 1. **Color Parsing**: Always prefer using `com.houzicore.shared.common.util.HouziColorParser.parse("string")` instead of raw `MiniMessage` deserializers. `HouziColorParser` supports hex, gradients (`<gradient:red:blue>`), and legacy formatting codes.
> 2. **Ticking/Timed UI Updates**: Never use `BukkitRunnable` or `BukkitScheduler` to repeatedly send action bars, titles, or scoreboard updates. Always listen to `com.houzicore.shared.updater.event.UpdateEvent` inside a `MiniPlugin` lifecycle using `UpdateType` (e.g., `UpdateType.SEC`).
> 3. **Bilingual Localization**: Do not hardcode raw English or Thai text in display messages. Retrieve translated strings from `LangManager` using keys (e.g., `LangManager.get(player, "key")`).
> 4. **Actionbars**: Do not call `Player#sendActionBar` directly. Set values through `com.houzicore.shared.common.actionbar.ActionBarService` to prevent channel collisions.

---

## 1. Core Component & Styling API

### Components
All text in Adventure is represented by the immutable `net.kyori.adventure.text.Component` class. You construct components using factory methods rather than direct instantiations:

```java
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

// 1. Simple Text Component
Component text = Component.text("Hello World", NamedTextColor.GOLD);

// 2. Complex Nested Builders
Component nested = Component.text()
    .content("Main Text ")
    .color(NamedTextColor.YELLOW)
    .append(Component.text("Bold and Red", NamedTextColor.RED, TextDecoration.BOLD))
    .append(Component.text(" back to normal."))
    .build();
```

### Text Formatting & Decorations
*   `TextColor`: Custom RGB colors (`TextColor.color(0x55FF55)`) or pre-defined colors in `NamedTextColor`.
*   `TextDecoration`: Style states (`BOLD`, `ITALIC`, `UNDERLINED`, `STRIKETHROUGH`, `OBFUSCATED`).
*   `Style`: A container grouping fonts, colors, decorations, click events, and hover events.

```java
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

Component interactive = Component.text("Click Me!")
    .color(TextColor.color(0x3498db))
    .decorate(TextDecoration.UNDERLINED)
    .clickEvent(ClickEvent.runCommand("/spawn"))
    .hoverEvent(HoverEvent.showText(Component.text("Teleports you to spawn point")));
```

---

## 2. MiniMessage Serialization Format

**MiniMessage** is a powerful format that parses simple XML-like tag strings into styled Adventure Components. It replaces legancy formatting codes (`§a`, `&a`).

### Common MiniMessage Tags
| Effect / Color | MiniMessage Tag Example |
| :--- | :--- |
| **Colors** | `<red>Text</red>` or `<#ff5555>Hex Color</#ff5555>` |
| **Styles** | `<bold>Bold</bold>`, `<italic>Italic</italic>`, `<underlined>Underline</underlined>` |
| **Gradients** | `<gradient:red:blue>Red to Blue Gradient</gradient>` |
| **Rainbow** | `<rainbow>Rainbow Text</rainbow>` |
| **Clicks** | `<click:run_command:"/help">Click for Help</click>` |
| **Hovers** | `<hover:show_text:"Hover Text">Info</hover>` |
| **Keybinds** | `<key:key.jump>` (Displays the player's bound jump key) |

### Serialization in Java
Use `net.kyori.adventure.text.minimessage.MiniMessage` to deserialize strings:

```java
import net.kyori.adventure.text.minimessage.MiniMessage;

Component parsed = MiniMessage.miniMessage().deserialize(
    "<yellow>Welcome <gradient:light_purple:gold><bold>" + player.getName() + "</bold></gradient> to our server!"
);
```

---

## 3. Audience Delivery Systems

In Adventure, any entity or target that can receive text, titles, sounds, or bossbars implements the `net.kyori.adventure.audience.Audience` interface. This includes `Player`, `ConsoleCommandSender`, `Server`, and custom `Audience` groupings.

### Standard Messages
```java
// Send direct text component
audience.sendMessage(Component.text("System Notification", NamedTextColor.GRAY));
```

### Action Bars
Displays temporary text above the player's hotbar.
```java
Component actionBarText = MiniMessage.miniMessage().deserialize("<green>Speed Boost Activated! <gold>+20%");
audience.sendActionBar(actionBarText);
```

### Title Sequences
Displays large text on the center of the screen with timings (Fade In, Stay, Fade Out).
```java
import net.kyori.adventure.title.Title;
import java.time.Duration;

Component mainTitle = Component.text("VICTORY", NamedTextColor.GOLD, TextDecoration.BOLD);
Component subtitle = Component.text("You won the match!", NamedTextColor.YELLOW);

// Configure timings
Title.Times times = Title.Times.times(
    Duration.ofMillis(500),  // Fade in
    Duration.ofMillis(2000), // Stay/Display
    Duration.ofMillis(500)   // Fade out
);

Title title = Title.title(mainTitle, subtitle, times);
audience.showTitle(title);

// To clear titles instantly
audience.clearTitle();
```

### BossBars
Displays a custom colored boss progress bar at the top of the player's screen.
```java
import net.kyori.adventure.bossbar.BossBar;

// Create BossBar (Title, Progress [0.0 - 1.0], Color, Overlay)
BossBar bossBar = BossBar.bossBar(
    Component.text("Match Time Remaining", NamedTextColor.RED),
    1.0f, // 100% full
    BossBar.Color.RED,
    BossBar.Overlay.PROGRESS
);

// Show to player
player.showBossBar(bossBar);

// Update progress dynamically (e.g., in a scheduler)
bossBar.progress(0.5f); // Half full

// Remove bossbar
player.hideBossBar(bossBar);
```

---

## 4. Adventure Sound API

Adventure provides a clean way to play audio files using custom resource keys and pitch/volume controls.

```java
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.key.Key;

// Create a sound representation
Sound chime = Sound.sound(
    Key.key("minecraft:block.note_block.chime"), // Sound key
    Sound.Source.BLOCK,                         // Sound source channel
    1.0f,                                       // Volume
    1.2f                                        // Pitch (speed)
);

// Play to the audience target
audience.playSound(chime);

// Play sound at specific block coordinates
audience.playSound(chime, location.getX(), location.getY(), location.getZ());

// Stop playing specific sounds
audience.stopSound(chime);
```

---

## 5. Rich Java Templates

### Example 1: Dynamic Match End Announcement (HouziCore Standard)

Broadcasts a victory sequence using `HouziColorParser`, playing a sound and showing a title to all players.

```java
package com.houzicore.arcade.nautilus.game.arcade.modules;

import com.houzicore.shared.common.util.HouziColorParser;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import java.time.Duration;

public class MatchAnnouncer {

    public static void announceWinner(String winnerName) {
        // 1. Audience target: all players on the server
        Audience allPlayers = Bukkit.getServer();

        // 2. Parse styled texts using HouziColorParser
        Component chatMsg = HouziColorParser.parse(
            "<gold>🏆 <bold>MATCH COMPLETED!</bold> Winner: <gradient:yellow:red>" + winnerName + "</gradient> 🏆"
        );
        Component titleMsg = HouziColorParser.parse("<gold><bold>GAME OVER</bold></gold>");
        Component subtitleMsg = HouziColorParser.parse("<yellow>Winner: " + winnerName + "</yellow>");

        // 3. Configure Title timings
        Title.Times times = Title.Times.times(
            Duration.ofMillis(250), 
            Duration.ofSeconds(3), 
            Duration.ofMillis(500)
        );
        Title title = Title.title(titleMsg, subtitleMsg, times);

        // 4. Create victory audio
        Sound fanfare = Sound.sound(
            Key.key("minecraft:ui.toast.challenge_complete"),
            Sound.Source.MASTER,
            1.0f,
            1.0f
        );

        // 5. Broadcast to all players
        allPlayers.sendMessage(chatMsg);
        allPlayers.showTitle(title);
        allPlayers.playSound(fanfare);
    }
}
```

### Example 2: Interactive Lobby Countdown (HouziCore Standard)

Shows a ticking actionbar to countdown the start of a minigame lobby, utilizing `UpdateEvent` and `MiniPlugin` instead of `BukkitRunnable`.

```java
package com.houzicore.arcade.nautilus.game.arcade.managers;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.HouziColorParser;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

public class LobbyCountdown extends MiniPlugin {

    private int _timer;
    private boolean _active;

    public LobbyCountdown(JavaPlugin plugin) {
        super("Lobby Countdown Manager", plugin);
    }

    public void startCountdown(int seconds) {
        _timer = seconds;
        _active = true;
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!_active || event.getType() != UpdateType.SEC) return;

        if (_timer <= 0) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendActionBar(HouziColorParser.parse("<green><bold>GAME STARTING NOW!</bold></green>"));
            }
            _active = false;
            return;
        }

        // Color-code the ticking seconds
        String color = _timer > 5 ? "<yellow>" : "<red><bold>";
        String rawMsg = "<gray>Lobby starts in: " + color + _timer + "s</gray>";

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendActionBar(HouziColorParser.parse(rawMsg));
        }

        _timer--;
    }
}
```

### Example 3: Dynamic BossBar Level Tracker (HouziCore Standard)

Displays a BossBar tracking player mana, ticking via `UpdateEvent`.

```java
package com.houzicore.arcade.nautilus.game.arcade.modules;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.HouziColorParser;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class ManaTracker extends MiniPlugin {

    private final HashMap<UUID, BossBar> _manaBars = new HashMap<>();
    private final HashMap<UUID, Integer> _playerMana = new HashMap<>();
    private final int MAX_MANA = 100;

    public ManaTracker(JavaPlugin plugin) {
        super("Mana Tracker", plugin);
    }

    public void enableManaBar(Player player) {
        BossBar bar = BossBar.bossBar(
            HouziColorParser.parse("<cyan>Mana: <gold>100/100</gold></cyan>"),
            1.0f,
            BossBar.Color.BLUE,
            BossBar.Overlay.NOTCHED_10
        );
        _manaBars.put(player.getUniqueId(), bar);
        _playerMana.put(player.getUniqueId(), MAX_MANA);
        player.showBossBar(bar);
    }

    public void spendMana(Player player, int amount) {
        int current = _playerMana.getOrDefault(player.getUniqueId(), MAX_MANA);
        _playerMana.put(player.getUniqueId(), Math.max(0, current - amount));
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        // Regenerate 5 mana every second
        if (event.getType() != UpdateType.SEC) return;

        for (UUID uuid : _manaBars.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;

            int current = _playerMana.getOrDefault(uuid, MAX_MANA);
            if (current < MAX_MANA) {
                current = Math.min(MAX_MANA, current + 5);
                _playerMana.put(uuid, current);
            }

            BossBar bar = _manaBars.get(uuid);
            float progress = (float) current / MAX_MANA;
            bar.progress(progress);
            bar.name(HouziColorParser.parse("<cyan>Mana: <gold>" + current + "/" + MAX_MANA + "</gold></cyan>"));

            // Dynamic color thresholding
            if (progress < 0.25f) {
                bar.color(BossBar.Color.RED);
            } else if (progress < 0.5f) {
                bar.color(BossBar.Color.YELLOW);
            } else {
                bar.color(BossBar.Color.BLUE);
            }
        }
    }

    public void disableManaBar(Player player) {
        BossBar bar = _manaBars.remove(player.getUniqueId());
        _playerMana.remove(player.getUniqueId());
        if (bar != null) {
            player.hideBossBar(bar);
        }
    }
}
```}
}
```
