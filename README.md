# 📱 Voravio POS — Mobile Point of Sale & Smart Retail Management
**Versi 2.0 (Stable Release)**

Voravio POS adalah aplikasi kasir pintar (*Point of Sale*) dan manajemen ritel/gudang enterprise yang dibangun secara native menggunakan **Kotlin** dan **Jetpack Compose (Material 3)**. Didesain untuk kecepatan, keandalan luring (*offline-first*), serta performa tinggi bahkan pada perangkat Android berspesifikasi rendah (*low-end devices*).

---

## 🚀 Apa yang Baru di Versi 2.0 (Release Notes)

### 1. ⚡ Arsitektur Dual-LruCache untuk Perangkat Low-End
- **ProductDataCache**: Memori cache instan berbasis LRU untuk produk, kode SKU, dan barcode. Pemindaian barcode kini memiliki latensi **0 ms** tanpa beban disk/database SQLite.
- **InventoryImageCache**: Penguraian gambar produk dengan konfigurasi `Bitmap.Config.RGB_565` (menghemat alokasi RAM hingga 50% dibanding format standar `ARGB_8888`) serta *downsampling* otomatis.
- **Dukungan TrimMemory & LowMemory**: Otomatis membersihkan cache saat sistem operasi Android mendeteksi keterbatasan RAM, mencegah terjadinya *OutOfMemoryError* (OOM).

### 2. 🏷️ Otomatisasi Versi Berbasis Git Tag & CI/CD
- Penomoran `versionCode` dan `versionName` kini dihitung otomatis dari Git Tag (contoh: `v2.0`, `v2.1.0`) dan nomor antrean commit/pipeline (`GITHUB_RUN_NUMBER`).
- Sepenuhnya kompatibel dengan Gradle 9+ Configuration Cache.

### 3. 🛡️ CI/CD Pipeline & Build Fail-Safe
- **Auto-signing Keystore Fallback**: Build rilis pada GitHub Actions tidak akan pernah gagal lagi saat variabel kredensial rilis belum diatur; sistem otomatis beralih ke kunci debug yang valid untuk menghasilkan APK siap pakai.
- **Distribusi APK Otomatis**: Setiap commit/push ke branch `main` atau tag rilis otomatis menghasilkan dan mengunggah APK di tab **GitHub Actions Artifacts** dan **GitHub Releases**.

---

## 🌟 Fitur Utama Voravio POS

### 🛒 1. Kasir & Transaksi Cepat (*Point of Sale*)
- **Katalog Visual & Grid Cepat**: Tampilan produk dengan kategori dinamis, ikon warna, dan gambar pratinjau responsif.
- **Multi-Metode Pembayaran**:
  - 💵 **Tunai**: Kalkulator kembalian otomatis dengan tombol nominal cepat.
  - 📱 **QRIS Dinamis / Statis**: Dukungan pembayaran digital langsung.
  - 💳 **Transfer Bank / Kartu Debit**.
  - 🤝 **Utang / Bon Pelanggan (Tempo)**: Terintegrasi langsung dengan buku kasbon CRM.
  - 🔀 **Split Bill / Bayar Terpisah**: Fleksibilitas pembayaran ganda dalam satu transaksi.
- **Keranjang Belanja Pintar**: Dukungan diskon per item, diskon global, penyesuaian pajak (PPN), dan catatan khusus per pesanan.
- **Simpan Draf Pesanan**: Fitur *Hold Order* untuk menunda transaksi saat pelanggan masih berbelanja.

### 📷 2. Barcode Scanner & Generator Dinamis
- **Scanner Kamera Real-Time**: Terintegrasi langsung dengan CameraX dan Google ML Kit/ZXing untuk pemindaian cepat dari kamera ponsel.
- **Dukungan Scanner Fisik (USB / Bluetooth Laser / OTG)**: Menangkap input pemindai barcode eksternal secara instan.
- **Generator Barcode Mandiri**: Buat dan cetak barcode atau kode QR produk (format EAN-13, Code 128, QR Code) langsung dari aplikasi.

### 📦 3. Manajemen Inventaris & Stok Opname
- **Pelacakan Stok Real-Time**: Pengurangan stok otomatis seketika saat transaksi checkout selesai.
- **Peringatan Stok Menipis (*Low Stock Alert*)**: Penanda visual saat jumlah barang mencapai batas minimum.
- **Penyesuaian Stok & Opname**: Pencatatan riwayat penyesuaian stok (masuk, keluar, rusak, hilang, kedaluwarsa) lengkap dengan nama petugas dan alasan.
- **Purchase Order (PO) & Supplier**: Buat pesanan pembelian ke pemasok dan terima barang langsung ke stok toko.

