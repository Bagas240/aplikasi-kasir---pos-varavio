#Voravio POS — Mobile Point of Sale & Smart Retail Management
**Versi 2.0 (Stable Release)**

Voravio POS adalah aplikasi kasir pintar (*Point of Sale*) dan manajemen ritel/gudang enterprise yang dibangun secara native menggunakan **Kotlin** dan **Jetpack Compose (Material 3)**. Didesain untuk kecepatan, keandalan luring (*offline-first*), serta performa tinggi bahkan pada perangkat Android berspesifikasi rendah (*low-end devices*).

##Fitur Utama Voravio POS

###1. Kasir & Transaksi Cepat (*Point of Sale*)
- **Katalog Visual & Grid Cepat**: Tampilan produk dengan kategori dinamis, ikon warna, dan gambar pratinjau responsif.
- **Multi-Metode Pembayaran**:
  - **Tunai**: Kalkulator kembalian otomatis dengan tombol nominal cepat.
  - **QRIS Dinamis / Statis**: Dukungan pembayaran digital langsung.
  - **Transfer Bank / Kartu Debit**.
  - **Utang / Bon Pelanggan (Tempo)**: Terintegrasi langsung dengan buku kasbon CRM.
  - **Split Bill / Bayar Terpisah**: Fleksibilitas pembayaran ganda dalam satu transaksi.
- **Keranjang Belanja Pintar**: Dukungan diskon per item, diskon global, penyesuaian pajak (PPN), dan catatan khusus per pesanan.
- **Simpan Draf Pesanan**: Fitur *Hold Order* untuk menunda transaksi saat pelanggan masih berbelanja.

### 2. Barcode Scanner & Generator Dinamis
- **Scanner Kamera Real-Time**: Terintegrasi langsung dengan CameraX dan Google ML Kit/ZXing untuk pemindaian cepat dari kamera ponsel.
- **Dukungan Scanner Fisik (USB / Bluetooth Laser / OTG)**: Menangkap input pemindai barcode eksternal secara instan.
- **Generator Barcode Mandiri**: Buat dan cetak barcode atau kode QR produk (format EAN-13, Code 128, QR Code) langsung dari aplikasi.

### 3. Manajemen Inventaris & Stok Opname
- **Pelacakan Stok Real-Time**: Pengurangan stok otomatis seketika saat transaksi checkout selesai.
- **Peringatan Stok Menipis (*Low Stock Alert*)**: Penanda visual saat jumlah barang mencapai batas minimum.
- **Penyesuaian Stok & Opname**: Pencatatan riwayat penyesuaian stok (masuk, keluar, rusak, hilang, kedaluwarsa) lengkap dengan nama petugas dan alasan.
- **Purchase Order (PO) & Supplier**: Buat pesanan pembelian ke pemasok dan terima barang langsung ke stok toko.

### 4. CRM, Pelanggan & Buku Utang (Kasbon)
- **Database Pelanggan**: Catat nomor telepon, alamat, riwayat transaksi, dan total belanja pelanggan.
- **Sistem Poin Loyalitas**: Akumulasi poin belanja otomatis dan penukaran diskon langsung di meja kasir.
- **Buku Catatan Utang / Tempo**: Pantau saldo piutang pelanggan, jatuh tempo, serta catat cicilan atau pelunasan dengan struk bukti pembayaran.

### 5. Manajemen Shift Kasir & Rekonsiliasi Kas
- **Buka & Tutup Shift**: Catat modal kas awal (*cash float*) dan verifikasi uang fisik di laci saat pergantian jam kerja.
- **Rekonsiliasi Kas Otomatis**: Mendeteksi otomatis selisih kas (surplus / defisit) antara data sistem dan hitungan fisik.
- **Role & Hak Akses Pengguna**: Manajemen staf berbasis PIN (Owner, Manajer, Kasir).

### 6. Cetak Struk Thermal & Laporan Finansial
- **Thermal Receipt Printer**: Kompatibel dengan printer Bluetooth ukuran 58mm dan 80mm standar ESC/POS.
- **Simulator Struk Digital**: Pratinjau struk sebelum dicetak atau kirim struk digital ke pelanggan via WhatsApp / Email.
- **Ekspor Data Laporan**: Ekspor riwayat transaksi penjualan, laba-rugi, dan log mutasi stok ke format **CSV / Excel** untuk pembukuan.

---

## Arsitektur & Teknologi

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
