package com.example.sikeren.network

import com.example.sikeren.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object ApiClient {
    var primaryHost: String = "club-postposted-flights-son.trycloudflare.com"
    var localHost: String = "192.168.50.49:8000"
    var activeHost: String = primaryHost

    val baseUrl: String
        get() {
            val host = activeHost.trim().removePrefix("http://").removePrefix("https://").trimEnd('/')
            val scheme = if (host.contains("trycloudflare.com") || host.startsWith("https")) "https" else "http"
            return "$scheme://$host/api/v1"
        }

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    suspend fun login(username: String, password: String): Result<Pair<UserProfile, String>> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("username", username)
                put("password", password)
            }
            val request = Request.Builder()
                .url("$baseUrl/login")
                .post(jsonBody.toString().toRequestBody(JSON_MEDIA))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(body)
                val token = json.optString("token", "")
                val userJson = json.getJSONObject("user")
                val role = userJson.optString("role", "admin_pst")
                val user = UserProfile(
                    id = userJson.optInt("id", 32),
                    niplama = userJson.optString("niplama", "340019879"),
                    nipbaru = userJson.optString("nipbaru", "197002052007011003"),
                    namaLengkap = userJson.optString("nama_lengkap", "Yunus Samuel Tandi Bua"),
                    username = userJson.optString("username", username),
                    email = userJson.optString("email", "$username@bps.go.id"),
                    foto = if (userJson.has("foto") && !userJson.isNull("foto")) userJson.getString("foto") else null,
                    jabatan = userJson.optString("jabatan", "Staf Bagian Umum"),
                    organisasi = userJson.optString("organisasi", "Bagian Umum"),
                    idSatker = userJson.optString("id_satker", "7400"),
                    role = role,
                    isAdminPst = userJson.optBoolean("is_admin_pst", role == "admin_pst"),
                    isPimpinan = userJson.optBoolean("is_pimpinan", role == "pimpinan")
                )
                Result.success(Pair(user, token))
            } else {
                Result.success(Pair(UserProfile(), "token-demo-7400"))
            }
        } catch (e: Exception) {
            Result.success(Pair(UserProfile(), "token-demo-7400"))
        }
    }

    suspend fun fetchDailySummary(token: String): DailySummary = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/daily-summary")
                .header("Authorization", "Bearer $token")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val json = JSONObject(body)
                val todayDate = json.optString("today_date", "Rabu, 21 Oktober 2026")
                val presensiJson = json.optJSONObject("presensi")
                val presensi = PresensiStatus(
                    code = if (presensiJson?.optBoolean("has_checked_in") == true) 1 else 0,
                    label = presensiJson?.optString("current_status", "Belum Hadir") ?: "Belum Hadir",
                    jam = if (presensiJson != null && presensiJson.has("latest_time") && !presensiJson.isNull("latest_time")) presensiJson.getString("latest_time") else null,
                    hasCheckedIn = presensiJson?.optBoolean("has_checked_in", false) ?: false,
                    checkInTime = if (presensiJson != null && presensiJson.has("check_in_time") && !presensiJson.isNull("check_in_time")) presensiJson.getString("check_in_time") else null
                )
                val unreadNotifs = json.optInt("unread_notifications", 3)
                val taskArray = json.optJSONArray("kegiatan_today") ?: JSONArray()
                val tasks = mutableListOf<KegiatanItem>()
                for (i in 0 until taskArray.length()) {
                    val t = taskArray.getJSONObject(i)
                    tasks.add(
                        KegiatanItem(
                            id = t.optInt("id", i + 1),
                            judul = t.optString("judul", "Kegiatan BPS"),
                            agenda = t.optString("agenda", "-"),
                            tempat = t.optString("tempat", "Ruang Rapat Utama BPS"),
                            pemimpin = t.optString("pemimpin", "-"),
                            notulis = t.optString("notulis", "-"),
                            jenis = t.optString("jenis", "Kegiatan"),
                            nomor = if (t.has("nomor") && !t.isNull("nomor")) t.getString("nomor") else null,
                            startDate = "2026-10-21",
                            dateAkhir = "2026-10-21",
                            startJam = t.optString("start_jam", "08:00"),
                            endJam = t.optString("end_jam", "Selesai"),
                            jamDisplay = "${t.optString("start_jam", "08:00")} - ${t.optString("end_jam", "Selesai")}",
                            tanggalDisplay = "21 Okt 2026",
                            hari = "Rabu",
                            status = "Hari Ini",
                            isToday = true,
                            isUpcoming = false
                        )
                    )
                }

                // Also fetch PST Data
                val pstData = fetchPstToday(token)

                // Filter Rapat
                val (allStats, allKegiatan) = fetchKegiatan(token, "all")
                val rapatItems = allKegiatan.filter { it.jenis.equals("Rapat", ignoreCase = true) }

                DailySummary(
                    todayDate = todayDate,
                    presensi = presensi,
                    unreadNotifications = unreadNotifs,
                    kegiatanTodayCount = tasks.size,
                    kegiatanToday = tasks,
                    rapatList = if (rapatItems.isNotEmpty()) rapatItems else getMockRapatList(),
                    pstData = pstData
                )
            } else {
                getMockDailySummary()
            }
        } catch (e: Exception) {
            getMockDailySummary()
        }
    }

    suspend fun fetchPstToday(token: String): PstDailyData = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/pst/today")
                .header("Authorization", "Bearer $token")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val json = JSONObject(body)
                val todayDate = json.optString("today_date", "Rabu, 21 Oktober 2026")
                val currentSesi = json.optInt("current_sesi", 1)

                val activeGuardJson = json.optJSONObject("active_guard")
                val activeGuardianName = activeGuardJson?.optString("petugas_nama", "Harningsih, SST") ?: "Harningsih, SST"
                val activeSesiLabel = activeGuardJson?.optString("sesi_label", "Sesi 1 (08:00 - 12:00 WITA)") ?: "Sesi 1"
                val isReplacement = activeGuardJson?.optBoolean("is_pengganti", false) ?: false

                val myScheduleJson = json.optJSONObject("my_schedule")
                val mySchedule = if (myScheduleJson != null) {
                    PstScheduleItem(
                        id = myScheduleJson.optInt("id", 1),
                        tanggal = todayDate,
                        sesi = myScheduleJson.optInt("sesi", 1),
                        sesiLabel = myScheduleJson.optString("sesi_label", "Sesi 1"),
                        niplama = "340019879",
                        namaPetugas = "Yunus Samuel Tandi Bua",
                        statusKehadiran = myScheduleJson.optString("status_kehadiran", "Terjadwal"),
                        alasanBerhalangan = myScheduleJson.optString("alasan", null),
                        statusTukar = myScheduleJson.optString("status_tukar", "none")
                    )
                } else null

                val schedulesArray = json.optJSONArray("schedules") ?: JSONArray()
                val schedulesList = mutableListOf<PstScheduleItem>()
                for (i in 0 until schedulesArray.length()) {
                    val s = schedulesArray.getJSONObject(i)
                    schedulesList.add(
                        PstScheduleItem(
                            id = s.optInt("id", i + 1),
                            tanggal = s.optString("tanggal", "2026-10-21"),
                            sesi = s.optInt("sesi", 1),
                            sesiLabel = s.optString("sesi_label", "Sesi ${s.optInt("sesi", 1)}"),
                            niplama = s.optString("niplama", ""),
                            namaPetugas = s.optString("nama_petugas", ""),
                            statusKehadiran = s.optString("status_kehadiran", "Terjadwal"),
                            alasanBerhalangan = if (s.has("alasan_berhalangan") && !s.isNull("alasan_berhalangan")) s.getString("alasan_berhalangan") else null,
                            statusTukar = s.optString("status_tukar", "none"),
                            penggantiNiplama = if (s.has("pengganti_niplama") && !s.isNull("pengganti_niplama")) s.getString("pengganti_niplama") else null,
                            penggantiNama = if (s.has("pengganti_nama") && !s.isNull("pengganti_nama")) s.getString("pengganti_nama") else null
                        )
                    )
                }

                val repRequestsArray = json.optJSONArray("replacement_requests") ?: JSONArray()
                val repList = mutableListOf<PstScheduleItem>()
                for (i in 0 until repRequestsArray.length()) {
                    val r = repRequestsArray.getJSONObject(i)
                    repList.add(
                        PstScheduleItem(
                            id = r.optInt("id", 0),
                            tanggal = todayDate,
                            sesi = r.optInt("sesi", 1),
                            sesiLabel = r.optString("sesi_label", "Sesi 1"),
                            niplama = r.optString("niplama", ""),
                            namaPetugas = r.optString("nama_petugas", ""),
                            statusKehadiran = "Berhalangan",
                            alasanBerhalangan = r.optString("alasan", "Izin keperluan mendesak"),
                            statusTukar = "requested"
                        )
                    )
                }

                PstDailyData(
                    todayDate = todayDate,
                    currentSesi = currentSesi,
                    activeGuardian = activeGuardianName,
                    activeGuardianSesi = activeSesiLabel,
                    isReplacement = isReplacement,
                    mySchedule = mySchedule,
                    schedules = schedulesList,
                    replacementRequests = repList
                )
            } else {
                getMockPstData()
            }
        } catch (e: Exception) {
            getMockPstData()
        }
    }

    suspend fun reportPstUnable(token: String, jadwalId: Int, alasan: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("jadwal_id", jadwalId)
                put("alasan", alasan)
            }
            val request = Request.Builder()
                .url("$baseUrl/pst/berhalangan")
                .header("Authorization", "Bearer $token")
                .post(json.toString().toRequestBody(JSON_MEDIA))
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            true // simulate success offline
        }
    }

    suspend fun claimPstReplacement(token: String, jadwalId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("jadwal_id", jadwalId)
            }
            val request = Request.Builder()
                .url("$baseUrl/pst/ambil-tukar")
                .header("Authorization", "Bearer $token")
                .post(json.toString().toRequestBody(JSON_MEDIA))
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            true
        }
    }

    suspend fun releaseRoom(token: String, taskId: Int?, namaRuangan: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                if (taskId != null) put("task_id", taskId)
                put("nama_ruangan", namaRuangan)
            }
            val request = Request.Builder()
                .url("$baseUrl/ruangan/lepas")
                .header("Authorization", "Bearer $token")
                .post(json.toString().toRequestBody(JSON_MEDIA))
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            true
        }
    }

    suspend fun fetchNotifications(token: String): List<NotificationItem> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/notifications")
                .header("Authorization", "Bearer $token")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val json = JSONObject(body)
                val data = json.optJSONArray("data") ?: JSONArray()
                val list = mutableListOf<NotificationItem>()
                for (i in 0 until data.length()) {
                    val n = data.getJSONObject(i)
                    list.add(
                        NotificationItem(
                            id = n.optInt("id", i + 1),
                            judul = n.optString("judul", "Notifikasi"),
                            pesan = n.optString("pesan", ""),
                            link = if (n.has("link") && !n.isNull("link")) n.getString("link") else null,
                            tipe = if (n.has("tipe") && !n.isNull("tipe")) n.getString("tipe") else null,
                            kegiatanNama = if (n.has("kegiatan_nama") && !n.isNull("kegiatan_nama")) n.getString("kegiatan_nama") else null,
                            subKegiatan = if (n.has("sub_kegiatan") && !n.isNull("sub_kegiatan")) n.getString("sub_kegiatan") else null,
                            isRead = n.optBoolean("is_read", false),
                            waktu = n.optString("waktu", "Baru saja")
                        )
                    )
                }
                list
            } else {
                getMockNotifications()
            }
        } catch (e: Exception) {
            getMockNotifications()
        }
    }

    suspend fun markNotificationRead(token: String, id: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/notifications/$id/read")
                .header("Authorization", "Bearer $token")
                .post("{}".toRequestBody(JSON_MEDIA))
                .build()
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            true
        }
    }

    suspend fun fetchKegiatan(token: String, type: String = "all", search: String? = null): Pair<KegiatanStats, List<KegiatanItem>> = withContext(Dispatchers.IO) {
        try {
            var url = "$baseUrl/kegiatan?type=$type"
            if (!search.isNullOrBlank()) {
                url += "&search=$search"
            }
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $token")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val json = JSONObject(body)
                val statsJson = json.optJSONObject("stats")
                val stats = KegiatanStats(
                    totalAll = statsJson?.optInt("total_all", 4) ?: 4,
                    totalToday = statsJson?.optInt("total_today", 2) ?: 2,
                    totalUpcoming = statsJson?.optInt("total_upcoming", 2) ?: 2,
                    totalRapat = statsJson?.optInt("total_rapat", 2) ?: 2,
                    totalKegiatan = statsJson?.optInt("total_kegiatan", 2) ?: 2
                )
                val dataArray = json.optJSONArray("data") ?: JSONArray()
                val list = mutableListOf<KegiatanItem>()
                for (i in 0 until dataArray.length()) {
                    val item = dataArray.getJSONObject(i)
                    list.add(
                        KegiatanItem(
                            id = item.optInt("id", i + 1),
                            judul = item.optString("judul", "Agenda"),
                            agenda = item.optString("agenda", "-"),
                            tempat = item.optString("tempat", "Ruang Vicon"),
                            pemimpin = item.optString("pemimpin", "-"),
                            notulis = item.optString("notulis", "-"),
                            jenis = item.optString("jenis", "Kegiatan"),
                            nomor = if (item.has("nomor") && !item.isNull("nomor")) item.getString("nomor") else null,
                            startDate = item.optString("start_date", ""),
                            dateAkhir = item.optString("date_akhir", ""),
                            startJam = item.optString("start_jam", "08:00"),
                            endJam = item.optString("end_jam", "Selesai"),
                            jamDisplay = item.optString("jam_display", "08:00 - Selesai"),
                            tanggalDisplay = item.optString("tanggal_display", "21 Okt 2026"),
                            hari = item.optString("hari", "Rabu"),
                            status = item.optString("status", "Aktif"),
                            isToday = item.optBoolean("is_today", false),
                            isUpcoming = item.optBoolean("is_upcoming", false)
                        )
                    )
                }
                Pair(stats, list)
            } else {
                getMockKegiatanList()
            }
        } catch (e: Exception) {
            getMockKegiatanList()
        }
    }

    suspend fun submitPresensi(token: String, status: Int, qrCode: String? = null, ketDinas: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("status", status)
                put("qr_code", qrCode ?: "BPS7400-SCAN-OK")
                if (!ketDinas.isNullOrBlank()) {
                    put("ket_dinas", ketDinas)
                }
            }
            val request = Request.Builder()
                .url("$baseUrl/presensi/scan")
                .header("Authorization", "Bearer $token")
                .post(json.toString().toRequestBody(JSON_MEDIA))
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            true // fallback success for offline test
        }
    }

    fun getMockPstData(): PstDailyData {
        return PstDailyData(
            todayDate = "Rabu, 21 Oktober 2026",
            currentSesi = 1,
            activeGuardian = "Harningsih, SST",
            activeGuardianSesi = "Sesi 1 (08:00 - 12:00 WITA)",
            isReplacement = false,
            mySchedule = PstScheduleItem(
                id = 99,
                tanggal = "2026-10-21",
                sesi = 2,
                sesiLabel = "Sesi 2 (12:00 - 15:30 WITA)",
                niplama = "340019879",
                namaPetugas = "Yunus Samuel Tandi Bua",
                statusKehadiran = "Terjadwal",
                alasanBerhalangan = null,
                statusTukar = "none"
            ),
            schedules = listOf(
                PstScheduleItem(1, "2026-10-21", 1, "Sesi 1 (08:00 - 12:00)", "340016230", "Harningsih, SST", "Hadir"),
                PstScheduleItem(2, "2026-10-21", 1, "Sesi 1 (08:00 - 12:00)", "340016400", "Rachmatiah Rachman, S.E.M.Si", "Terjadwal"),
                PstScheduleItem(3, "2026-10-21", 2, "Sesi 2 (12:00 - 15:30)", "340017046", "Burit Retnowati", "Berhalangan", "Tugas dinas lapangan mendadak", "requested"),
                PstScheduleItem(4, "2026-10-21", 2, "Sesi 2 (12:00 - 15:30)", "340019879", "Yunus Samuel Tandi Bua", "Terjadwal")
            ),
            replacementRequests = listOf(
                PstScheduleItem(
                    id = 3,
                    tanggal = "2026-10-21",
                    sesi = 2,
                    sesiLabel = "Sesi 2 (12:00 - 15:30 WITA)",
                    niplama = "340017046",
                    namaPetugas = "Burit Retnowati",
                    statusKehadiran = "Berhalangan",
                    alasanBerhalangan = "Tugas verifikasi data lapangan mendadak",
                    statusTukar = "requested"
                )
            )
        )
    }

    fun getMockRapatList(): List<KegiatanItem> {
        return listOf(
            KegiatanItem(
                id = 301,
                judul = "Rapat Koordinasi Evaluasi SAKIP BPS Sultra",
                agenda = "Tindak lanjut rekomendasi hasil evaluasi AKIP dan pemantauan capaian target kinerja IKU triwulan IV.",
                tempat = "Ruang Rapat Utama Lt. 2",
                pemimpin = "Kepala Bagian Umum",
                notulis = "Yunus Samuel Tandi Bua",
                jenis = "Rapat",
                nomor = "B-201/7400/OT.01/2026",
                startDate = "2026-10-21",
                dateAkhir = "2026-10-21",
                startJam = "09:00",
                endJam = "11:30",
                jamDisplay = "09:00 - 11:30 WITA",
                tanggalDisplay = "21 Okt 2026",
                hari = "Rabu",
                status = "Berlangsung",
                isToday = true,
                isUpcoming = false,
                statusRuangan = "Sedang Digunakan"
            ),
            KegiatanItem(
                id = 302,
                judul = "Rapat Pleno Penyusunan PDRB Triwulan IV",
                agenda = "Konsolidasi data neraca wilayah bersama dinas dan instansi terkait se-Sultra.",
                tempat = "Ruang Vicon Lt. 1",
                pemimpin = "Koordinator Fungsi Nerwilis",
                notulis = "Staf Neraca",
                jenis = "Rapat",
                nomor = "B-204/7400/NW.01/2026",
                startDate = "2026-10-23",
                dateAkhir = "2026-10-23",
                startJam = "13:30",
                endJam = "15:30",
                jamDisplay = "13:30 - 15:30 WITA",
                tanggalDisplay = "23 Okt 2026",
                hari = "Jumat",
                status = "Mendatang",
                isToday = false,
                isUpcoming = true,
                statusRuangan = "Terjadwal"
            )
        )
    }

    fun getMockNotifications(): List<NotificationItem> {
        return listOf(
            NotificationItem(
                id = 101,
                judul = "Permintaan Pengganti Jaga PST",
                pesan = "Rekan Burit Retnowati berhalangan hadir jaga PST Sesi 2 (12:00 - 15:30 WITA). Alasan: Tugas verifikasi data lapangan mendadak. Siapa rekan yang bersedia menggantikan?",
                tipe = "pst_replacement_request",
                kegiatanNama = "Pelayanan Statistik Terpadu (PST)",
                subKegiatan = "Jaga Frontline & Konsultasi Publik Sesi 2",
                isRead = false,
                waktu = "10 menit yang lalu"
            ),
            NotificationItem(
                id = 102,
                judul = "Pemberitahuan Pelepasan Ruangan",
                pesan = "Ruangan Aula Polinggona Lt. 3 telah selesai digunakan untuk Sosialisasi dan kini telah dilepas. Ruangan siap digunakan kembali.",
                tipe = "ruangan_lepas",
                kegiatanNama = "Manajemen Sarana & Ruangan Rapat",
                subKegiatan = "Pelepasan Ruang Aula Polinggona",
                isRead = false,
                waktu = "35 menit yang lalu"
            ),
            NotificationItem(
                id = 103,
                judul = "Penugasan Kegiatan Baru",
                pesan = "Anda ditugaskan sebagai Notulis pada Rapat Koordinasi Evaluasi SAKIP BPS Sultra.",
                tipe = "penugasan",
                kegiatanNama = "Evaluasi Kinerja & SAKIP 2026",
                subKegiatan = "Penyusunan Risalah Rapat & Rekomendasi",
                isRead = true,
                waktu = "2 jam yang lalu"
            )
        )
    }

    private fun getMockDailySummary(): DailySummary {
        val rapatList = getMockRapatList()
        val mockItems = listOf(
            rapatList[0],
            KegiatanItem(
                id = 242,
                judul = "Briefing Tim Pengelolaan BMN & Umum",
                agenda = "Pengecekan kesiapan sarana prasarana dan inventarisasi aset semester akhir.",
                tempat = "Ruang Bagian Umum",
                pemimpin = "Yunus Samuel Tandi Bua",
                notulis = "Staf Umum",
                jenis = "Kegiatan",
                nomor = "B-202/7400/KU.02/2026",
                startDate = "2026-10-21",
                dateAkhir = "2026-10-21",
                startJam = "13:30",
                endJam = "15:00",
                jamDisplay = "13:30 - 15:00 WITA",
                tanggalDisplay = "21 Okt 2026",
                hari = "Rabu",
                status = "Hari Ini",
                isToday = true,
                isUpcoming = false
            )
        )
        return DailySummary(
            todayDate = "Rabu, 21 Oktober 2026",
            presensi = PresensiStatus(
                code = 1,
                label = "Sudah Masuk",
                jam = "07:45",
                hasCheckedIn = true,
                checkInTime = "07:45"
            ),
            unreadNotifications = 2,
            kegiatanTodayCount = mockItems.size,
            kegiatanToday = mockItems,
            rapatList = rapatList,
            pstData = getMockPstData()
        )
    }

    private fun getMockKegiatanList(): Pair<KegiatanStats, List<KegiatanItem>> {
        val daily = getMockDailySummary().kegiatanToday
        val upcoming = listOf(
            KegiatanItem(
                id = 243,
                judul = "Sosialisasi Hasil Sensus Pertanian 2026",
                agenda = "Diseminasi indikator strategis sektor pertanian kepada stakeholder daerah.",
                tempat = "Grand Claro Hotel Kendari",
                pemimpin = "Koordinator Fungsi Statistik Produksi",
                notulis = "Tim Diseminasi",
                jenis = "Kegiatan",
                nomor = "B-203/7400/ST.03/2026",
                startDate = "2026-10-22",
                dateAkhir = "2026-10-22",
                startJam = "08:30",
                endJam = "16:00",
                jamDisplay = "08:30 - 16:00 WITA",
                tanggalDisplay = "22 Okt 2026",
                hari = "Kamis",
                status = "Mendatang",
                isToday = false,
                isUpcoming = true
            ),
            KegiatanItem(
                id = 244,
                judul = "Rapat Pleno Penyusunan PDRB Triwulan IV",
                agenda = "Konsolidasi data neraca wilayah dan rekonsiliasi sektoral.",
                tempat = "Ruang Vicon BPS Sultra",
                pemimpin = "Ketua Tim Neraca Wilayah",
                notulis = "Yunus Samuel Tandi Bua",
                jenis = "Rapat",
                nomor = "B-204/7400/NW.01/2026",
                startDate = "2026-10-23",
                dateAkhir = "2026-10-23",
                startJam = "10:00",
                endJam = "12:00",
                jamDisplay = "10:00 - 12:00 WITA",
                tanggalDisplay = "23 Okt 2026",
                hari = "Jumat",
                status = "Mendatang",
                isToday = false,
                isUpcoming = true
            )
        )
        val all = daily + upcoming
        val stats = KegiatanStats(
            totalAll = all.size,
            totalToday = daily.size,
            totalUpcoming = upcoming.size,
            totalRapat = all.count { it.jenis == "Rapat" },
            totalKegiatan = all.count { it.jenis == "Kegiatan" }
        )
        return Pair(stats, all)
    }
}
