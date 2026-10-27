package com.example.sikeren.model

data class UserProfile(
    val id: Int = 32,
    val niplama: String = "340019879",
    val nipbaru: String = "197002052007011003",
    val namaLengkap: String = "Yunus Samuel Tandi Bua",
    val username: String = "yunus.samuel",
    val email: String = "yunus.samuel@bps.go.id",
    val foto: String? = null,
    val jabatan: String = "Staf Bagian Umum",
    val organisasi: String = "Bagian Umum",
    val idSatker: String = "7400",
    val role: String = "admin_pst",
    val isKetuaTim: Boolean = true,
    val isAdminPst: Boolean = true,
    val isPimpinan: Boolean = false
)

data class KegiatanItem(
    val id: Int,
    val judul: String,
    val agenda: String,
    val tempat: String,
    val pemimpin: String,
    val notulis: String,
    val jenis: String, // "Rapat" or "Kegiatan"
    val nomor: String? = null,
    val startDate: String,
    val dateAkhir: String,
    val startJam: String,
    val endJam: String,
    val jamDisplay: String,
    val tanggalDisplay: String,
    val hari: String,
    val status: String,
    val isToday: Boolean,
    val isUpcoming: Boolean,
    val statusRuangan: String = "Digunakan"
)

data class KegiatanStats(
    val totalAll: Int = 0,
    val totalToday: Int = 0,
    val totalUpcoming: Int = 0,
    val totalRapat: Int = 0,
    val totalKegiatan: Int = 0
)

data class RuanganStatusItem(
    val id: Int,
    val nama: String,
    val lokasi: String,
    val kapasitas: Int,
    val fasilitas: String,
    val isInUse: Boolean,
    val statusText: String,
    val activeAgenda: String,
    val activeTime: String,
    val organizer: String
)

data class ZoomStatusItem(
    val id: String,
    val nama: String,
    val kapasitas: Int,
    val isInUse: Boolean,
    val statusText: String,
    val activeAgenda: String,
    val activeTime: String
)

data class PetugasKontakItem(
    val id: Int,
    val grup: String, // "satpam", "cs", "pst", "ppid"
    val nama: String,
    val niplama: String = "",
    val sesi: Int = 1,
    val jam: String = "",
    val status: String = "Bertugas",
    val keterangan: String = "",
    val noHp: String = ""
)

data class UndanganRapatItem(
    val id: Int,
    val judul: String,
    val nomorSurat: String,
    val agenda: String,
    val tempat: String,
    val pemimpin: String,
    val notulis: String = "-",
    val tanggalDisplay: String,
    val jamDisplay: String,
    val suratUrl: String? = null,
    val statusKehadiran: String = "Belum Konfirmasi",
    val isToday: Boolean = false
)

data class PresensiKegiatanItem(
    val id: Int,
    val judul: String,
    val agenda: String,
    val tempat: String,
    val jenis: String = "Kegiatan",
    val jam: String,
    val statusKehadiran: String = "Belum Hadir", // "Belum Hadir", "Hadir", "Berhalangan"
    val waktuPresensi: String? = null,
    val keterangan: String? = null,
    val canCheckin: Boolean = true,
    val canExcuse: Boolean = true
)

data class PresensiStatus(
    val code: Int = 0, // 0 = Belum Hadir, 1 = Masuk, 2 = Keluar (P), 3 = Kembali, 4 = Pulang, 5 = Dinas
    val label: String = "Belum Hadir",
    val jam: String? = null,
    val hasCheckedIn: Boolean = false,
    val checkInTime: String? = null
)

data class NotificationItem(
    val id: Int,
    val judul: String,
    val pesan: String,
    val link: String? = null,
    val tipe: String? = null, // "penugasan_ketua_tim", "tugas_kegiatan", "sub_kegiatan", "ruangan_release"
    val kegiatanNama: String? = null,
    val subKegiatan: String? = null,
    val isRead: Boolean = false,
    val waktu: String = "Baru saja"
)

data class PstScheduleItem(
    val id: Int,
    val tanggal: String,
    val sesi: Int,
    val sesiLabel: String,
    val niplama: String,
    val namaPetugas: String,
    val statusKehadiran: String, // 'Terjadwal', 'Hadir', 'Berhalangan', 'Digantikan'
    val alasanBerhalangan: String? = null,
    val statusTukar: String = "none",
    val penggantiNiplama: String? = null,
    val penggantiNama: String? = null
)

data class PstDailyData(
    val todayDate: String = "",
    val currentSesi: Int = 1,
    val activeGuardian: String = "Petugas PST",
    val activeGuardianSesi: String = "Sesi 1 (08:00 - 12:00 WITA)",
    val isReplacement: Boolean = false,
    val mySchedule: PstScheduleItem? = null,
    val schedules: List<PstScheduleItem> = emptyList(),
    val replacementRequests: List<PstScheduleItem> = emptyList()
)

data class DailySummary(
    val todayDate: String = "Selasa, 27 Oktober 2026",
    val presensi: PresensiStatus = PresensiStatus(),
    val unreadNotifications: Int = 0,
    val kegiatanTodayCount: Int = 0,
    val kegiatanToday: List<KegiatanItem> = emptyList(),
    val rapatList: List<KegiatanItem> = emptyList(),
    val pstData: PstDailyData = PstDailyData(),
    val ruanganList: List<RuanganStatusItem> = emptyList(),
    val zoomList: List<ZoomStatusItem> = emptyList(),
    val satpamList: List<PetugasKontakItem> = emptyList(),
    val csList: List<PetugasKontakItem> = emptyList(),
    val ppidList: List<PetugasKontakItem> = emptyList(),
    val undanganRapatList: List<UndanganRapatItem> = emptyList(),
    val presensiKegiatanList: List<PresensiKegiatanItem> = emptyList(),
    val isKetuaTim: Boolean = false
)

data class OfficeLocation(
    val name: String = "BPS Provinsi Sulawesi Tenggara",
    val address: String = "Jl. Dr. Soetomo No. 1, Dapu-Dapura, Kendari",
    val distance: String = "15m dari kantor",
    val status: String = "Dalam Radius Kantor",
    val workingHours: String = "07:30 - 16:00 WITA"
)
