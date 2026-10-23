<div align="center">
  <img src="app/src/main/res/drawable/ic_lumo_logo.xml" width="120" alt="Lumo Store logo" />

  <h1>Lumo Store</h1>
  <p><strong>A free, open-source app store for Android.</strong></p>
  <p>Discover, install, and update open-source apps — without hidden backends.</p>

  <p>
    <a href="https://github.com/adityasaini404/LumoStore/stargazers">
      <img src="https://img.shields.io/github/stars/adityasaini404/LumoStore?logo=github&color=yellow" alt="Stars">
    </a>
    <a href="https://github.com/adityasaini404/LumoStore/network/members">
      <img src="https://img.shields.io/github/forks/adityasaini404/LumoStore?logo=github&color=orange" alt="Forks">
    </a>
    <a href="https://github.com/adityasaini404/LumoStore/blob/main/LICENSE">
      <img src="https://img.shields.io/github/license/adityasaini404/LumoStore?color=blue" alt="License">
    </a>
  </p>

  <p>
    <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">
    <img src="https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
    <img src="https://img.shields.io/badge/Material_3-6750A4?style=for-the-badge&logo=materialdesign&logoColor=white" alt="Material 3">
    <img src="https://img.shields.io/badge/Ktor-087CFA?style=for-the-badge&logo=ktor&logoColor=white" alt="Ktor">
    <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android">
  </p>

  <p>
    <a href="https://github.com/adityasaini404/LumoStore/releases">
      <img src="https://img.shields.io/github/v/release/adityasaini404/LumoStore?style=for-the-badge&logo=github&label=Version&color=6366f1" alt="Latest Version">
    </a>
    <a href="https://github.com/adityasaini404/LumoStore/releases">
      <img src="https://img.shields.io/github/downloads/adityasaini404/LumoStore/total?style=for-the-badge&logo=cloud&label=Downloads&color=10b981" alt="Total Downloads">
    </a>
  </p>
</div>

---

## 🌟 Overview

**Lumo Store** is a lightweight, open-source Android app store that focuses on three things: **simplicity**, **transparency**, and **speed**. Instead of wrapping apps in a private backend, Lumo Store pulls its catalog from a public JSON file and pre-computes every release into a `mirror.json` using **GitHub Actions**.

That means:

- **Zero GitHub API calls from the client** — no rate limits for users
- **No account required** — anonymous browsing by default
- **Full transparency** — every catalog entry is inspectable
- **Community driven** — anyone can submit an app via a GitHub issue

> Lumo Store is built for people who care about where their apps come from.

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔍 **Smart Discovery** | Categories, featured apps, and search |
| ⚡ **Mirror-Powered Speed** | One `mirror.json` file feeds the entire store |
| 🎨 **13+ Themes** | Dark, Light, AMOLED, Ocean, Cyberpunk, Forest, Sunset, Sakura, Dracula, Nord, Coffee, iPhone Light/Dark |
| 👤 **Custom Profile** | Pick your name + 4 hand-drawn avatars (Robot, Cat, Astronaut, Alien) |
| 🛡️ **VirusTotal Integration** | Every app links to a scan of the exact APK |
| 🔑 **Optional GitHub Token** | Works free out-of-the-box; power users can add a PAT for 5,000 req/hour |
| 📦 **Native Install Flow** | Built-in download manager with progress |
| 🌐 **100% Open Source** | MIT licensed |

---

## 📸 Screenshots

> Screenshots coming soon with v1.1

---

## 🛠️ Tech Stack

- **Language:** Kotlin 2.x
- **UI:** Jetpack Compose + Material 3
- **HTTP:** Ktor Client
- **JSON:** Kotlinx Serialization
- **Image Loading:** Coil 3
- **Markdown:** compose-markdown-text
- **CI/CD:** GitHub Actions

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Ladybug or newer
- **JDK 17+**
- **Android SDK 34+**
- Android device or emulator running **Android 8.0+ (API 26+)**

### Clone & Build

\`\`\`bash
git clone https://github.com/adityasaini404/LumoStore.git
cd LumoStore
./gradlew assembleDebug
\`\`\`

APK will be at \`app/build/outputs/apk/debug/app-debug.apk\`.

### Release Build

Create \`keystore.properties\` in project root (never commit this file):

\`\`\`properties
storeFile=/path/to/your.keystore
keyAlias=your-alias
storePassword=your-store-password
keyPassword=your-key-password
\`\`\`

Then:

\`\`\`bash
./gradlew assembleRelease
\`\`\`

---

## 📁 Project Structure

\`\`\`
LumoStore/
├── app/src/main/java/com/lumo/store/
│   ├── MainActivity.kt              # Root activity + navigation
│   ├── data/                        # Models, API, managers
│   │   ├── GitHubApi.kt             # Ktor client + URL normalizer
│   │   ├── TokenManager.kt          # GitHub PAT storage
│   │   ├── UserProfileManager.kt    # Name + avatar index
│   │   ├── ThemeManager.kt          # Theme persistence
│   │   ├── FavoritesManager.kt      # Favorites
│   │   └── ReleaseCache.kt          # Local release cache
│   ├── ui/
│   │   ├── components/              # Reusable composables
│   │   ├── screens/                 # Full screens
│   │   └── theme/                   # LumoTheme + 13 color schemes
│   └── viewmodel/
│       ├── AppViewModel.kt          # Catalog loading
│       └── DownloadViewModel.kt     # APK download + install
└── app/src/main/res/                # Drawables, strings, manifests
\`\`\`

---

## 🤖 The Mirror System

Lumo Store doesn't hit GitHub's API every time you open it. Instead, a GitHub Action pre-computes everything:

\`\`\`
apps.json  →  GitHub Actions (every 6h)  →  mirror.json  →  Lumo Store
\`\`\`

**Result:** Even with 200 apps, the app makes **1 HTTP request** per refresh. Users never see a rate limit.

---

## 🤝 Contributing

Pull requests are welcome! Whether it's:

- 🐛 Bug fixes
- ✨ New features
- 🎨 UI polish
- 📝 Documentation
- 🌐 Translations

Before submitting a PR:

\`\`\`bash
./gradlew test
\`\`\`

---

## 📤 Submitting an App

Anyone can submit an app to Lumo Store:

1. Open Lumo Store
2. Tap the **+** button in the header
3. Fill in the details — live previews of icon & screenshots
4. Tap **Submit for Review**
5. A GitHub issue will open — submit it

---

## 📜 License

This project is licensed under the MIT License — see [LICENSE](LICENSE) for details.

---

<div align="center">
  <p><strong>Made with 💜 in India</strong></p>
  <p>© 2026 Aditya Saini</p>
  <p><sub>Lumo Store is not affiliated with Google, F-Droid, or Orion Store.</sub></p>
</div>
\`\`\`

---
