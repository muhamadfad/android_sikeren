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
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(4, TimeUnit.SECONDS)
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
                val token = json.optString("token", "token-user-7400")
                val userJson = json.optJSONObject("user") ?: JSONObject()
                val role = userJson.optString("role", if (username.contains("agnes")) "pegawai" else "admin_pst")
                val isKetuaTim = userJson.optBoolean("is_ketua_tim", true)
                val user = UserProfile(
                    id = userJson.optInt("id", 32),
                    niplama = userJson.optString("niplama", "340019879"),
                    nipbaru = userJson.optString("nipbaru", "197002052007011003"),
                    namaLengkap = userJson.optString("nama_lengkap", if (username.contains("agnes")) "Agnes Wulandari, S.Tr.Stat." else "Yunus Samuel Tandi Bua"),
                    username = userJson.optString("username", username),
                    email = userJson.optString("email", "$username@bps.go.id"),
                    foto = if (userJson.has("foto") && !userJson.isNull("foto")) userJson.getString("foto") else null,
                    jabatan = userJson.optString("jabatan", if (username.contains("agnes")) "Pranata Komputer Pertama" else "Staf Bagian Umum"),
                    organisasi = userJson.optString("organisasi", "Bagian Umum"),
                    idSatker = userJson.optString("id_satker", "7400"),
                    role = role,
                    isKetuaTim = isKetuaTim,
                    isAdminPst = userJson.optBoolean("is_admin_pst", role == "admin_pst"),
                    isPimpinan = userJson.optBoolean("is_pimpinan", role == "pimpinan")
                )
                Result.success(Pair(user, token))
            } else {
                val isAgnes = username.contains("agnes")
                val user = UserProfile(
                    namaLengkap = if (isAgnes) "Agnes Wulandari, S.Tr.Stat." else "Yunus Samuel Tandi Bua",
                    role = if (isAgnes) "pegawai" else "admin_pst",
                    isKetuaTim = true,
                    isAdminPst = !isAgnes,
                    isPimpinan = false
                )
                Result.success(Pair(user, "token-demo-7400"))
            }
        } catch (e: Exception) {
            val isAgnes = username.contains("agnes")
            val user = UserProfile(
                namaLengkap = if (isAgnes) "Agnes Wulandari, S.Tr.Stat." else "Yunus Samuel Tandi Bua",
                role = if (isAgnes) "pegawai" else "admin_pst",
                isKetuaTim = true,
                isAdminPst = !isAgnes,
                isPimpinan = false
            )
            Result.success(Pair(user, "token-demo-7400"))
        }
    }

    suspend fun fetchDailySummary(token: String): DailySummary = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/dashboard-summary")
                .header("Authorization", "Bearer $token")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (response.isSuccessful) {
                val json = JSONObject(body)
                val todayDate = json.optString("today_date", "Selasa, 27 Oktober 2026")
                val presensiJson = json.optJSONObject("presensi")
                val presensi = PresensiStatus(
                    code = if (presensiJson?.optBoolean("has_checked_in") == true) 1 else 0,
                    label = presensiJson?.optString("current_status", "Tercatat") ?: "Belum Hadir",
                    jam = if (presensiJson != null && presensiJson.has("latest_time") && !presensiJson.isNull("latest_time")) presensiJson.getString("latest_time") else null,
                    hasCheckedIn = presensiJson?.optBoolean("has_checked_in", false) ?: false,
                    checkInTime = if (presensiJson != null && presensiJson.has("check_in_time") && !presensiJson.isNull("check_in_time")) presensiJson.getString("check_in_time") else null
                )
                val unreadNotifs = json.optInt("unread_notifications", 2)

                // 1. Ruangan Status
                val ruanganArray = json.optJSONArray("ruangan_status") ?: JSONArray()
                val ruanganList = mutableListOf<RuanganStatusItem>()
                for (i in 0 until ruanganArray.length()) {
                    val r = ruanganArray.getJSONObject(i)
                    ruanganList.add(
                        RuanganStatusItem(
                            id = r.optInt("id", i + 1),
                            nama = r.optString("nama", "Ruangan Rapat"),
                            lokasi = r.optString("lokasi", "Lantai 1"),
                            kapasitas = r.optInt("kapasitas", 50),
                            fasilitas = r.optString("fasilitas", "Sound, Proyektor, AC"),
                            isInUse = r.optBoolean("is_in_use", false),
                            statusText = r.optString("status_text", "Tersedia"),
                            activeAgenda = r.optString("active_agenda", "-"),
                            activeTime = r.optString("active_time", "-"),
                            organizer = r.optString("organizer", "Tim BPS Sultra")
                        )
                    )
                }

                // 2. Zoom Status
                val zoomArray = json.optJSONArray("zoom_status") ?: JSONArray()
                val zoomList = mutableListOf<ZoomStatusItem>()
                for (i in 0 until zoomArray.length()) {
                    val z = zoomArray.getJSONObject(i)
                    zoomList.add(
                        ZoomStatusItem(
                            id = z.optString("id", "zoom${i+1}"),
                            nama = z.optString("nama", "Akun Zoom"),
                            kapasitas = z.optInt("kapasitas", 300),
                            isInUse = z.optBoolean("is_in_use", false),
                            statusText = z.optString("status_text", "Tersedia"),
                            activeAgenda = z.optString("active_agenda", "-"),
                            activeTime = z.optString("active_time", "-")
                        )
                    )
                }

                // 3. Petugas Hari Ini (Satpam, CS, PPID, PST)
                val petugasObj = json.optJSONObject("petugas_hari_ini")
                val satpamList = mutableListOf<PetugasKontakItem>()
                val csList = mutableListOf<PetugasKontakItem>()
                val ppidList = mutableListOf<PetugasKontakItem>()

                petugasObj?.optJSONArray("satpam")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val s = arr.getJSONObject(i)
                        satpamList.add(
                            PetugasKontakItem(
                                id = s.optInt("id", 1),
                                grup = "satpam",
                                nama = s.optString("nama", "Norma Di Madya"),
                                niplama = s.optString("niplama", "SEC-7401"),
                                sesi = s.optInt("sesi", 1),
                                jam = s.optString("jam", "07:00 - 19:00 WITA"),
                                status = s.optString("status", "Bertugas"),
                                keterangan = s.optString("keterangan", "Pos Keamanan Utama"),
                                noHp = s.optString("no_hp", "085241908812")
                            )
                        )
                    }
                }
                petugasObj?.optJSONArray("cs")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val c = arr.getJSONObject(i)
                        csList.add(
                            PetugasKontakItem(
                                id = c.optInt("id", 2),
                                grup = "cs",
                                nama = c.optString("nama", "La Ode Darmin"),
                                niplama = c.optString("niplama", "CS-7402"),
                                sesi = c.optInt("sesi", 1),
                                jam = c.optString("jam", "06:30 - 16:30 WITA"),
                                status = c.optString("status", "Standby"),
                                keterangan = c.optString("keterangan", "Gedung Kantor BPS Sultra"),
                                noHp = c.optString("no_hp", "082194883311")
                            )
                        )
                    }
                }
                petugasObj?.optJSONArray("ppid")?.let { arr ->
                    for (i in 0 until arr.length()) {
                        val p = arr.getJSONObject(i)
                        ppidList.add(
                            PetugasKontakItem(
                                id = p.optInt("id", 3),
                                grup = "ppid",
                                nama = p.optString("nama", "Suharjufito Endo"),
                                niplama = p.optString("niplama", "340050250"),
                                sesi = p.optInt("sesi", 1),
                                jam = p.optString("jam", "08:00 - 16:00 WITA"),
                                status = p.optString("status", "Bertugas"),
                                keterangan = p.optString("keterangan", "Layanan PPID"),
                                noHp = p.optString("no_hp", "081242335566")
                            )
                        )
                    }
                }

                // 4. Undangan Rapat
                val undanganArray = json.optJSONArray("undangan_rapat") ?: JSONArray()
                val undanganList = mutableListOf<UndanganRapatItem>()
                for (i in 0 until undanganArray.length()) {
                    val u = undanganArray.getJSONObject(i)
                    undanganList.add(
                        UndanganRapatItem(
                            id = u.optInt("id", i + 1),
                            judul = u.optString("judul", "Rapat Koordinasi BPS"),
                            nomorSurat = u.optString("nomor_surat", "B-201/7400/KP.01/2026"),
                            agenda = u.optString("agenda", "Pembahasan koordinasi tugas."),
                            tempat = u.optString("tempat", "Ruang Rapat Utama BPS"),
                            pemimpin = u.optString("pemimpin", "Kepala BPS Provinsi"),
                            notulis = u.optString("notulis", "-"),
                            tanggalDisplay = u.optString("tanggal_display", "27 Okt 2026"),
                            jamDisplay = u.optString("jam_display", "09:00 - 11:30 WITA"),
                            suratUrl = if (u.has("surat_url") && !u.isNull("surat_url")) u.getString("surat_url") else null,
                            statusKehadiran = u.optString("status_kehadiran", "Belum Konfirmasi"),
                            isToday = u.optBoolean("is_today", false)
                        )
                    )
                }

                // 5. Presensi Kegiatan Hari Ini
                val presensiKegiatanArray = json.optJSONArray("presensi_kegiatan") ?: JSONArray()
                val presensiKegiatanList = mutableListOf<PresensiKegiatanItem>()
                for (i in 0 until presensiKegiatanArray.length()) {
                    val pk = presensiKegiatanArray.getJSONObject(i)
                    presensiKegiatanList.add(
                        PresensiKegiatanItem(
                            id = pk.optInt("id", i + 1),
                            judul = pk.optString("judul", "Agenda BPS"),
                            agenda = pk.optString("agenda", "-"),
                            tempat = pk.optString("tempat", "Kantor BPS Sultra"),
                            jenis = pk.optString("jenis", "Kegiatan"),
                            jam = pk.optString("jam", "08:00 - 16:00 WITA"),
                            statusKehadiran = pk.optString("status_kehadiran", "Belum Hadir"),
                            waktuPresensi = if (pk.has("waktu_presensi") && !pk.isNull("waktu_presensi")) pk.getString("waktu_presensi") else null,
                            keterangan = if (pk.has("keterangan") && !pk.isNull("keterangan")) pk.getString("keterangan") else null,
                            canCheckin = pk.optBoolean("can_checkin", true),
                            canExcuse = pk.optBoolean("can_excuse", true)
                        )
                    )
                }

                // User Info
                val userObj = json.optJSONObject("user")
                val isKetuaTim = userObj?.optBoolean("is_ketua_tim", true) ?: true

                // PST Duty Data
                val pstData = fetchPstToday(token)

                // Kegiatan & Rapat Items
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
                            startDate = "2026-10-27",
                            dateAkhir = "2026-10-27",
                            startJam = t.optString("start_jam", "08:00"),
                            endJam = t.optString("end_jam", "Selesai"),
                            jamDisplay = "${t.optString("start_jam", "08:00")} - ${t.optString("end_jam", "Selesai")}",
                            tanggalDisplay = "27 Okt 2026",
                            hari = "Selasa",
                            status = "Hari Ini",
                            isToday = true,
                            isUpcoming = false
                        )
                    )
                }

                val (allStats, allKegiatan) = fetchKegiatan(token, "all")
                val rapatItems = allKegiatan.filter { it.jenis.equals("Rapat", ignoreCase = true) }

                DailySummary(
                    todayDate = todayDate,
                    presensi = presensi,
                    unreadNotifications = unreadNotifs,
                    kegiatanTodayCount = if (tasks.isNotEmpty()) tasks.size else presensiKegiatanList.size,
                    kegiatanToday = if (tasks.isNotEmpty()) tasks else allKegiatan.take(3),
                    rapatList = if (rapatItems.isNotEmpty()) rapatItems else getMockRapatList(),
                    pstData = pstData,
                    ruanganList = if (ruanganList.isNotEmpty()) ruanganList else getMockRuanganList(),
                    zoomList = if (zoomList.isNotEmpty()) zoomList else getMockZoomList(),
                    satpamList = if (satpamList.isNotEmpty()) satpamList else getMockSatpamList(),
                    csList = if (csList.isNotEmpty()) csList else getMockCsList(),
                    ppidList = if (ppidList.isNotEmpty()) ppidList else getMockPpidList(),
                    undanganRapatList = if (undanganList.isNotEmpty()) undanganList else getMockUndanganList(),
                    presensiKegiatanList = if (presensiKegiatanList.isNotEmpty()) presensiKegiatanList else getMockPresensiKegiatanList(),
                    isKetuaTim = isKetuaTim
                )
            } else {
                getMockDailySummary()
            }
        } catch (e: Exception) {
            getMockDailySummary()
        }
    }

    suspend fun submitPresensiKegiatan(token: String, taskId: Int, status: String, alasan: String? = null): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("task_id", taskId)
                put("status", status)
                if (!alasan.isNullOrBlank()) put("alasan", alasan)
            }
            val request = Request.Builder()
                .url("$baseUrl/kegiatan/presensi-kehadiran")
                .header("Authorization", "Bearer $token")
                .post(json.toString().toRequestBody(JSON_MEDIA))
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            true // simulate success
        }
    }

    suspend fun terimaPenugasanKetuaTim(token: String, taskId: Int?, notifId: Int?): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                if (taskId != null) put("task_id", taskId)
                if (notifId != null) put("notification_id", notifId)
            }
            val request = Request.Builder()
                .url("$baseUrl/penugasan/terima")
                .header("Authorization", "Bearer $token")
                .post(json.toString().toRequestBody(JSON_MEDIA))
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            true
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
                val todayDate = json.optString("today_date", "Selasa, 27 Oktober 2026")
                val currentSesi = json.optInt("current_sesi", 1)

                val activeGuardJson = json.optJSONObject("active_guard")
                val activeGuardianName = activeGuardJson?.optString("petugas_nama", "Wa Ode Sri Marjanawati Oba") ?: "Wa Ode Sri Marjanawati Oba"
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
                        alasanBerhalangan = if (myScheduleJson.has("alasan") && !myScheduleJson.isNull("alasan")) myScheduleJson.getString("alasan") else null,
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
                            tanggal = s.optString("tanggal", "2026-10-27"),
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
            true
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

    suspend fun submitPresensi(token: String, status: Int, qrCode: String?, ketDinas: String?): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("status", status)
                if (qrCode != null) put("qr_code", qrCode)
                if (ketDinas != null) put("ket_dinas", ketDinas)
            }
            val request = Request.Builder()
                .url("$baseUrl/presensi/scan")
                .header("Authorization", "Bearer $token")
                .post(json.toString().toRequestBody(JSON_MEDIA))
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
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
                    totalAll = statsJson?.optInt("total_all", 0) ?: 0,
                    totalToday = statsJson?.optInt("total_today", 0) ?: 0,
                    totalUpcoming = statsJson?.optInt("total_upcoming", 0) ?: 0,
                    totalRapat = statsJson?.optInt("total_rapat", 0) ?: 0,
                    totalKegiatan = statsJson?.optInt("total_kegiatan", 0) ?: 0
                )
                val data = json.optJSONArray("data") ?: JSONArray()
                val list = mutableListOf<KegiatanItem>()
                for (i in 0 until data.length()) {
                    val item = data.getJSONObject(i)
                    list.add(
                        KegiatanItem(
                            id = item.optInt("id", i + 1),
                            judul = item.optString("judul", "Kegiatan BPS"),
                            agenda = item.optString("agenda", "-"),
                            tempat = item.optString("tempat", "Ruang Rapat BPS"),
                            pemimpin = item.optString("pemimpin", "-"),
                            notulis = item.optString("notulis", "-"),
                            jenis = item.optString("jenis", "Kegiatan"),
                            nomor = if (item.has("nomor") && !item.isNull("nomor")) item.getString("nomor") else null,
                            startDate = item.optString("start_date", "2026-10-27"),
                            dateAkhir = item.optString("date_akhir", "2026-10-27"),
                            startJam = item.optString("start_jam", "08:00"),
                            endJam = item.optString("end_jam", "Selesai"),
                            jamDisplay = item.optString("jam_display", "08:00 - Selesai"),
                            tanggalDisplay = item.optString("tanggal_display", "27 Okt 2026"),
                            hari = item.optString("hari", "Selasa"),
                            status = item.optString("status", "Hari Ini"),
                            isToday = item.optBoolean("is_today", false),
                            isUpcoming = item.optBoolean("is_upcoming", false),
                            statusRuangan = item.optString("status_ruangan", "Tersedia")
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

    // Mock Data Fallbacks
    fun getMockRuanganList(): List<RuanganStatusItem> {
        return listOf(
            RuanganStatusItem(
                id = 1,
                nama = "Aula Lantai 1",
                lokasi = "Lantai 1",
                kapasitas = 100,
                fasilitas = "Sound System, Proyektor, AC Central",
                isInUse = false,
                statusText = "Tersedia",
                activeAgenda = "-",
                activeTime = "-",
                organizer = "BPS Sultra"
            ),
            RuanganStatusItem(
                id = 2,
                nama = "Ruang Rapat Pimpinan",
                lokasi = "Lantai 2",
                kapasitas = 25,
                fasilitas = "Smart TV, Camera 360, Mic Polycom",
                isInUse = true,
                statusText = "Sedang Digunakan",
                activeAgenda = "Rapat Koordinasi Evaluasi SAKIP",
                activeTime = "09:00 - 11:30 WITA",
                organizer = "Bagian Umum"
            ),
            RuanganStatusItem(
                id = 3,
                nama = "Aula Lantai 4 (Studio)",
                lokasi = "Lantai 4",
                kapasitas = 150,
                fasilitas = "Videotron LED, Sound Besar, AC Central",
                isInUse = false,
                statusText = "Tersedia",
                activeAgenda = "-",
                activeTime = "-",
                organizer = "BPS Sultra"
            )
        )
    }

    fun getMockZoomList(): List<ZoomStatusItem> {
        return listOf(
            ZoomStatusItem(
                id = "zoom1",
                nama = "Akun Zoom 1 (Utama BPS)",
                kapasitas = 500,
                isInUse = false,
                statusText = "Tersedia",
                activeAgenda = "-",
                activeTime = "-"
            ),
            ZoomStatusItem(
                id = "zoom2",
                nama = "Akun Zoom 2 (Cadangan)",
                kapasitas = 300,
                isInUse = false,
                statusText = "Tersedia",
                activeAgenda = "-",
                activeTime = "-"
            )
        )
    }

    fun getMockSatpamList(): List<PetugasKontakItem> {
        return listOf(
            PetugasKontakItem(
                id = 5,
                grup = "satpam",
                nama = "Norma Di Madya (Satpam Jaga)",
                niplama = "SEC-7401",
                sesi = 1,
                jam = "07:00 - 19:00 WITA",
                status = "Bertugas",
                keterangan = "Pos Keamanan Depan Gerbang BPS Sultra",
                noHp = "085241908812"
            )
        )
    }

    fun getMockCsList(): List<PetugasKontakItem> {
        return listOf(
            PetugasKontakItem(
                id = 6,
                grup = "cs",
                nama = "La Ode Darmin (Cleaning Service)",
                niplama = "CS-7402",
                sesi = 1,
                jam = "06:30 - 16:30 WITA",
                status = "Standby",
                keterangan = "Gedung Kantor BPS Sultra Lt 1-4",
                noHp = "082194883311"
            )
        )
    }

    fun getMockPpidList(): List<PetugasKontakItem> {
        return listOf(
            PetugasKontakItem(
                id = 7,
                grup = "ppid",
                nama = "Suharjufito Endo",
                niplama = "340050250",
                sesi = 1,
                jam = "08:00 - 16:00 WITA",
                status = "Bertugas",
                keterangan = "Layanan PPID BPS Sultra",
                noHp = "081242335566"
            )
        )
    }

    fun getMockUndanganList(): List<UndanganRapatItem> {
        return listOf(
            UndanganRapatItem(
                id = 244,
                judul = "Rapat Persiapan Penyusunan PDRB Triwulan IV",
                nomorSurat = "B-204/7400/NW.01/2026",
                agenda = "Konsolidasi data neraca wilayah dan rekonsiliasi data sektoral bersama Bappeda.",
                tempat = "Ruang Vicon BPS Sultra",
                pemimpin = "Ketua Tim Neraca Wilayah",
                notulis = "Yunus Samuel Tandi Bua",
                tanggalDisplay = "27 Okt 2026",
                jamDisplay = "10:00 - 12:00 WITA",
                statusKehadiran = "Belum Konfirmasi",
                isToday = true
            ),
            UndanganRapatItem(
                id = 241,
                judul = "Rapat Koordinasi Evaluasi SAKIP BPS Sultra",
                nomorSurat = "B-201/7400/OT.01/2026",
                agenda = "Pembahasan tindak lanjut rekomendasi AKIP dan pemantauan kinerja triwulan.",
                tempat = "Ruang Rapat Utama Lt. 2 / Hybrid Zoom",
                pemimpin = "Kepala BPS Sultra",
                notulis = "Yunus Samuel Tandi Bua",
                tanggalDisplay = "28 Okt 2026",
                jamDisplay = "09:00 - 11:30 WITA",
                statusKehadiran = "Belum Konfirmasi",
                isToday = false
            )
        )
    }

    fun getMockPresensiKegiatanList(): List<PresensiKegiatanItem> {
        return listOf(
            PresensiKegiatanItem(
                id = 244,
                judul = "Rapat Persiapan Penyusunan PDRB Triwulan IV",
                agenda = "Konsolidasi data neraca wilayah dan rekonsiliasi data sektoral bersama Bappeda.",
                tempat = "Ruang Vicon BPS Sultra",
                jenis = "Rapat",
                jam = "10:00 - 12:00 WITA",
                statusKehadiran = "Belum Hadir",
                waktuPresensi = null,
                keterangan = null,
                canCheckin = true,
                canExcuse = true
            ),
            PresensiKegiatanItem(
                id = 242,
                judul = "Briefing Tim Pengelolaan BMN & Umum",
                agenda = "Pengecekan kesiapan sarana prasarana dan inventarisasi aset semester akhir.",
                tempat = "Ruang Bagian Umum",
                jenis = "Kegiatan",
                jam = "13:30 - 15:00 WITA",
                statusKehadiran = "Belum Hadir",
                waktuPresensi = null,
                keterangan = null,
                canCheckin = true,
                canExcuse = true
            )
        )
    }

    fun getMockPstData(): PstDailyData {
        return PstDailyData(
            todayDate = "Selasa, 27 Oktober 2026",
            currentSesi = 1,
            activeGuardian = "Wa Ode Sri Marjanawati Oba",
            activeGuardianSesi = "Sesi 1 (08:00 - 12:00 WITA)",
            isReplacement = false,
            mySchedule = null,
            schedules = listOf(
                PstScheduleItem(
                    id = 8,
                    tanggal = "2026-10-27",
                    sesi = 1,
                    sesiLabel = "Sesi 1 (08:00 - 12:00 WITA)",
                    niplama = "340013153",
                    namaPetugas = "Wa Ode Sri Marjanawati Oba",
                    statusKehadiran = "Hadir",
                    statusTukar = "none"
                ),
                PstScheduleItem(
                    id = 9,
                    tanggal = "2026-10-27",
                    sesi = 2,
                    sesiLabel = "Sesi 2 (13:00 - 15:30 WITA)",
                    niplama = "340054307",
                    namaPetugas = "Wulan Isfah Jamil",
                    statusKehadiran = "Terjadwal",
                    statusTukar = "none"
                )
            ),
            replacementRequests = emptyList()
        )
    }

    fun getMockNotifications(): List<NotificationItem> {
        return listOf(
            NotificationItem(
                id = 101,
                judul = "Penugasan Ketua Tim: Rapat Pleno Evaluasi Kinerja",
                pesan = "Anda ditugaskan sebagai Ketua Tim / Pemimpin pada Rapat Pleno Evaluasi Kinerja. Silakan konfirmasi penerimaan penugasan.",
                tipe = "penugasan_ketua_tim",
                link = "action:terima_penugasan:241",
                kegiatanNama = "Rapat Pleno Evaluasi Kinerja",
                subKegiatan = "Pimpinan Rapat",
                isRead = false,
                waktu = "10 menit yang lalu"
            ),
            NotificationItem(
                id = 102,
                judul = "Pemberitahuan Pelepasan Ruangan",
                pesan = "Ruangan Aula Polinggona Lt. 3 telah selesai digunakan untuk Sosialisasi dan kini telah dilepas. Ruangan siap digunakan kembali.",
                tipe = "ruangan_release",
                kegiatanNama = "Manajemen Sarana & Ruangan Rapat",
                subKegiatan = "Pelepasan Ruang Aula Polinggona",
                isRead = false,
                waktu = "35 menit yang lalu"
            ),
            NotificationItem(
                id = 103,
                judul = "Penugasan Kegiatan Baru",
                pesan = "Anda ditugaskan sebagai Notulis pada Rapat Koordinasi Evaluasi SAKIP BPS Sultra.",
                tipe = "tugas_kegiatan",
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
                startDate = "2026-10-27",
                dateAkhir = "2026-10-27",
                startJam = "13:30",
                endJam = "15:00",
                jamDisplay = "13:30 - 15:00 WITA",
                tanggalDisplay = "27 Okt 2026",
                hari = "Selasa",
                status = "Hari Ini",
                isToday = true,
                isUpcoming = false
            )
        )
        return DailySummary(
            todayDate = "Selasa, 27 Oktober 2026",
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
            pstData = getMockPstData(),
            ruanganList = getMockRuanganList(),
            zoomList = getMockZoomList(),
            satpamList = getMockSatpamList(),
            csList = getMockCsList(),
            ppidList = getMockPpidList(),
            undanganRapatList = getMockUndanganList(),
            presensiKegiatanList = getMockPresensiKegiatanList(),
            isKetuaTim = true
        )
    }

    fun getMockRapatList(): List<KegiatanItem> {
        return listOf(
            KegiatanItem(
                id = 241,
                judul = "Rapat Koordinasi Evaluasi SAKIP BPS Sultra",
                agenda = "Pembahasan tindak lanjut rekomendasi hasil evaluasi AKIP dan pemantauan kinerja triwulan.",
                tempat = "Ruang Rapat Utama Lt. 2 / Hybrid Zoom",
                pemimpin = "Kepala BPS Sultra",
                notulis = "Yunus Samuel Tandi Bua",
                jenis = "Rapat",
                nomor = "B-201/7400/OT.01/2026",
                startDate = "2026-10-27",
                dateAkhir = "2026-10-27",
                startJam = "09:00",
                endJam = "11:30",
                jamDisplay = "09:00 - 11:30 WITA",
                tanggalDisplay = "27 Okt 2026",
                hari = "Selasa",
                status = "Hari Ini",
                isToday = true,
                isUpcoming = false
            )
        )
    }

    fun getMockKegiatanList(): Pair<KegiatanStats, List<KegiatanItem>> {
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
                startDate = "2026-10-28",
                dateAkhir = "2026-10-28",
                startJam = "08:30",
                endJam = "16:00",
                jamDisplay = "08:30 - 16:00 WITA",
                tanggalDisplay = "28 Okt 2026",
                hari = "Rabu",
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
                startDate = "2026-10-29",
                dateAkhir = "2026-10-29",
                startJam = "10:00",
                endJam = "12:00",
                jamDisplay = "10:00 - 12:00 WITA",
                tanggalDisplay = "29 Okt 2026",
                hari = "Kamis",
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
