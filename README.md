# TFM2 Launcher

Лаунчер для запуска **Teamfight Manager 2** (Steam, Windows) на Android в один тап.

Лаунчер сам игру не эмулирует. Он запускает её через [GameNative](https://github.com/utkarshdalal/GameNative)
и передаёт настройки, подобранные под TFM2. Файлы игры в APK не входят: игра скачивается
из твоей библиотеки Steam внутри GameNative.

## Как пользоваться

1. Установи GameNative ([релизы](https://github.com/utkarshdalal/GameNative/releases/latest)).
2. Войди в Steam в GameNative и установи Teamfight Manager 2.
3. Скачай `tfm2-launcher.apk`: [последняя сборка](https://github.com/yannyoky/tfm2-launcher/releases/download/latest-build/tfm2-launcher.apk) (обновляется на каждый пуш) или стабильную версию из Releases.
4. Открой TFM2 Launcher и нажми «Играть».

## Настройки

| Настройка | По умолчанию | Когда менять |
| --- | --- | --- |
| Разрешение | 1280×720 | выше, если FPS с запасом |
| Графика (DirectX) | Авто | WineD3D, если чёрный экран |
| Режим эмуляции (Box64) | Stability | Не влияет на Wine arm64ec: там x64-код исполняет FEXCore, его пресет («Производительность») ставится в настройках игры в GameNative |
| Показывать FPS | выкл. | для проверки производительности |

«Авто» означает, что лаунчер не трогает эту настройку и GameNative выбирает сам
(на Adreno он ставит драйвер Turnip).

## Как это устроено

GameNative принимает интент `app.gamenative.LAUNCH_GAME` с полями `app_id` (Steam ID игры,
у TFM2 это 3009300), `game_source` и `container_config` (JSON с временными настройками
контейнера). Лаунчер собирает этот JSON в `LaunchConfig.java` и отправляет интент.
Код GameNative в этот проект не входит.

## Сборка

APK собирается в GitHub Actions на каждый пуш. Локально: `./gradlew :app:assembleDebug`
(нужен Android SDK и JDK 17).

`keystore/debug.keystore` — отладочный ключ, нужен, чтобы новые сборки ставились поверх старых.
Для публикации в магазине он не подходит.
