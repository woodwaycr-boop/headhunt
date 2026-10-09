# HeadHunt

Paper 1.21.11 plugin. Requires Vault and an economy plugin (e.g. EssentialsX).

## Features
- Mobs drop their head when a player kills them (chance and price per mob in `config.yml`). Spawner mobs count.
- `/headlist` opens a menu of every participating mob head with its sell value and drop chance.
- `/sellheads` sells every plugin-made head in your inventory. `/sellheads hand` sells only the held stack.
- Only heads made by this plugin can be sold. `/give`d, placed or menu-icon heads are worthless.
- Optional anti-farm: list spawn reasons in `blocked-spawn-reasons` (empty by default).

## Commands and permissions
| Command | Permission | Default |
|---|---|---|
| `/headlist` | `headhunt.list` | everyone |
| `/sellheads [hand]` | `headhunt.sell` | everyone |
| `/headhunt reload` | `headhunt.admin` | op |

## Mobs
Active now: creeper, spider, zombie, skeleton, chicken, cow, sheep, pig.
Many more are in `config.yml`, commented out. Delete the `#` to enable one. `/headlist` fits 28 mobs.

## Build the jar
**GitHub (nothing to install):** create a repository, upload this folder's contents (`pom.xml`, `README.md`, `src`, `.github`), open the Actions tab, and download the `HeadHunt` artifact. Unzip it to get `HeadHunt-1.0.0.jar`.
**Your PC:** install JDK 21 and Maven, run `mvn clean package`, take `target/HeadHunt-1.0.0.jar`.

## Install on ScalaCube
1. Server must run Paper 1.21.11.
2. Upload `HeadHunt-1.0.0.jar`, Vault, and EssentialsX to `/plugins`.
3. Restart. Edit `plugins/HeadHunt/config.yml`, then `/headhunt reload`.

## Notes
- Prices and chances are placeholders. Tune them against your server's income rates.
- Spider, chicken, cow, sheep and pig heads load their skins from Mojang's MHF accounts. If one shows a default skin, paste a base64 `texture:` value for that mob in the config.
