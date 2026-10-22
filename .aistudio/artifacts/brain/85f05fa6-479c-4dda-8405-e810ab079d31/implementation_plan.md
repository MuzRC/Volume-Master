# Volume Master Pro (Pengawal Kelantangan Telefon)

Aplikasi Android moden, berprestasi tinggi dan elegan untuk mengawal kesemua saluran audio telefon pintar (Media, Deringan, Notifikasi, Penggera, dan Panggilan) dengan meter audio dinamik, mod pantas satu sentuhan, dan profil pratetap tersuai.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> Berdasarkan jawapan anda dalam fasa penjelasasan:
> - **Saluran Audio**: Mengawal kesemua 5 saluran utama — Media, Nada Dering (Ring), Notifikasi, Penggera (Alarm), dan Panggilan Suara (Voice Call).
> - **Ciri Profil**: Dilengkapi dengan profil pratetap pantas satu sentuhan (Senyap, Getar, Pejabat/Mesyuarat, Normal, Luaran/Maksimum) serta sokongan mencipta dan menyimpan profil tersuai pengguna.
> - **Gaya Visual & Tema**: Tema gelap moden (Sleek Dark Theme) dengan aksen warna bertenaga (Electric Cyan & Neon Emerald), meter tahap kelantangan dinamik bergaya visualizer, dan slider interaktif responsif.
> - **Sinkronisasi Masa Nyata**: Memantau perubahan butang fizikal kelantangan telefon secara langsung menggunakan `BroadcastReceiver` (`android.media.VOLUME_CHANGED_ACTION`).

---

## 1. Overview & Core Concept

- **Fungsi Utama**: Menyediakan pusat kawalan audio setempat untuk peranti Android yang membolehkan pengguna melihat dan melaras tahap kelantangan setiap saluran secara individu tanpa perlu masuk ke tetapan sistem yang rumit.
- **Kelebihan Utama**:
  - Peluncur kelantangan berasingan bagi setiap saluran dengan peratusan tepat dan penunjuk had minimum/maksimum.
  - Mod profil pantas satu sentuhan bagi penukaran situasi harian (cth: masuk mesyuarat, waktu tidur, atau tempat bising).
  - Butang *Uji Bunyi* (Sound Preview) untuk mendengar aras kelantangan serta-merta tanpa perlu memainkan lagu luaran.
  - Meter visualizer audio dinamik yang memberikan maklum balas visual interaktif semasa pelarasan.
  - Pengurusan profil tersuai (simpan, padam, dan guna semula profil kegemaran).

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Paparan Utama (Master Hub)**:
   - Kad status induk menunjukkan mod deringan semasa (Normal, Getar, Senyap).
   - Baris butang profil pantas (*Quick Profiles*): Senyap, Getar, Pejabat, Normal, Maksimum.
   - Kad-kad saluran audio individu:
     - **Media / Muzik** (Ikon muzik, peluncur kelantangan, peratusan, butang mute pantas, butang uji nada).
     - **Nada Dering** (Ikon panggilan telefon, peluncur, penunjuk getar/dering).
     - **Notifikasi** (Ikon loceng, peluncur, penunjuk senyap).
     - **Penggera** (Ikon jam loceng, peluncur keselamatan waktu bangun tidur).
     - **Panggilan Suara** (Ikon corong telefon, peluncur audio pendengaran panggilan).
2. **Tab Profil Pratetap (Presets & Custom Profiles)**:
   - Senarai profil sedia ada (Malam Senyap, Kerja/Mesyuarat, Harian Seimbang, Hiburan, Di Luar).
   - Borang dialog mudah "Cipta Profil Baharu" untuk merakam tetapan aras semasa menjadi profil peribadi.
3. **Maklum Balas Semasa**:
   - Apabila pengguna menekan butang kelantangan fizikal pada telefon, meter dan slider dalam aplikasi dikemas kini secara automatik dalam masa nyata.

### Visual Identity & Theme
- **Gaya Estetik**: *Modern Sleek Cyber-Dark Dashboard* dengan kemasan Material 3, kontras tinggi, dan kad permukaan bergradien halus.
- **Palet Warna**:
  - Latar Belakang: `#0B0F19` (Deep Obsidian Void)
  - Permukaan Kad: `#131B2E` dengan sempadan halus `#1E293B`
  - Aksen Media: `#00E5FF` (Electric Cyan)
  - Aksen Deringan & Panggilan: `#6366F1` (Indigo Glow)
  - Aksen Notifikasi: `#38BDF8` (Sky Blue)
  - Aksen Penggera: `#F59E0B` (Vibrant Amber)
  - Status/Normal: `#10B981` (Emerald Glow)
