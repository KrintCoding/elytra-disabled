
  ![plugin-fon](https://github.com/user-attachments/assets/5aa6f542-ebd8-4ba2-82da-d542b66d6ce6)

**ElytraDisabled** - A comprehensive solution for controlling elytra usage on your server. The plugin completely disables elytra in specified worlds, providing maximum protection against bypasses.

**Features**:

- Complete blocking of equipping and flying with elytra
- Auto-removal when entering restricted worlds
- Advanced anti-bypass protection with tick-by-tick monitoring
- Smart cooldown-based notifications to prevent spam
- Multi-language support (English, Russian, Ukrainian)
- Configurable sound effects for blocked actions
- Automatic update checking via Modrinth API
- Protection against dispenser armor equip exploits
- Optimized performance with efficient cooldown management

Perfect for servers that value fair gameplay, stability, and absolute control over elytra mechanics. ElytraDisabled ensures predictable player behavior and eliminates any possibility of unwanted flight exploits.
## config.yml

```
# ===================================================
# ElytraDisabled - Основные настройки / Main Settings
# ===================================================

settings:
  # Включить плагин? / Enable plugin?
  enable_plugin: true

  # Проверять наличие обновлений плагина при запуске
  # Check for plugin updates on startup
  check_updates: true

  # Язык плагина (ru, en, ua) / Plugin language (ru, en, ua)
  # ru - русский, en - english, ua - українська
  language: en

  # Кулдаун между сообщениями игроку (в миллисекундах)
  # 3000 = 3 секунды, чтобы не спамить при повторных попытках
  # Cooldown between messages to player (in milliseconds)
  # 3000 = 3 seconds, to avoid spam on repeated attempts
  message_cooldown: 3000

  # Список миров, где запрещены элитры
  # List of worlds where elytra is disabled
  disable_in_worlds:
    - world_the_end

  # Запретить надевание элитр в запрещённых мирах
  # Если false - игроки смогут носить элитры, но не смогут летать
  # ВАЖНО: Для работы при false необходимо также установить force_unequip_on_enter: false
  #
  # Prevent equipping elytra in disabled worlds
  # If false - players can wear elytra but cannot glide
  # IMPORTANT: For this to work when false, you must also set force_unequip_on_enter: false
  prevent_equip: true

  # Автоматически снимать элитры при входе в запрещённый мир
  # Automatically remove elytra when entering disabled world
  force_unequip_on_enter: true

  # Останавливать активный полёт на элитрах в запрещённых мирах
  # Stop active elytra flight in disabled worlds
  stop_existing_glide: true

  # Грубый анти-байпас: проверять каждый тик (может нагружать сервер)
  # true - максимальная защита (проверка каждый тик), false - экономия производительности
  # Rough anti-bypass: check every tick (may affect server performance)
  # true - maximum protection (check every tick), false - performance saving
  check_every_tick: true

  # Воспроизводить звук при блокировке элитр
  # Play sound when elytra is blocked
  play_sound: true

  # Тип звука (список звуков: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html)
  # Sound type (sound list: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html)
  # Популярные варианты / Popular options:
  # - ENTITY_VILLAGER_NO
  # - BLOCK_ANVIL_LAND
  # - ENTITY_ENDERMAN_TELEPORT
  # - BLOCK_NOTE_BLOCK_BASS
  sound_type: "ENTITY_VILLAGER_NO"

permissions:
  # Игроки с этим пермишеном могут использовать элитры в любых мирах
  # Players with this permission can use elytra in any worlds
  bypass: "elytradisabled.bypass"
```
# Video
[![Video](https://img.youtube.com/vi/0RfcnuwPMrI/hqdefault.jpg)](https://www.youtube.com/embed/0RfcnuwPMrI)
