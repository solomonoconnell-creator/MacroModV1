# Macro Recorder (Fabric, Minecraft Java 26.1.x)

Client-side mod. Records movement/camera/action inputs and replays them, with a safety-rule menu.

| Key | Action |
|-----|--------|
| N | Start / stop recording (saves automatically on stop) |
| U | Start / stop playback |
| I | Open menu |

All three are rebindable under Options > Controls > Macro Recorder.

## What is recorded (per tick)
W/A/S/D, jump, sneak, sprint, attack, use, drop, swap-offhand, hotbar slot, and camera yaw/pitch (this is the "mouse movement").
Not recorded: clicking inside GUIs (inventory, chests) and chat. Playback pauses while any screen is open.

## Tips
Start playback from the same spot and facing as when you started recording, or the path will drift.
Recordings live in `.minecraft/config/macrorecorder/recordings/`, settings in `.minecraft/config/macrorecorder/config.json`.

## Build
Needs JDK 25. Check https://fabricmc.net/develop for the current Loom / Loader / Fabric API versions for 26.1.2 and edit gradle.properties.
If there is no gradle wrapper: copy `gradlew`, `gradlew.bat` and `gradle/` from the Fabric example mod, or run `gradle wrapper`.
Then `./gradlew build` and take the jar from `build/libs/` (not the -sources one).

Many servers ban automation. Check the rules before using this online.
