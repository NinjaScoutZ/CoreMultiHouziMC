<div align="center">
  <h3>
    <a href="README.md">EN</a> | 
    <a href="README-RU.md">RU</a>
  </h3>
</div>

<div align="center">

### 🎥 Видеообзор HouziExtension

[![HouziExtension](https://img.youtube.com/vi/UjIlfjXzdxE/maxresdefault.jpg)](https://youtu.be/UjIlfjXzdxE "Посмотреть")

</div>

<div class="center-row" align="center">
    <a href="https://www.spigotmc.org/"><img src="https://houzicore.net/pulse/bukkit.svg" alt="bukkit" class="hover-brightness"></a>
    <a href="https://www.spigotmc.org/"><img src="https://houzicore.net/pulse/spigot.svg" alt="spigot" class="hover-brightness"></a>
    <a href="https://papermc.io/"><img src="https://houzicore.net/pulse/paper.svg" alt="paper" class="hover-brightness"></a>
    <a href="https://purpurmc.org/"><img src="https://houzicore.net/pulse/purpur.svg" alt="purpur" class="hover-brightness"></a>
    <a href="https://papermc.io/software/folia"><img src="https://houzicore.net/pulse/folia.svg" alt="folia" class="hover-brightness"></a>
    <a href="https://www.spigotmc.org/wiki/bungeecord/"><img src="https://houzicore.net/pulse/bungeecord.svg" alt="bungeecord" class="hover-brightness"></a>
    <a href="https://papermc.io/software/velocity"><img src="https://houzicore.net/pulse/velocity.svg" alt="velocity" class="hover-brightness"></a>
    <h1>HouziExtension — Каждое сообщение под вашим контролем!</h1>
    <a href="https://boosty.to/thefaser"><img src="https://houzicore.net/pulse/boosty.svg" alt="boosty" class="hover-brightness"></a>
    <a href="https://modrinth.com/plugin/houziextension"><img src="https://houzicore.net/pulse/modrinth.svg" alt="modrinth" class="hover-brightness"></a>
    <a href="https://houzicore.net/pulse/"><img src="https://houzicore.net/pulse/documentation.svg" alt="documentation" class="hover-brightness"></a>
    <a href="https://discord.houzicore.net/"><img src="https://houzicore.net/pulse/discord.svg" alt="discord" class="hover-brightness"></a>
</div>

## 🏆 Что делает HouziExtension особенным?

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/Houzi/HouziExtension)

HouziExtension — плагин и мод для Minecraft-серверов, который берёт под контроль чат, сообщения и уведомления. Новичкам он особенно понравится, потому что настройка простая, а результат — красивый чат, интеграции и полезные команды без лишней мороки.

- Все операции выполняются асинхронно, основной поток сервера не затрагивается
- Используется Google Guice для инъекции зависимостей, что упрощает расширение функционала
- Поддерживает все популярные платформы Bukkit, Spigot, Paper, Purpur, Folia, Fabric, BungeeCord, Waterfall и Velocity на версиях 1.8.8 до самой последней

## 🎨 Гибкое форматирование текста

Поддерживаются все форматы цветов, от устаревших (`&` или `§` для цветов) до современных тегов MiniMessage

| **Ввод кода**                                        | **Преобразование**                                             |  
|-------------------------------------------------------|-------------------------------------------------------------|  
| `&0`-`&9`, `&a`-`&f`                                  | `<black>`, `<dark_blue>`, ..., `<white>`                    |  
| `&l`/`&m`/`&n`/`&o`/`&k`/`&r`                         | `<b>` / `<st>` / `<u>` / `<i>` / `<obf>` / `<reset>`        |  
| `&#rrggbb`, `#rrggbb`, `&x&r&r&g&g&b&b`, `<##rrggbb>` | `<#rrggbb>`                                                 |  
| Теги MiniMessage                                      | `<color:#rrggbb>`, `<rainbow>`, `<click:...>`, `<font>`, и т.д. |

```yaml
# ПРИМЕР
join:
  format: "<gradient:#FF0000:#00FF00>&lПривет</gradient> <rainbow><player></rainbow>!"
```

![color](https://houzicore.net/pulse/welcomemessage.png)

## 🧱 Любые текстуры в сообщениях (БЕЗ РЕСУРСПАКА)

Добавьте свою текстуру (изображение) с помощью плейсхолдера `<texture:название>`
[![texture1](https://houzicore.net/pulse/texturemotd.png)](https://houzicore.net/pulse/docs/message/format/object/)
[![texture2](https://houzicore.net/pulse/texture.png)](https://houzicore.net/pulse/docs/message/format/object/)

Используйте аватарки в сообщениях с помощью плейсхолдера `<player_head>` и символы Minecraft через `<sprite:название>`
[![object](https://houzicore.net/pulse/object.png)](https://houzicore.net/pulse/docs/message/format/object/)

## 🌈 Настройка чата с /chatsetting

![chatsetting](https://houzicore.net/pulse/commandchatsetting.gif)

Команда /chatsetting открывает меню для быстрой кастомизации сообщений каждому игроку. Выберите цвета, стили и отключите ненужные сообщения

## 🌍 Умная локализация

### Как это работает
[![locale](https://houzicore.net/pulse/locale.gif)](https://houzicore.net/pulse/docs/config/language/)

При включённом `by_player: true` HouziExtension определяет язык клиента и показывает сообщения на нём. Если перевода нет, будет использоваться дефолтный из конфига. Подробнее в [документации](https://houzicore.net/pulse/docs/config#language-player) 🔗

## ✨ Настраиваемые элементы

| **Визуал** | **Описание** |  
|---------------|-----------------|  
| ![status](https://houzicore.net/pulse/version.png) | **MOTD сервера** <br> Добавьте кастомные тексты для приветствия в списке серверов |  
| ![join](https://houzicore.net/pulse/join.png) | **Сообщения о входе** <br> Приветствуйте игроков |  
| ![tab](https://houzicore.net/pulse/tab.png) | **TAB-меню** <br> Покажите пинг, онлайн, ранги — всё в одном месте |  
| ![death](https://houzicore.net/pulse/deathserver.png) | **Сообщения о смерти** <br> Сделайте их забавными с текстом или звуками |  
| ![brand](https://houzicore.net/pulse/brand.png) | **Брендинг** <br> Добавьте название сервера в меню F3 |  
| ![advancement](https://houzicore.net/pulse/task.png) | **Достижения** <br> Кастомные сообщения о достижениях |  
| Полный список в [документации](https://houzicore.net/pulse/docs/message/) 🔗                                                | ...                                                               |

## 🤝 Интеграции

### Внешние платформы

| **Визуал** | **Описание** |  
|---------|----------|  
| [![discord](https://houzicore.net/pulse/discordmessage.png)](https://houzicore.net/pulse/docs/integration/discord/) | **Discord** <br> Синхронизируйте чат сервера с каналами Discord |  
| [![telegram](https://houzicore.net/pulse/telegrammessage2.png)](https://houzicore.net/pulse/docs/integration/telegram/) | **Telegram** <br> Отправляйте сообщения через бота в Telegram и синхронизируйте чаты |  
| [![twitch](https://houzicore.net/pulse/twitchmessage.png)](https://houzicore.net/pulse/docs/integration/twitch/) | **Twitch** <br> Уведомления о стримах в чате Minecraft и синхронизация чата сервера |  

### Плагины

| **Плагин**                                                                                | **Описание**                                                 |  
|-----------------------------------------------------------------------------------------------|--------------------------------------------------------------------|  
| 💬 **[InteractiveChat](https://houzicore.net/pulse/docs/integration/interactivechat/)**        | Интерактивные элементы в чате              | 
| 🛡️ **[LuckPerms](https://houzicore.net/pulse/docs/integration/luckperms/)**                   | Управление правами и группами         |  
| 🧩 **[PlaceholderAPI](https://houzicore.net/pulse/docs/integration/placeholderapi/)**          | Плейсхолдеры из других плагинов, например %player_level%           |  
| 🎙️ **[PlasmoVoice & SimpleVoice](https://houzicore.net/pulse/docs/integration/plasmovoice/)** | Синхронизация игноров и мутов в голосовом чате           |  
| 🖼️ **[SkinsRestorer](https://houzicore.net/pulse/docs/integration/skinsrestorer/)**           | Отображение скинов в чате и TAB                 |  
| 👻 **[SuperVanish](https://houzicore.net/pulse/docs/integration/supervanish/)**                | Скрытие игроков в ванише              |  
| Полный список в [документации](https://houzicore.net/pulse/docs/integration/) 🔗                                                                                          | ...                                                               |

## 🎮 Более 30 команд

| **Визуал** | **Описание** |  
|--------------|------------------------|  
| [![ball](https://houzicore.net/pulse/commandball.png)](https://houzicore.net/pulse/docs/command/) | **/ball** <br> Волшебный шар с множеством ответов |  
| [![tictactoe](https://houzicore.net/pulse/commandtictactoe.png)](https://houzicore.net/pulse/docs/command/) | **/tictactoe** <br> Крестики-нолики |  
| [![stream](https://houzicore.net/pulse/commandstream.png)](https://houzicore.net/pulse/docs/command/) | **/stream** <br> Уведомления о стримах в чате |  
| [![try](https://houzicore.net/pulse/commandtry.png)](https://houzicore.net/pulse/docs/command/) | **/try** <br> Испытай удачу от 0% до 100% |  
| Полный список в [документации](https://houzicore.net/pulse/docs/command/) 🔗                                                                                           | ...                                                              |

## ❓ FAQ

Вопросы по установке или настройке? Загляните в [FAQ](https://houzicore.net/pulse/docs/) в документации

## 🙏 Благодарности

HouziExtension основан на этих проектах:

- 🏗️ **[Google Guice](https://github.com/google/guice)** — для модульного кода
- 📚 **[JDBI](https://jdbi.org/)** с **[HikariCP](https://github.com/brettwooldridge/HikariCP)** — эффективная работа с БД
- 📦 **[Jackson](https://github.com/FasterXML/jackson)** — сериализация данных
- 🧙 **[PacketEvents](https://github.com/retrooper/packetevents)** — обработка пакетов
- 🎨 **[Adventure](https://github.com/KyoriPowered/adventure)** — форматирование текста
- ⌨️ **[Cloud](https://github.com/Incendo/cloud)** — команды с автодополнением
- ⏱️ **[Universal Scheduler](https://github.com/Anon8281/UniversalScheduler)** — планирование задач
- 🔣 **[Symbol Chat](https://github.com/replaceitem/symbol-chat)** — символы в чате
- 🖥️ **[PacketUxUi](https://github.com/OceJlot/PacketUxUi)** — GUI-элементы
- 💬 **[LightChatBubbles](https://github.com/atesin/LightChatBubbles)** — сообщения над головой
- 🌐 **[MiniTranslator](https://github.com/imDaniX/MiniTranslator)** — конвертация устаревших цветов
- 🌱 **[HouziChat](https://github.com/Houzi/HouziChat)** — предок HouziExtension

И спасибо сообществу! Каждая звезда на GitHub и отзыв на платформах показывают, что HouziExtension действительно нужен ⭐

## 📊 Статистика проекта
<div align="center">
  <a href="https://houzicore.net/en/pulse/metrics" target="_blank">
    <img src="https://houzicore.net/api/pulse/metrics/svg" alt="Статистика HouziExtension">
  </a>
</div>

## ❤️ Код открытый, а проект бесплатный

HouziExtension полностью бесплатный. Скачивайте, модифицируйте, ставьте на сервер. А для приоритетной поддержки, раннего доступа к фичам и помощи с настройками под ваш сервер поддержите на Boosty. Это мотивирует развивать проект дальше!

<div align="center">
  <a href="https://boosty.to/thefaser"><img src="https://houzicore.net/pulse/boosty.svg" alt="boosty" class="hover-brightness"></a>
  <h2><b>HouziExtension ждёт вас! Готовы установить? 😎</b></h2>
  <a href="https://modrinth.com/plugin/houziextension"><img src="https://houzicore.net/pulse/modrinth.svg" width="200" alt="modrinth"></a>
  <br>
  <h3>P.S. Присоединяйтесь к <a href="https://discord.houzicore.net/">Discord</a></h3>
</div>