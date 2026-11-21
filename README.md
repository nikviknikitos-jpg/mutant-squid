# MutantSquidMod (Fabric 1.20.1)

## Быстрый старт
1. Установите JDK 17.
2. Установите Gradle 8.6 или сгенерируйте wrapper: `gradle wrapper --gradle-version 8.6`
3. Положите файл `ritual_song.ogg` в `src/main/resources/assets/mutantsquid/sounds/ritual_song.ogg`
4. Соберите: `./gradlew build`
5. Готовый jar: `build/libs/mutantsquid-1.0.0.jar`

## Ритуал
- Бросьте `minecraft:nautilus_shell` на землю.
- Ждите 5 минут (или используйте `/skipritual`).
- Приведите 4 мобов (не игроков) в радиусе 10 блоков от щупалец.
- После поедания появится босс.

## Команды
- `/skipritual` — перемотка таймера на последние 3 секунды (для тестов).
