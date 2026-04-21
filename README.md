# Complaint Logbook

Aplikasi logbook komplain tamu berbasis Android — dicatat, dilacak, dan diselesaikan keluhan secara terorganisir.

[![Build & Lint](https://github.com/hoshiyomiX/complaint-logbook/actions/workflows/build.yml/badge.svg)](https://github.com/hoshiyomiX/complaint-logbook/actions/workflows/build.yml)
[![Release](https://github.com/hoshiyomiX/complaint-logbook/actions/workflows/release.yml/badge.svg)](https://github.com/hoshiyomiX/complaint-logbook/actions/workflows/release.yml)

## Fitur

- Kalender interaktif dengan dot indikator (oranye = aktif, hijau = selesai)
- 4 period view: Harian / Mingguan / Bulanan / Tahunan
- Navigasi periode maju/mundur + tombol "Hari Ini"
- Filter status: Semua / Aktif / Selesai
- Dialog konfirmasi sebelum menghapus komplain
- Snackbar feedback setelah setiap aksi (tambah, selesai, hapus)
- Tandai selesai dengan rekam timestamp otomatis
- Kategori: AC, Lampu, Kebersihan, Air, TV/WiFi, Lainnya
- Data persisten via Room database (dengan destructive migration fallback)
- Adaptive icon + edge-to-edge display
- Dark mode otomatis

## Teknologi

| Layer | Teknologi |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| Arsitektur | MVVM (ViewModel + StateFlow + CreationExtras) |
| Database | Room (SQLite) |
| Bahasa | Kotlin |
| Build | Gradle + KSP |
| CI/CD | GitHub Actions |

## CI/CD Pipeline

| Workflow | Trigger | Output |
|----------|---------|--------|
| **Build & Lint** | Push/PR ke `main` | Debug APK + Lint report (artifacts) |
| **Release** | Tag `v*` (contoh: `v1.0.0`) | Signed release APK + GitHub Release |

### Build & Lint

Setiap push ke `main` atau pull request akan menjalankan:
1. Build debug APK (`assembleDebug`)
2. Android Lint analysis (`lintDebug`)
3. Upload APK dan lint report sebagai artifacts

Download APK dari tab **Actions** → pilih workflow run → **Artifacts**.

### Release

Untuk membuat release APK yang signed:

1. **Setup GitHub Secrets** di repository Settings → Secrets and variables → Actions:
   - `RELEASE_KEYSTORE_BASE64` — Keystore file yang di-encode ke base64
   - `KEYSTORE_PASSWORD` — Password keystore
   - `KEY_ALIAS` — Alias key yang digunakan
   - `KEY_PASSWORD` — Password key

2. **Generate keystore** (hanya sekali):
   ```bash
   keytool -genkey -v -keystore release.keystore \
     -alias complaint-logbook -keyalg RSA -keysize 2048 \
     -validity 10000 -storepass YOUR_STORE_PASSWORD \
     -keypass YOUR_KEY_PASSWORD
   ```

3. **Encode keystore ke base64**:
   ```bash
   base64 -w 0 release.keystore > keystore_base64.txt
   ```
   Copy isi `keystore_base64.txt` ke secret `RELEASE_KEYSTORE_BASE64`.

4. **Buat tag dan push**:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

   GitHub Actions akan otomatis build release APK dan membuat GitHub Release dengan file APK terlampir.

## Cara Build Lokal

1. Clone repository
2. Buka di **Android Studio** (Hedgehog atau yang lebih baru)
3. Tunggu Gradle sync selesai
4. Run → Build APK

```bash
# Atau via command line:
chmod +x gradlew
./gradlew assembleDebug
```

Output APK: `app/build/outputs/apk/debug/app-debug.apk`

## Struktur Project

```
complaint-logbook/
├── .github/workflows/
│   ├── build.yml                 (CI: debug APK + lint)
│   └── release.yml               (CD: release APK + GitHub Release)
├── app/src/main/
│   ├── java/.../complaintlogbook/
│   │   ├── ComplaintApplication.kt
│   │   ├── MainActivity.kt
│   │   ├── data/local/           (Entity, DAO, Database, Tuple)
│   │   ├── data/repository/      (ComplaintRepository)
│   │   └── ui/
│   │       ├── viewmodel/        (ComplaintViewModel + UiState)
│   │       ├── screens/          (MainScreen, ItemCard, AddSheet)
│   │       ├── components/       (CalendarGrid)
│   │       └── theme/            (Theme)
│   └── res/                      (strings, themes, mipmap, drawable)
├── gradle/wrapper/               (Gradle 8.11.1)
├── build.gradle.kts              (Root plugins)
└── app/build.gradle.kts          (App config + signing)
```
