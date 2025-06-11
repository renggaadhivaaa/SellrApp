Sellr - Aplikasi E-Commerce Makanan Sederhana
Sellr adalah aplikasi e-commerce sederhana untuk platform Android yang dirancang sebagai studi kasus dalam membangun aplikasi belanja online. Aplikasi ini memungkinkan pengguna untuk mendaftar, login, melihat produk makanan, menambahkannya ke keranjang, dan menyelesaikan transaksi. Seluruh data transaksi disimpan secara real-time menggunakan Firebase.


✨ Fitur Utama
		Aplikasi ini dilengkapi dengan fitur-fitur esensial sebagai berikut:

🔐 Autentikasi Pengguna:

Pendaftaran (Sign Up) menggunakan Email & Password.
Login pengguna dengan kredensial yang sudah terdaftar.
Sistem sesi yang menjaga pengguna tetap login.
Integrasi penuh dengan Firebase Authentication.

🛍️ Katalog & Keranjang Belanja:

Menampilkan daftar produk makanan yang tersedia (di-hardcode di dalam aplikasi).
Menambahkan produk ke keranjang belanja.
Mengubah kuantitas produk di dalam keranjang (+ / -).
Keranjang belanja disimpan per pengguna di Firebase Realtime Database.

💳 Proses Transaksi & Riwayat:

Fitur Checkout untuk memproses semua item di keranjang.
Konfirmasi sebelum menyelesaikan pesanan.
Membuat catatan riwayat pesanan setelah transaksi berhasil.
Riwayat pesanan disimpan secara permanen di Firebase Realtime Database.
Tampilan khusus untuk riwayat pesanan yang telah selesai.

📱 Antarmuka Pengguna (UI):

Desain modern menggunakan Material Design 3.
Tampilan yang bersih dan responsif.
Empty state yang informatif (misalnya, saat keranjang atau riwayat kosong).
Pemuatan gambar dari URL secara asynchronous menggunakan Glide.

🛠️ Teknologi & Library yang Digunakan
Proyek ini dibangun menggunakan teknologi dan library modern untuk pengembangan Android:

Bahasa Pemrograman: Kotlin
Arsitektur & Pola Desain:
	ViewBinding: Untuk mengakses view secara aman dan efisien.
	Pola Adapter untuk RecyclerView.

Firebase Suite:
	Firebase Authentication: Untuk manajemen pengguna.
	Firebase Realtime Database: Untuk menyimpan data keranjang dan riwayat pesanan.

Library Pihak Ketiga:
	Glide: Untuk memuat dan meng-cache gambar.

Android Material Components: Untuk komponen UI modern.

Android Jetpack:
	RecyclerView & CardView untuk menampilkan daftar.
	AppCompat & ConstraintLayout untuk layout dan kompatibilitas.

🚀 Setup & Instalasi
Untuk menjalankan proyek ini di komputer Anda, ikuti langkah-langkah berikut:

1. Prasyarat
Android Studio (versi terbaru direkomendasikan).

Akun Google untuk mengakses Firebase.

2. Kloning Repositori
git clone https://github.com/NAMA_PENGGUNA_ANDA/NAMA_REPOSITORI_ANDA.git
cd NAMA_REPOSITORI_ANDA

3. Konfigurasi Firebase
Aplikasi ini membutuhkan konfigurasi Firebase agar dapat berfungsi.

Buka Firebase Console.

Buat proyek Firebase baru.

Tambahkan aplikasi Android ke proyek Firebase Anda dengan applicationId: com.example.sellr.

Aktifkan Layanan:

Buka Authentication -> Sign-in method -> Aktifkan provider Email/Password.

Buka Realtime Database -> Create Database.

Pilih lokasi server (misalnya, asia-southeast1). Penting: Catat URL database Anda.

Mulai dalam mode terkunci (locked mode).

Perbarui Aturan Database (Rules):

Di tab Rules pada Realtime Database, ganti aturan default dengan aturan berikut untuk mengizinkan pengguna yang terautentikasi untuk membaca dan menulis datanya sendiri:

{
  "rules": {
    "carts": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    },
    "orders": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }
}

Tambahkan google-services.json:

Unduh file google-services.json dari pengaturan proyek Firebase Anda.

Salin file tersebut ke dalam direktori app/ di proyek Android Studio Anda.

Perbarui URL Database (PENTING):

Buka file-file berikut: MainActivity.kt, CartActivity.kt, dan HistoryActivity.kt.

Cari baris inisialisasi database:

private val database = FirebaseDatabase.getInstance("URL_DATABASE_ANDA").reference

Ganti "URL_DATABASE_ANDA" dengan URL Realtime Database Anda yang sebenarnya (misalnya, https://sellr-9c516-default-rtdb.asia-southeast1.firebasedatabase.app). Ini sangat penting karena database Anda berada di region tertentu.

4. Build & Jalankan
Buka proyek di Android Studio.

Biarkan Gradle menyelesaikan proses sync.

Jalankan aplikasi di emulator atau perangkat fisik.

📝 Catatan Penting
Daftar Produk: Untuk kemudahan, daftar produk pada aplikasi ini di-hardcode (disimpan langsung dalam kode) di dalam file MainActivity.kt. Aplikasi tidak mengambil daftar produk dari database.

Penggunaan Database: Firebase Realtime Database hanya digunakan untuk menyimpan data yang bersifat dinamis dan transaksional, yaitu keranjang belanja (carts) dan riwayat pesanan (orders).

URL Gambar: Gambar produk dimuat dari URL publik di internet. URL ini mungkin bisa menjadi tidak aktif di masa mendatang. Untuk aplikasi produksi, disarankan untuk mengunggah gambar ke layanan penyimpanan seperti Firebase Storage.

🤝 Kontribusi
Kontribusi dalam bentuk pull request, isu, atau ide fitur sangat diterima. Jangan ragu untuk membuat fork dari proyek ini dan melakukan eksperimen.

📜 Lisensi
Proyek ini dilisensikan di bawah MIT License. Lihat file LICENSE untuk detail lebih lanjut.
