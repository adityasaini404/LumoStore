 <div align="center">

<img src="assets/ic_lumo_logo.png" width="104" alt="Lumo Store" />

# LUMO STORE

### Your apps. Your freedom. Your choice.

**A free, open-source app store for Android.**

Discover, explore, and update open-source Android apps — with a transparent, community-driven catalog and no private backend required.

<br />

[![GitHub Stars](https://img.shields.io/github/stars/adityasaini404/LumoStore?style=flat-square&logo=github&color=A78BFA)](https://github.com/adityasaini404/LumoStore/stargazers)
[![GitHub Forks](https://img.shields.io/github/forks/adityasaini404/LumoStore?style=flat-square&logo=github&color=818CF8)](https://github.com/adityasaini404/LumoStore/network/members)
[![License](https://img.shields.io/github/license/adityasaini404/LumoStore?style=flat-square&color=34D399)](LICENSE)
[![Latest Release](https://img.shields.io/github/v/release/adityasaini404/LumoStore?style=flat-square&logo=github&color=8B5CF6)](https://github.com/adityasaini404/LumoStore/releases)

<br />

[**Download Lumo Store**](https://github.com/adityasaini404/LumoStore/releases) · [**Explore Features**](#-features) · [**Contribute**](#-contributing) · [**Report a Bug**](https://github.com/adityasaini404/LumoStore/issues)

<br />

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Material_3-6750A4?style=for-the-badge&logo=materialdesign&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)

</div>

---

## ✦ At a glance

<table>
<tr>
<td width="50%" valign="top">

### ◈ Privacy-minded

Browse without creating an account. No private application backend is required for the public catalog.

</td>
<td width="50%" valign="top">

### ◈ Fast by design

A precomputed `mirror.json` reduces catalog loading to a single HTTP request per refresh.

</td>
</tr>
<tr>
<td width="50%" valign="top">

### ◈ Open by default

Inspect the catalog, explore the source code, and contribute through GitHub.

</td>
<td width="50%" valign="top">

### ◈ Built for Android

A native experience powered by Kotlin, Jetpack Compose, and Material 3.

</td>
</tr>
</table>

## 📱 Preview

> Replace these image paths with actual screenshots from your running app. The gallery is designed to showcase the interface before visitors dive into the documentation.

<div align="center">

| Home | App details |
|:---:|:---:|
| <img src="assets/screenshots/home.png" width="220" alt="Lumo Store home screen" /> | <img src="assets/screenshots/app-details.png" width="220" alt="Lumo Store app details screen" /> |
| Discover featured apps and browse the catalog. | Explore app information and available releases. |

| Updates | Settings |
|:---:|:---:|
| <img src="assets/screenshots/updates.png" width="220" alt="Lumo Store updates screen" /> | <img src="assets/screenshots/settings.png" width="220" alt="Lumo Store settings screen" /> |
| Review available app updates. | Customize your experience and preferences. |

</div>

---

## ✨ Features

| Feature | What it offers |
|---|---|
| 🔍 **Smart Discovery** | Search, categories, featured apps, and catalog browsing. |
| ⚡ **Mirror-Powered Catalog** | Precomputed release metadata served through `mirror.json`. |
| 🎨 **13+ Themes** | Dark, Light, AMOLED, Ocean, Cyberpunk, Forest, Sunset, Sakura, Dracula, Nord, Coffee, and iPhone-inspired Light/Dark themes. |
| 👤 **Custom Profile** | Choose a display name and one of four hand-drawn avatars: Robot, Cat, Astronaut, or Alien. |
| 🛡️ **VirusTotal Links** | Open a scan for the exact APK when a corresponding scan is available. |
| 🔑 **Optional GitHub Token** | Works without a personal token; an optional PAT supports higher API limits for compatible requests. |
| 📦 **Native Install Flow** | Download APK files with progress tracking and an installation flow. |
| ➕ **Community Submissions** | Submit app suggestions through the in-app contribution flow. |
| ❤️ **Favorites** | Keep your preferred apps easy to find. |
| 🌐 **Open Source** | MIT-licensed project with a public source repository. |

---

## 🧭 Why Lumo Store?

Many app stores depend on accounts, centralized services, or complex infrastructure. Lumo Store takes a simpler approach.

Its public catalog is maintained as JSON, while GitHub Actions periodically builds a release mirror. The Android client reads the prepared catalog instead of independently querying GitHub for every app.

**The idea is simple:** make discovering open-source Android apps easier while keeping the underlying data and implementation inspectable.

### The mirror pipeline

```text
     apps.json
         │
         ▼
  GitHub Actions
  Scheduled build
         │
         ▼
     mirror.json
         │
         ▼
     Lumo Store
         │
         ▼
 Browse · Search · Discover
```

**Design goals**

- Minimize unnecessary API requests.
- Avoid requiring an account for public catalog browsing.
- Keep the catalog transparent and community-maintainable.
- Deliver a native, customizable Android experience.

---

## 🛠️ Built with

| Technology | Purpose |
|---|---|
| **Kotlin 2.x** | Application language |
| **Jetpack Compose** | Declarative Android UI |
| **Material 3** | Components, color schemes, and theming |
| **Ktor Client** | HTTP networking |
| **Kotlinx Serialization** | JSON parsing and serialization |
| **Coil 3** | Image loading |
| **compose-markdown-text** | Markdown rendering |
| **GitHub Actions** | Catalog mirror automation and CI workflows |

---

## 🚀 Get started

### Requirements

- Android Studio Ladybug or newer
- JDK 17 or newer
- Android SDK 34 or newer
- Android 8.0 or newer (API 26+)

### Clone the repository

```bash
git clone https://github.com/adityasaini404/LumoStore.git
cd LumoStore
```

### Build the debug APK

On macOS/Linux:

```bash
./gradlew assembleDebug
```

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

Find the generated APK at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Build a release APK

Configure your signing credentials in `keystore.properties` in the project root. Never commit this file or your signing credentials.

```properties
storeFile=/path/to/your.keystore
keyAlias=your-alias
storePassword=your-store-password
keyPassword=your-key-password
```

Then run:

```bash
./gradlew assembleRelease
```

Ensure the release signing configuration is set up in your Gradle build before running the release task.

---

## 🗂️ Project structure

```text
LumoStore/
├── app/
│   └── src/main/
│       ├── java/com/lumo/store/
│       │   ├── MainActivity.kt
│       │   ├── data/
│       │   │   ├── GitHubApi.kt
│       │   │   ├── TokenManager.kt
│       │   │   ├── UserProfileManager.kt
│       │   │   ├── ThemeManager.kt
│       │   │   ├── FavoritesManager.kt
│       │   │   └── ReleaseCache.kt
│       │   ├── ui/
│       │   │   ├── components/
│       │   │   ├── screens/
│       │   │   └── theme/
│       │   └── viewmodel/
│       │       ├── AppViewModel.kt
│       │       └── DownloadViewModel.kt
│       └── res/
│
├── .github/
│   └── workflows/
├── assets/
│   ├── ic_lumo_logo.png
│   └── screenshots/
├── LICENSE
└── README.md
```

*This tree illustrates the main source areas. Your actual repository may contain additional files and directories.*

---

## 📤 Submit an app

Help expand the Lumo Store catalog by suggesting an open-source Android application.

1. Open Lumo Store.
2. Tap the **+** button in the header.
3. Enter the app details and review the available previews.
4. Tap **Submit for Review**.
5. Review the generated GitHub issue and submit it.

Submissions can then be reviewed through the project's community workflow.

---

## 🤝 Contributing

Contributions of all sizes are welcome.

- 🐛 Fix bugs and improve reliability.
- ✨ Propose useful features.
- 🎨 Improve UI, accessibility, and animations.
- 📝 Improve documentation.
- 🌍 Add translations.
- 📦 Help maintain the catalog.

### Before opening a pull request

Run the project's tests:

```bash
./gradlew test
```

Please include a clear description of your change and screenshots for significant UI updates.

**Have an idea or found a bug?**

[Open an issue](https://github.com/adityasaini404/LumoStore/issues) · [Browse existing issues](https://github.com/adityasaini404/LumoStore/issues)

---

## 🔐 Transparency & security

- The source code is publicly available for inspection.
- Public catalog browsing does not require a GitHub account.
- An optional GitHub token is intended for users who need higher API limits.
- VirusTotal links provide access to relevant APK scans; a scan is not a guarantee that an app is safe.
- Only install APKs from sources you trust, and review requested permissions before installation.

---

## 📜 License

Lumo Store is licensed under the **MIT License**.

See the [LICENSE](LICENSE) file for the complete terms.

---

<div align="center">

<img src="assets/ic_lumo_logo.png" width="58" alt="Lumo Store logo" />

### Made with 💜 in India

**Lumo Store — Discover more. Own your experience.**

[GitHub Repository](https://github.com/adityasaini404/LumoStore) · [Releases](https://github.com/adityasaini404/LumoStore/releases) · [Issues](https://github.com/adityasaini404/LumoStore/issues)

<sub>© 2026 Aditya Saini · Lumo Store is an independent project and is not affiliated with Google, F-Droid, or Orion Store.</sub>

</div>