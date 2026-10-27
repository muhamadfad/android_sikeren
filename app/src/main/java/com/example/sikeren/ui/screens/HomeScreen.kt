package com.example.sikeren.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sikeren.R
import com.example.sikeren.model.*
import com.example.sikeren.network.ApiClient
import com.example.sikeren.theme.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    user: UserProfile,
    dailySummary: DailySummary,
    onNavigateToQr: () -> Unit,
    onNavigateToKegiatan: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToDashboardKeseluruhan: () -> Unit = {},
    onSelectKegiatan: (KegiatanItem) -> Unit,
    onRefreshData: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var showNotifDialog by remember { mutableStateOf(false) }
    var showBerhalanganKegiatanDialog by remember { mutableStateOf<PresensiKegiatanItem?>(null) }
    var showBerhalanganPstDialog by remember { mutableStateOf(false) }
    var showLepasRuanganDialog by remember { mutableStateOf<KegiatanItem?>(null) }

    var alasanBerhalanganKegiatan by remember { mutableStateOf("") }
    var alasanBerhalanganPst by remember { mutableStateOf("") }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var pstData by remember { mutableStateOf(dailySummary.pstData) }

    // Fetch notifications & PST asynchronously
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val notifsDeferred = async { ApiClient.fetchNotifications("token-user") }
            val livePstDeferred = async { ApiClient.fetchPstToday("token-user") }

            val notifs = notifsDeferred.await()
            if (notifs.isNotEmpty()) {
                notifications = notifs
            }

            val livePst = livePstDeferred.await()
            if (livePst.schedules.isNotEmpty()) {
                pstData = livePst
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // =========================================================================
            // 1. TOP HEADER (Logo + Identitas User + Bell Notifikasi)
            // =========================================================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToProfile() }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CardBg,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .size(42.dp)
                                .border(0.5.dp, BorderLight, RoundedCornerShape(12.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_bps_official),
                                    contentDescription = "Logo BPS",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "BPS SULTRA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BpsNavy,
                                    letterSpacing = 0.8.sp
                                )
                                if (user.isKetuaTim) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(BpsBlueLight)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "KETUA TIM",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BpsNavy
                                        )
                                    }
                                }
                                if (user.isAdminPst) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(BpsAmberSoft)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "ADMIN PST",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB45309)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Halo, ${user.namaLengkap.split(" ").firstOrNull() ?: "Pegawai"} 👋",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Bell Notifikasi
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CardBg)
                            .border(1.dp, BorderLight, CircleShape)
                            .clickable { showNotifDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Notifikasi",
                            tint = BpsNavy,
                            modifier = Modifier.size(22.dp)
                        )
                        val unreadCount = notifications.count { !it.isRead }
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-4).dp, y = 4.dp)
                                    .clip(CircleShape)
                                    .background(DangerRed)
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // 2. HERO: PRESENSI HADIR KEGIATAN HARI INI (Bukan Presensi Hadir Kantor)
            // =========================================================================
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(BpsNavy, BpsBlue)
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AssignmentTurnedIn,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Presensi Kegiatan Hari Ini",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = dailySummary.todayDate,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val kegiatanList = dailySummary.presensiKegiatanList
                            if (kegiatanList.isEmpty()) {
                                Text(
                                    text = "Tidak ada agenda kegiatan khusus hari ini. Anda bebas dari jadwal rapat/tugas.",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    kegiatanList.take(2).forEach { item ->
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = Color.White.copy(alpha = 0.15f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = item.judul,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        modifier = Modifier.weight(1f),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(
                                                                if (item.statusKehadiran == "Hadir") AccentGreen
                                                                else if (item.statusKehadiran == "Berhalangan") DangerRed
                                                                else BpsAmber
                                                            )
                                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = item.statusKehadiran,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "${item.jam} • ${item.tempat}",
                                                    fontSize = 11.sp,
                                                    color = Color.White.copy(alpha = 0.85f)
                                                )

                                                if (item.statusKehadiran != "Hadir" && item.statusKehadiran != "Berhalangan") {
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        // Tombol Presensi Hadir
                                                        Button(
                                                            onClick = {
                                                                coroutineScope.launch {
                                                                    val ok = ApiClient.submitPresensiKegiatan("token-user", item.id, "Hadir")
                                                                    if (ok) {
                                                                        snackbarMessage = "Kehadiran pada '${item.judul}' berhasil dicatat!"
                                                                        onRefreshData()
                                                                    }
                                                                }
                                                            },
                                                            modifier = Modifier.weight(1f),
                                                            shape = RoundedCornerShape(8.dp),
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                                            contentPadding = PaddingValues(vertical = 4.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = null,
                                                                tint = BpsNavy,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = "Hadir",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = BpsNavy
                                                            )
                                                        }

                                                        // Tombol Berhalangan (Cari Pengganti)
                                                        OutlinedButton(
                                                            onClick = {
                                                                showBerhalanganKegiatanDialog = item
                                                            },
                                                            modifier = Modifier.weight(1f),
                                                            shape = RoundedCornerShape(8.dp),
                                                            colors = ButtonDefaults.outlinedButtonColors(
                                                                contentColor = Color.White
                                                            ),
                                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.7f)),
                                                            contentPadding = PaddingValues(vertical = 4.dp)
                                                        ) {
                                                            Text(
                                                                text = "Berhalangan (Ganti)",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = Color.White
                                                            )
                                                        }
                                                    }
                                                } else if (item.statusKehadiran == "Hadir" && item.waktuPresensi != null) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Waktu presensi: ${item.waktuPresensi}",
                                                        fontSize = 10.sp,
                                                        color = AccentGreenSoft
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 3. SHORTCUT MENU: DASHBOARD KESELURUHAN BPS
            // =========================================================================
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CardBg,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToDashboardKeseluruhan() }
                        .border(1.dp, BorderLight, RoundedCornerShape(18.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(BpsBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dashboard,
                                    contentDescription = null,
                                    tint = BpsNavy,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Dashboard Keseluruhan",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Status 3 Ruangan, 2 Zoom & Petugas Jaga Hari Ini",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = BpsNavy,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // =========================================================================
            // 4. KONTAK SIAGA: SATPAM & CLEANING SERVICE HARI INI
            // =========================================================================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kontak Siaga Satpam & CS Hari Ini",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            // Satpam Card
            dailySummary.satpamList.firstOrNull()?.let { s ->
                item {
                    PetugasQuickContactCard(
                        nama = s.nama,
                        role = "Petugas Keamanan (Satpam)",
                        jam = s.jam,
                        status = s.status,
                        noHp = s.noHp,
                        icon = Icons.Default.Security,
                        badgeBg = AccentOrangeSoft,
                        badgeColor = AccentOrange,
                        context = context
                    )
                }
            }

            // Cleaning Service Card
            dailySummary.csList.firstOrNull()?.let { c ->
                item {
                    PetugasQuickContactCard(
                        nama = c.nama,
                        role = "Petugas Kebersihan (Cleaning Service)",
                        jam = c.jam,
                        status = c.status,
                        noHp = c.noHp,
                        icon = Icons.Default.CleaningServices,
                        badgeBg = AccentGreenSoft,
                        badgeColor = AccentGreen,
                        context = context
                    )
                }
            }

            // =========================================================================
            // 5. UNDANGAN RAPAT RESMI
            // =========================================================================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Undangan Rapat Resmi",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Lihat Semua",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BpsNavy,
                        modifier = Modifier.clickable { onNavigateToKegiatan() }
                    )
                }
            }

            if (dailySummary.undanganRapatList.isEmpty()) {
                item {
                    Text(
                        text = "Belum ada undangan rapat baru.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            } else {
                items(dailySummary.undanganRapatList.take(2)) { u ->
                    UndanganRapatCard(item = u)
                }
            }

            // =========================================================================
            // 6. PETUGAS PST UNTUK MENJAGA (Roster & Tukar Shift)
            // =========================================================================
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CardBg,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderLight, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BpsBlueLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SupportAgent,
                                        contentDescription = null,
                                        tint = BpsNavy,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Petugas PST Menjaga Hari Ini",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${pstData.activeGuardian} (${pstData.activeGuardianSesi})",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AccentGreenSoft)
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "LIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGreenDark
                                )
                            }
                        }

                        // My PST schedule banner
                        if (pstData.mySchedule != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BpsBlueLight,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Jadwal Jaga PST Anda: ${pstData.mySchedule?.sesiLabel}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BpsNavy
                                        )
                                        Text(
                                            text = "Status: ${pstData.mySchedule?.statusKehadiran}",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    if (pstData.mySchedule?.statusKehadiran != "Berhalangan") {
                                        OutlinedButton(
                                            onClick = { showBerhalanganPstDialog = true },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text("Berhalangan", fontSize = 10.sp, color = DangerRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // SNACKBAR FEEDBACK
        // =========================================================================
        snackbarMessage?.let { msg ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp, start = 20.dp, end = 20.dp),
                action = {
                    TextButton(onClick = { snackbarMessage = null }) {
                        Text("OK", color = Color.White)
                    }
                }
            ) {
                Text(msg)
            }
        }

        // =========================================================================
        // DIALOG: LAPOR BERHALANGAN KEGIATAN
        // =========================================================================
        if (showBerhalanganKegiatanDialog != null) {
            val item = showBerhalanganKegiatanDialog!!
            Dialog(onDismissRequest = { showBerhalanganKegiatanDialog = null }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Lapor Berhalangan Kegiatan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Agenda: ${item.judul}\nKeterangan Anda akan disampaikan agar dapat digantikan pegawai lain.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = alasanBerhalanganKegiatan,
                            onValueChange = { alasanBerhalanganKegiatan = it },
                            label = { Text("Alasan Berhalangan", fontSize = 12.sp) },
                            placeholder = { Text("Sakit / Tugas Luar Kota / Izin...", fontSize = 12.sp) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showBerhalanganKegiatanDialog = null }) {
                                Text("Batal", color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        ApiClient.submitPresensiKegiatan("token-user", item.id, "Berhalangan", alasanBerhalanganKegiatan)
                                        showBerhalanganKegiatanDialog = null
                                        alasanBerhalanganKegiatan = ""
                                        snackbarMessage = "Laporan berhalangan tercatat. Sistem siap mencari pengganti."
                                        onRefreshData()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Kirim & Cari Pengganti", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // DIALOG: LAPOR BERHALANGAN PST
        // =========================================================================
        if (showBerhalanganPstDialog) {
            Dialog(onDismissRequest = { showBerhalanganPstDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Lapor Berhalangan Jaga PST",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Notifikasi akan dikirimkan ke seluruh pegawai non-pimpinan untuk mencari pengganti jaga PST.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = alasanBerhalanganPst,
                            onValueChange = { alasanBerhalanganPst = it },
                            label = { Text("Alasan Berhalangan", fontSize = 12.sp) },
                            placeholder = { Text("Tugas lapangan / Sakit / Izin...", fontSize = 12.sp) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showBerhalanganPstDialog = false }) {
                                Text("Batal", color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val jadwalId = pstData.mySchedule?.id ?: 1
                                    coroutineScope.launch {
                                        ApiClient.reportPstUnable("token-user", jadwalId, alasanBerhalanganPst)
                                        showBerhalanganPstDialog = false
                                        alasanBerhalanganPst = ""
                                        snackbarMessage = "Laporan berhalangan dikirim!"
                                        pstData = ApiClient.fetchPstToday("token-user")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Kirim Laporan", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // DIALOG: NOTIFIKASI & PENUGASAN KETUA TIM
        // =========================================================================
        if (showNotifDialog) {
            Dialog(onDismissRequest = { showNotifDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Notifikasi & Penugasan",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            IconButton(onClick = { showNotifDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (notifications.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Tidak ada notifikasi tugas baru.", color = TextMuted, fontSize = 13.sp)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(notifications) { notif ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (!notif.isRead) BpsBlueSubtle else SurfaceBg,
                                        border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderLight),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = notif.judul,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = notif.waktu,
                                                    fontSize = 10.sp,
                                                    color = TextMuted
                                                )
                                            }

                                            Text(
                                                text = notif.pesan,
                                                fontSize = 12.sp,
                                                color = TextSecondary,
                                                modifier = Modifier.padding(top = 3.dp)
                                            )

                                            // Action for Ketua Tim to accept task
                                            if (notif.tipe == "penugasan_ketua_tim" && !notif.isRead) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Button(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            val ok = ApiClient.terimaPenugasanKetuaTim("token-user", null, notif.id)
                                                            if (ok) {
                                                                snackbarMessage = "Penugasan Ketua Tim berhasil diterima!"
                                                                notifications = ApiClient.fetchNotifications("token-user")
                                                            }
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = BpsNavy),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Terima Penugasan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PetugasQuickContactCard(
    nama: String,
    role: String,
    jam: String,
    status: String,
    noHp: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeBg: Color,
    badgeColor: Color,
    context: Context
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = role,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentGreenSoft)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = nama,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Jam Tugas: $jam",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            if (noHp.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$noHp"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Telepon",
                            modifier = Modifier.size(14.dp),
                            tint = BpsNavy
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Telepon", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BpsNavy)
                    }

                    Button(
                        onClick = {
                            val cleanNo = noHp.replace("-", "").replace(" ", "").trim()
                            val waNumber = if (cleanNo.startsWith("0")) "62" + cleanNo.substring(1) else cleanNo
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$waNumber"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "WhatsApp",
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun UndanganRapatCard(item: UndanganRapatItem) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BpsBlueLight)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.nomorSurat,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = BpsNavy
                    )
                }

                Text(
                    text = item.tanggalDisplay,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.judul,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.agenda,
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.jamDisplay,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.tempat,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