### 👥 4. CRM, Pelanggan & Buku Utang (Kasbon)
- **Database Pelanggan**: Catat nomor telepon, alamat, riwayat transaksi, dan total belanja pelanggan.
- **Sistem Poin Loyalitas**: Akumulasi poin belanja otomatis dan penukaran diskon langsung di meja kasir.
- **Buku Catatan Utang / Tempo**: Pantau saldo piutang pelanggan, jatuh tempo, serta catat cicilan atau pelunasan dengan struk bukti pembayaran.

### ⏱️ 5. Manajemen Shift Kasir & Rekonsiliasi Kas
- **Buka & Tutup Shift**: Catat modal kas awal (*cash float*) dan verifikasi uang fisik di laci saat pergantian jam kerja.
- **Rekonsiliasi Kas Otomatis**: Mendeteksi otomatis selisih kas (surplus / defisit) antara data sistem dan hitungan fisik.
- **Role & Hak Akses Pengguna**: Manajemen staf berbasis PIN (Owner, Manajer, Kasir).

### 🧾 6. Cetak Struk Thermal & Laporan Finansial
- **Thermal Receipt Printer**: Kompatibel dengan printer Bluetooth ukuran 58mm dan 80mm standar ESC/POS.
- **Simulator Struk Digital**: Pratinjau struk sebelum dicetak atau kirim struk digital ke pelanggan via WhatsApp / Email.
- **Ekspor Data Laporan**: Ekspor riwayat transaksi penjualan, laba-rugi, dan log mutasi stok ke format **CSV / Excel** untuk pembukuan.

---

## 🛠️ Arsitektur & Teknologi

| Komponen | Teknologi yang Digunakan |
|---|---|
| **Bahasa** | Kotlin 2.2+ |
| **UI Framework** | Jetpack Compose (Material Design 3) |
| **Penyimpanan Lokal** | Room Database (SQLite) dengan skema reaktif Kotlin Flow |
| **Caching Layer** | Dynamic In-Memory `LruCache` (Bitmap & Entitas Produk) |
| **Kamera & Pemindai** | CameraX & ZXing Android Embedded |
| **Pemuat Gambar** | Coil 2.7+ dengan konfigurasi hemat memori (`RGB_565`) |
| **Asynchronous** | Kotlin Coroutines & StateFlow |
| **Pengujian** | JUnit 4, Robolectric 4.16+ |
| **Sistem Build** | Gradle Kotlin DSL (`.gradle.kts`) dengan AGP 9.1+ |

---

## 📲 Panduan Instalasi & Build

### Mengunduh APK Siap Pakai dari GitHub
1. Buka halaman repositori GitHub proyek Anda.
2. Masuk ke tab **Actions**.
3. Klik riwayat *workflow run* terbaru bernama **Build & Release Voravio APK**.
4. Gulir ke bagian **Artifacts** di bawah dan klik **Voravio-POS-v2.0-APK** untuk mengunduh file `.apk`.
5. Salin ke ponsel Android Anda dan install (*Izinkan instalasi dari sumber tidak dikenal* jika diminta).

### Menjalankan secara Lokal di Android Studio
1. Buka [Android Studio](https://developer.android.com/studio) (versi Ladybug / Iguana atau yang lebih baru).
2. Pilih **File > Open**, lalu pilih folder repositori ini.
3. Tunggu hingga proses *Gradle Sync* selesai.
4. Hubungkan perangkat Android via USB atau jalankan Android Emulator.
5. Klik tombol **Run (Shift + F10)**.

### Membangun File APK Mandiri via Terminal
```bash
# Build APK Debug
./gradlew assembleDebug

# Build APK Release (Otomatis menggunakan fallback debug key jika tanpa upload key)
./gradlew assembleRelease
```
File APK yang dihasilkan akan berada di:
`app/build/outputs/apk/debug/` atau `app/build/outputs/apk/release/`.

---

## 📄 Lisensi
Hak Cipta © 2026 Voravio POS. Dikembangkan untuk efisiensi bisnis ritel dan kemudahan operasional UMKM modern.
