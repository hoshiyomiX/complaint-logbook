# Complaint Logbook

Aplikasi logbook komplain tamu berbasis Android — dicatat, dilacak, dan diselesaikan keluhan secara terorganisir.

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

## Cara Build

1. Clone repository
2. Buka di **Android Studio** (Hedgehog atau yang lebih baru)
3. Tunggu Gradle sync selesai
4. Run → Build APK

```bash
# Atau via command line:
./gradlew assembleDebug
```

Output APK: `app/build/outputs/apk/debug/app-debug.apk`

## Struktur Project

```
app/src/main/java/com/hoshiyomix/complaintlogbook/
├── ComplaintApplication.kt       (Application class, DI root)
├── MainActivity.kt
├── data/
│   ├── local/
│   │   ├── ComplaintEntity.kt    (Room entity)
│   │   ├── DateMarkerTuple.kt    (Room projection)
│   │   ├── ComplaintDao.kt       (Room DAO)
│   │   └── AppDatabase.kt        (Room database)
│   └── repository/
│       └── ComplaintRepository.kt
└── ui/
    ├── theme/Theme.kt
    ├── viewmodel/ComplaintViewModel.kt
    ├── screens/
    │   ├── MainScreen.kt
    │   ├── ComplaintItemCard.kt
    │   └── AddComplaintSheet.kt
    └── components/
        └── CalendarGrid.kt
```