- **Tipografi**: Susunan hierarki bersih dengan angka peratusan tebal dan label saluran yang jelas.
- **Interaktiviti**: Maklum balas haptik semasa menyentuh nilai ekstrem (0% atau 100%), peluncur sentuhan lancar 48dp, dan meter aras berbentuk bar visualizer.

---

## 3. Key Product Decisions & Trade-Offs

- **Penggunaan Android `AudioManager` Sebenar**:
  - *Keputusan*: Berinteraksi terus dengan subsistem audio perkakasan Android melalui `Context.AUDIO_SERVICE`, bukan simulasi palsu.
  - *Rasional*: Memastikan kelantangan telefon pengguna benar-benar berubah di peringkat sistem operasi.
- **Sokongan DND (Do Not Disturb) & Kebenaran Akses**:
  - *Keputusan*: Memeriksa dan mengendalikan `NotificationManager.isNotificationPolicyAccessGranted()` dengan anggun. Jika sistem Android memerlukan kebenaran khas untuk meletakkan telefon dalam mod senyap sepenuhnya, aplikasi menyediakan panduan dan butang terus ke tetapan DND sistem tanpa mogok (*crash-free*).
- **Penyimpanan Profil Tersuai Menggunakan Local Cache & Preferences**:
  - *Keputusan*: Menyimpan profil pratetap audio pengguna secara tempatan dalam peranti secara persisten dan ringan.
- **Sedia untuk GitHub**:
  - *Keputusan*: Kod distrukturkan secara bersih mengikut piawaian moden Jetpack Compose dan MVVM, sedia untuk dieksport dan ditolak (*push*) ke repositori GitHub pengguna.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                      Jetpack Compose UI                      │
│                                                              │
│  ┌─────────────────────────┐   ┌──────────────────────────┐  │
│  │   Master Profile Bar    │   │ Dynamic Level Visualizer │  │
│  └────────────┬────────────┘   └─────────────┬────────────┘  │
│               │                              │               │
│  ┌────────────┴──────────────────────────────┴────────────┐  │
│  │   Stream Volume Cards (Media, Ring, Notif, Alarm, Call)│  │
│  └────────────────────────────┬───────────────────────────┘  │
└───────────────────────────────┼──────────────────────────────┘
                                │ State & Events
                                ▼
┌──────────────────────────────────────────────────────────────┐
│                    VolumeControlViewModel                    │
│                                                              │
│  - volumeUiState: StateFlow<VolumeState>                     │
│  - setStreamVolume(streamType, volume)                       │
│  - applyProfile(profile)                                     │
│  - testAudioStream(streamType)                               │
│  - saveCustomPreset(preset)                                  │
└───────────────────────┬──────────────┬───────────────────────┘
                        │              │
       AudioManager API │              │ Persistent Storage
                        ▼              ▼
┌───────────────────────────────┐ ┌────────────────────────────┐
│   Android System Services     │ │   VolumePresetRepository   │
│ - AudioManager                │ │ - SharedPreferences/Cache  │
│ - Volume BroadcastReceiver    │ │ - Default & Custom Presets │
│ - ToneGenerator / SoundPool   │ └────────────────────────────┘
└───────────────────────────────┘
```

### Components & Responsibilities
1. **`VolumeStreamType`**: Enum yang memetakan jenis saluran audio (`STREAM_MUSIC`, `STREAM_RING`, `STREAM_NOTIFICATION`, `STREAM_ALARM`, `STREAM_VOICE_CALL`) bersama ikon, julat, warna tema, dan nilai semasa.
2. **`VolumePreset`**: Data model untuk profil bunyi (nama, ikon, nilai setiap saluran, mod deringan).
3. **`VolumeBroadcastReceiver`**: Menerima intent `android.media.VOLUME_CHANGED_ACTION` untuk memastikan slider sentiasa segerak dengan butang fizikal.
4. **`TonePreviewPlayer`**: Memainkan nada ujian ringkas bagi setiap saluran audio supaya pengguna boleh mengesahkan kelantangan dengan segera.
5. **`VolumeSliderItem`**: Komponen Compose tersuai dengan meter aras visual, butang tambah/kurang, butang mute, dan penunjuk peratusan.
