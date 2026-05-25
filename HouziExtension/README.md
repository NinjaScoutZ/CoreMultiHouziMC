<div align="center">
  <h3>
    <a href="README.md">EN</a> | 
    <a href="README-RU.md">RU</a>
  </h3>
</div>

<div align="center">

### 🎥 HouziExtension Video Review

[![HouziExtension](https://img.youtube.com/vi/UjIlfjXzdxE/maxresdefault.jpg)](https://youtu.be/UjIlfjXzdxE "Watch")

</div>

<div class="center-row" align="center">
    <a href="https://www.spigotmc.org/"><img src="https://houzicore.net/pulse/bukkit.svg" alt="bukkit" class="hover-brightness"></a>
    <a href="https://www.spigotmc.org/"><img src="https://houzicore.net/pulse/spigot.svg" alt="spigot" class="hover-brightness"></a>
    <a href="https://papermc.io/"><img src="https://houzicore.net/pulse/paper.svg" alt="paper" class="hover-brightness"></a>
    <a href="https://purpurmc.org/"><img src="https://houzicore.net/pulse/purpur.svg" alt="purpur" class="hover-brightness"></a>
    <a href="https://papermc.io/software/folia"><img src="https://houzicore.net/pulse/folia.svg" alt="folia" class="hover-brightness"></a>
    <a href="https://www.spigotmc.org/wiki/bungeecord/"><img src="https://houzicore.net/pulse/bungeecord.svg" alt="bungeecord" class="hover-brightness"></a>
    <a href="https://papermc.io/software/velocity"><img src="https://houzicore.net/pulse/velocity.svg" alt="velocity" class="hover-brightness"></a>
    <h1>HouziExtension — Every message under your control!</h1>
    <a href="https://boosty.to/thefaser"><img src="https://houzicore.net/pulse/boosty.svg" alt="boosty" class="hover-brightness"></a>
    <a href="https://modrinth.com/plugin/houziextension"><img src="https://houzicore.net/pulse/modrinth.svg" alt="modrinth" class="hover-brightness"></a>
    <a href="https://houzicore.net/pulse/"><img src="https://houzicore.net/pulse/documentation.svg" alt="documentation" class="hover-brightness"></a>
    <a href="https://discord.houzicore.net/"><img src="https://houzicore.net/pulse/discord.svg" alt="discord" class="hover-brightness"></a>
</div>

## 🏆 What makes HouziExtension special?

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/Houzi/HouziExtension)

HouziExtension is a plugin and mod for Minecraft servers that takes control of chat, messages, and notifications. Beginners will especially love it because the setup is simple, and the result is a beautiful chat, integrations, and useful commands without any hassle.

- All operations are performed asynchronously, the main server thread is not affected
- Uses Google Guice for dependency injection, which simplifies extending functionality
- Supports all popular platforms Bukkit, Spigot, Paper, Purpur, Folia, Fabric, BungeeCord, Waterfall, and Velocity on versions 1.8.8 to the latest

## 🎨 Flexible text formatting

Supports all color formats, from legacy (`&` or `§` for colors) to modern MiniMessage tags

| **Input code**                                        | **Transformation**                                             |  
|-------------------------------------------------------|-------------------------------------------------------------|  
| `&0`-`&9`, `&a`-`&f`                                  | `<black>`, `<dark_blue>`, ..., `<white>`                    |  
| `&l`/`&m`/`&n`/`&o`/`&k`/`&r`                         | `<b>` / `<st>` / `<u>` / `<i>` / `<obf>` / `<reset>`        |  
| `&#rrggbb`, `#rrggbb`, `&x&r&r&g&g&b&b`, `<##rrggbb>` | `<#rrggbb>`                                                 |  
| MiniMessage tags                                      | `<color:#rrggbb>`, `<rainbow>`, `<click:...>`, `<font>`, etc. |

```yaml
# EXAMPLE
join:
  format: "<gradient:#FF0000:#00FF00>&lHello</gradient> <rainbow><player></rainbow>!"
```

![color](https://houzicore.net/pulse/welcomemessage.png)

## 🧱 Any textures in messages (WITHOUT RESOURCE PACK)

Add custom texture (image) using the `<texture:name>` placeholder
[![texture1](https://houzicore.net/pulse/texturemotd.png)](https://houzicore.net/pulse/docs/message/format/object/)
[![texture2](https://houzicore.net/pulse/texture.png)](https://houzicore.net/pulse/docs/message/format/object/)

Use avatars in messages using the `<player_head>` placeholder and Minecraft symbols using `<sprite:name>`
[![object](https://houzicore.net/pulse/object.png)](https://houzicore.net/pulse/docs/message/format/object/)

## 🌈 Chat customization with /chatsetting

![chatsetting](https://houzicore.net/pulse/commandchatsetting.gif)

The /chatsetting command opens a menu for quick message customization for each player. Choose colors, styles, and disable unnecessary messages

## 🌍 Smart localization

### How it works
[![locale](https://houzicore.net/pulse/locale.gif)](https://houzicore.net/pulse/docs/config/language/)

When `by_player: true` is enabled, HouziExtension detects the client's language and displays messages in it. If no translation exists, the default from the config is used. More in the [documentation](https://houzicore.net/pulse/docs/config#language-player) 🔗

## ✨ Customizable elements

| **Visual** | **Description** |  
|---------------|-----------------|  
| ![status](https://houzicore.net/pulse/version.png) | **Server MOTD** <br> Add custom texts for greetings in the server list |  
| ![join](https://houzicore.net/pulse/join.png) | **Join messages** <br> Greet players |  
| ![tab](https://houzicore.net/pulse/tab.png) | **TAB menu** <br> Show ping, online, ranks — all in one place |  
| ![death](https://houzicore.net/pulse/deathserver.png) | **Death messages** <br> Make them fun with text or sounds |  
| ![brand](https://houzicore.net/pulse/brand.png) | **Branding** <br> Add server name to the F3 menu |  
| ![advancement](https://houzicore.net/pulse/task.png) | **Advancements** <br> Custom advancement messages |  
| Full list in [documentation](https://houzicore.net/pulse/docs/message/) 🔗                                                | ...                                                               |

## 🤝 Integrations

### External platforms

| **Visual** | **Description** |  
|---------|----------|  
| [![discord](https://houzicore.net/pulse/discordmessage.png)](https://houzicore.net/pulse/docs/integration/discord/) | **Discord** <br> Sync server chat with Discord channels |  
| [![telegram](https://houzicore.net/pulse/telegrammessage2.png)](https://houzicore.net/pulse/docs/integration/telegram/) | **Telegram** <br> Send messages via bot to Telegram and sync chats |  
| [![twitch](https://houzicore.net/pulse/twitchmessage.png)](https://houzicore.net/pulse/docs/integration/twitch/) | **Twitch** <br> Stream notifications in Minecraft chat and server chat sync |  

### Plugins

| **Plugin**                                                                                | **Description**                                                 |  
|-----------------------------------------------------------------------------------------------|--------------------------------------------------------------------|  
| 💬 **[InteractiveChat](https://houzicore.net/pulse/docs/integration/interactivechat/)**        | Interactive elements in chat              | 
| 🛡️ **[LuckPerms](https://houzicore.net/pulse/docs/integration/luckperms/)**                   | Permission and group management         |  
| 🧩 **[PlaceholderAPI](https://houzicore.net/pulse/docs/integration/placeholderapi/)**          | Placeholders from other plugins, e.g. %player_level%           |  
| 🎙️ **[PlasmoVoice & SimpleVoice](https://houzicore.net/pulse/docs/integration/plasmovoice/)** | Ignore and mute sync in voice chat           |  
| 🖼️ **[SkinsRestorer](https://houzicore.net/pulse/docs/integration/skinsrestorer/)**           | Skin display in chat and TAB                 |  
| 👻 **[SuperVanish](https://houzicore.net/pulse/docs/integration/supervanish/)**                | Hide vanished players              |  
| Full list in [documentation](https://houzicore.net/pulse/docs/integration/) 🔗                                                                                          | ...                                                               |

## 🎮 Over 30 commands

| **Visual** | **Description** |  
|--------------|------------------------|  
| [![ball](https://houzicore.net/pulse/commandball.png)](https://houzicore.net/pulse/docs/command/) | **/ball** <br> Magic ball with many answers |  
| [![tictactoe](https://houzicore.net/pulse/commandtictactoe.png)](https://houzicore.net/pulse/docs/command/) | **/tictactoe** <br> Tic-tac-toe |  
| [![stream](https://houzicore.net/pulse/commandstream.png)](https://houzicore.net/pulse/docs/command/) | **/stream** <br> Stream notifications in chat |  
| [![try](https://houzicore.net/pulse/commandtry.png)](https://houzicore.net/pulse/docs/command/) | **/try** <br> Test your luck from 0% to 100% |  
| Full list in [documentation](https://houzicore.net/pulse/docs/command/) 🔗                                                                                           | ...                                                              |

## ❓ FAQ

Questions about installation or setup? Check the [FAQ](https://houzicore.net/pulse/docs/) in the documentation

## 🙏 Acknowledgments

HouziExtension is built on these projects:

- 🏗️ **[Google Guice](https://github.com/google/guice)** — for modular code
- 📚 **[JDBI](https://jdbi.org/)** with **[HikariCP](https://github.com/brettwooldridge/HikariCP)** — efficient database work
- 📦 **[Jackson](https://github.com/FasterXML/jackson)** — data serialization
- 🧙 **[PacketEvents](https://github.com/retrooper/packetevents)** — packet handling
- 🎨 **[Adventure](https://github.com/KyoriPowered/adventure)** — text formatting
- ⌨️ **[Cloud](https://github.com/Incendo/cloud)** — commands with autocompletion
- ⏱️ **[Universal Scheduler](https://github.com/Anon8281/UniversalScheduler)** — task scheduling
- 🔣 **[Symbol Chat](https://github.com/replaceitem/symbol-chat)** — symbols in chat
- 🖥️ **[PacketUxUi](https://github.com/OceJlot/PacketUxUi)** — GUI elements
- 💬 **[LightChatBubbles](https://github.com/atesin/LightChatBubbles)** — messages above head
- 🌐 **[MiniTranslator](https://github.com/imDaniX/MiniTranslator)** — legacy color conversion
- 🌱 **[HouziChat](https://github.com/Houzi/HouziChat)** — predecessor of HouziExtension

And thanks to the community! Every star on GitHub and review on platforms shows that HouziExtension is truly needed ⭐

## 📊 Project statistics
<div align="center">
  <a href="https://houzicore.net/en/pulse/metrics" target="_blank">
    <img src="https://houzicore.net/api/pulse/metrics/svg" alt="HouziExtension Statistics">
  </a>
</div>

## ❤️ Open source and free

HouziExtension is completely free. Download, modify, put on your server. For priority support, early access to features, and help with server-specific setup, support on Boosty. It motivates further development!

<div align="center">
  <a href="https://boosty.to/thefaser"><img src="https://houzicore.net/pulse/boosty.svg" alt="boosty" class="hover-brightness"></a>
  <h2><b>HouziExtension is waiting for you! Ready to install? 😎</b></h2>
  <a href="https://modrinth.com/plugin/houziextension"><img src="https://houzicore.net/pulse/modrinth.svg" width="200" alt="modrinth"></a>
  <br>
  <h3>P.S. Join <a href="https://discord.houzicore.net/">Discord</a></h3>
</div>