
  ![plugin-fon](https://github.com/user-attachments/assets/5aa6f542-ebd8-4ba2-82da-d542b66d6ce6)

**ElytraDisabled** - A comprehensive solution for controlling elytra usage on your server. The plugin completely disables elytra in specified worlds, providing maximum protection against bypasses.

**Features**:

- Complete blocking of equipping and flying
- Auto-removal when switching between worlds
- Anti-bypass protection and fall damage prevention
- Smart notifications and multi-language support
- Simple config setup and permission system

Perfect for servers that value fair gameplay, stability, and absolute control over elytra mechanics. ElytraDisabled ensures predictable player behavior and eliminates any possibility of unwanted flight exploits.
## config.yml

```
settings:
  # Включить плагин? / Enable plugin?
  enable_plugin: true

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
  # Prevent equipping elytra in disabled worlds
  prevent_equip: true

  # Автоматически снимать элитры при входе в запрещённый мир
  # Automatically remove elytra when entering disabled world
  force_unequip_on_enter: true

  # Запретить начало полёта на элитрах
  # Prevent starting elytra flight
  prevent_glide: true

  # Принудительно останавливать полёт, если он уже начался
  # Force stop flight if it has already started
  stop_existing_glide: true

  # Грубый анти-байпас: проверять каждый тик (может нагружать сервер)
  # true - максимальная защита (проверка каждый тик), false - экономия производительности
  # Rough anti-bypass: check every tick (may affect server performance)
  # true - maximum protection (check every tick), false - performance saving
  check_every_tick: true

  # Предотвращать урон от падения после снятия элитр плагином
  # Prevent fall damage after elytra removal by plugin
  prevent_fall_damage: true

permissions:
  # Игроки с этим пермишеном могут использовать элитры в любых мирах
  # Players with this permission can use elytra in any worlds
  bypass: "elytradisabled.bypass"
```
## Video
https://youtu.be/0RfcnuwPMrI
