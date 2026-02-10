# EasyLogin

A powerful, **server-only** authentication mod for Minecraft 1.21.1, supporting **NeoForge**, **Fabric**, and **Forge**.

EasyLogin provides a lightweight and secure `/login` and `/register` system for servers. Since it is server-only, players do not need to install anything on their clients to join and authenticate.

## 🚀 Features

- **Multi-Loader Support**: One codebase, three loaders (NeoForge, Forge, Fabric).
- **Server-Only**: No client-side installation required.
- **Secure Hashing**: Passwords are hashed using **BCrypt** (Cost Factor: 12).
- **Freeze-in-Place**: Unauthenticated players are frozen at their join position (movement, interaction, and damage are blocked).
- **Limbo System**: Optional teleportation to a dedicated "Limbo" coordinate during authentication.
- **FakePlayer Protection**: Automatically detects and bypasses FakePlayers (e.g., from mods like Carpet).
- **Rate Limiting**: Per-IP and per-UUID brute-force protection.
- **Configurable**: Customize messages, sounds, timeouts, and rules via a simple TOML configuration.
- **Persistent Storage**: Player data is stored safely in JSON format with atomic writes and backups.

## 🛠️ Commands

### Player Commands
- `/login <password>`: Authenticate with an existing account.
- `/register <password> <confirm_password>`: Create a new account.
- `/changepassword <old> <new> <confirm>`: Update your password while logged in.
- `/logout`: Log out of your current session (configurable cooldown).

### Admin Commands (Permission Level 3)
- `/auth status`: Show registration statistics.
- `/auth reload`: Reload the configuration file.
- `/auth force-login <player>`: Manually authenticate a player.
- `/auth force-register <player> <password>`: Register a player with a specific password.
- `/auth reset-password <player>`: Delete a player's registration data.
- `/auth purge <player>`: Completely wipe a player's account.

## 📦 Installation

1. Download the JAR for your server loader (NeoForge, Forge, or Fabric).
2. Place the JAR in the `mods` folder of your server.
3. Start the server to generate the configuration file at `config/easylogin/config.toml`.
4. (Optional) Customize the messages and settings in the config file.

## 🔨 Building

The project uses Gradle. To build all versions:

```bash
./gradlew assemble
```

Artifacts will be located in:
- `neoforge/build/libs/`
- `forge/build/libs/`
- `fabric/build/libs/`

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
