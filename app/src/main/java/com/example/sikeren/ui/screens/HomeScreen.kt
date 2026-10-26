package com.example.sikeren.ui.screens

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

/**
 * Responsive & Modern Dashboard for SIKEREN Mobile
 * Optimized for ALL screen sizes (no rigid box-in-box; fluid layout, fast parallel async loading).
 * Features:
 * 1. Hero Presensi Card with 1-tap QR Check-in.
 * 2. Live PST Guarding Strip (Active guardian, swap/claim request, pimpinan exempt, admin PST management).
 * 3. Jadwal Rapat prominently placed with "Lepas Ruangan" quick action.
 * 4. Notifications with Task Kegiatan & Sub-Kegiatan.
 */
@Composable
fun HomeScreen(
    user: UserProfile,
    dailySummary: DailySummary,
    onNavigateToQr: () -> Unit,
    onNavigateToKegiatan: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onSelectKegiatan: (KegiatanItem) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var showNotifDialog by remember { mutableStateOf(false) }
    var showBerhalanganDialog by remember { mutableStateOf(false) }
    var showLepasRuanganDialog by remember { mutableStateOf<KegiatanItem?>(null) }
    var alasanBerhalangan by remember { mutableStateOf("") }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var pstData by remember { mutableStateOf(dailySummary.pstData) }
    var rapatList by remember { mutableStateOf(dailySummary.rapatList) }
    var activeFilterTab by remember { mutableStateOf(1) } // Default to 1: Rapat

    // Fast parallel async fetch (never blocks the UI thread)
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val notifsDeferred = async { ApiClient.fetchNotifications("token-user") }
            val pstDeferred = async { ApiClient.fetchPstToday("token-user") }

            val notifs = notifsDeferred.await()
            if (notifs.isNotEmpty()) {
                notifications = notifs
            }

            val livePst = pstDeferred.await()
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
            contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // =========================================================================
            // 1. TOP HEADER (Responsive: Logo + User Greeting + Notif Bell)
            // =========================================================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Official BPS logo + User Info
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
                                } else if (user.isPimpinan) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AccentPurpleSoft)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "PIMPINAN",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentPurple
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

                    // Right: Notification Bell in Soft Circle
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
            // 2. HERO ATTENDANCE CARD (Harmonious BPS Navy/Blue Gradient)
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
                            // Top Date & Jam Kerja
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dailySummary.todayDate,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "07:30 - 16:00 WITA",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Main Status & Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Presensi Kantor BPS",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (dailySummary.presensi.hasCheckedIn) AccentGreen else BpsAmber
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = dailySummary.presensi.label,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                    if (dailySummary.presensi.checkInTime != null) {
                                        Text(
                                            text = "Masuk: ${dailySummary.presensi.checkInTime} WITA",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.85f),
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.White,
                                    shadowElevation = 2.dp,
                                    modifier = Modifier.clickable { onNavigateToQr() }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QrCodeScanner,
                                            contentDescription = null,
                                            tint = BpsNavy,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Scan QR",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BpsNavy
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 3. JADWAL PST UNTUK MENJAGA (Live Desk, Tukar Shift, Bebas Pimpinan)
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
                        // Live Guardian Row
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
                                        text = "Petugas Jaga PST Hari Ini",
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

                        // Urgent Replacement Request (Colleagues who are unable to attend)
                        if (pstData.replacementRequests.isNotEmpty() && !user.isPimpinan) {
                            val req = pstData.replacementRequests.first()
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = DangerRedSoft,
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, DangerRed.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Butuh Pengganti Jaga PST!",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DangerRedDark
                                        )
                                        Text(
                                            text = "${req.namaPetugas} berhalangan untuk ${req.sesiLabel}",
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                val success = ApiClient.claimPstReplacement("token-user", req.id)
                                                if (success) {
                                                    snackbarMessage = "Terima kasih! Anda resmi terdaftar menggantikan jaga PST."
                                                    pstData = ApiClient.fetchPstToday("token-user")
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreenDark),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Bantu Jaga", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // User's Own PST Assignment (if assigned, and user is not pimpinan)
                        val myPst = pstData.mySchedule
                        if (myPst != null && !user.isPimpinan) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Jadwal Tugas Anda: ${myPst.sesiLabel}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BpsNavy
                                )
                                if (myPst.statusKehadiran != "Berhalangan") {
                                    Text(
                                        text = "Lapor Berhalangan",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DangerRed,
                                        modifier = Modifier.clickable { showBerhalanganDialog = true }
                                    )
                                } else {
                                    Text(
                                        text = "Status: Berhalangan",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DangerRed
                                    )
                                }
                            }
                        } else if (user.isPimpinan) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• Bebas Tugas Jaga PST (Jabatan Pimpinan)",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // 4. JADWAL RAPAT & AGENDA (Prominent, No "Layanan")
            // =========================================================================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Jadwal Rapat & Agenda",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Filter Tabs
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterPill(
                            label = "Rapat",
                            isSelected = activeFilterTab == 1,
                            onClick = { activeFilterTab = 1 }
                        )
                        FilterPill(
                            label = "Kegiatan",
                            isSelected = activeFilterTab == 2,
                            onClick = { activeFilterTab = 2 }
                        )
                        FilterPill(
                            label = "Semua",
                            isSelected = activeFilterTab == 0,
                            onClick = { activeFilterTab = 0 }
                        )
                    }
                }
            }

            // Items List
            val displayList = when (activeFilterTab) {
                1 -> rapatList
                2 -> dailySummary.kegiatanToday.filter { it.jenis != "Rapat" }
                else -> rapatList + dailySummary.kegiatanToday.filter { it.jenis != "Rapat" }
            }

            if (displayList.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CardBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tidak ada jadwal rapat untuk saat ini.",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(displayList) { item ->
                    ResponsiveAgendaCard(
                        item = item,
                        onClick = { onSelectKegiatan(item) },
                        onLepasRuangan = { showLepasRuanganDialog = item }
                    )
                }
            }
        }

        // =========================================================================
        // 5. DIALOGS (Notifikasi dengan Kegiatan & Sub-Kegiatan, Lepas Ruangan)
        // =========================================================================
        // Notification Dialog with Task & Sub-Kegiatan detail
        if (showNotifDialog) {
            Dialog(onDismissRequest = { showNotifDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = CardBg,
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
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

                                            // Sub-kegiatan tag if available
                                            if (!notif.subKegiatan.isNullOrBlank() || !notif.kegiatanNama.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    if (!notif.kegiatanNama.isNullOrBlank()) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(BpsBlueLight)
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = notif.kegiatanNama,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = BpsNavy
                                                            )
                                                        }
                                                    }
                                                    if (!notif.subKegiatan.isNullOrBlank()) {
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(BpsAmberSoft)
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = notif.subKegiatan,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFFB45309)
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
                }
            }
        }

        // Dialog Lapor Berhalangan
        if (showBerhalanganDialog) {
            Dialog(onDismissRequest = { showBerhalanganDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Lapor Berhalangan PST",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Notifikasi akan dikirimkan ke rekan kerja non-pimpinan untuk mencari pengganti jadwal jaga Anda.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = alasanBerhalangan,
                            onValueChange = { alasanBerhalangan = it },
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
                            TextButton(onClick = { showBerhalanganDialog = false }) {
                                Text("Batal", color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val jadwalId = pstData.mySchedule?.id ?: 1
                                    coroutineScope.launch {
                                        ApiClient.reportPstUnable("token-user", jadwalId, alasanBerhalangan)
                                        showBerhalanganDialog = false
                                        alasanBerhalangan = ""
                                        snackbarMessage = "Laporan dikirim. Notifikasi telah disebarkan!"
                                        pstData = ApiClient.fetchPstToday("token-user")
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

        // Dialog Lepas Ruangan
        if (showLepasRuanganDialog != null) {
            val r = showLepasRuanganDialog!!
            Dialog(onDismissRequest = { showLepasRuanganDialog = null }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Konfirmasi Lepas Ruangan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Apakah rapat \"${r.judul}\" telah selesai? Ruangan ${r.tempat} akan otomatis ditandai bebas agar bisa dipakai tim lain.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showLepasRuanganDialog = null }) {
                                Text("Batal", color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        ApiClient.releaseRoom("token-user", r.id, r.tempat)
                                        showLepasRuanganDialog = null
                                        rapatList = rapatList.map {
                                            if (it.id == r.id) it.copy(statusRuangan = "Telah Dilepas (Tersedia)") else it
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BpsNavy),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Lepas Sekarang", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) BpsNavy else CardBg)
            .border(1.dp, if (isSelected) BpsNavy else BorderLight, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else TextSecondary
        )
    }
}

/**
 * Responsive Agenda Card (Adapts to all phone screen widths)
 */
@Composable
private fun ResponsiveAgendaCard(
    item: KegiatanItem,
    onClick: () -> Unit,
    onLepasRuangan: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Time + Status Badge + Quick Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (item.jenis == "Rapat") BpsBlueLight else BpsAmberSoft)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.jenis,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.jenis == "Rapat") BpsNavy else Color(0xFFB45309)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.jamDisplay,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BpsNavy
                    )
                }

                if (item.jenis == "Rapat" && item.statusRuangan == "Dipakai") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BpsBlueSubtle)
                            .border(0.5.dp, BpsBlue.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { onLepasRuangan() }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Lepas Ruangan",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BpsNavy
                        )
                    }
                } else if (item.statusRuangan == "Telah Dilepas (Tersedia)") {
                    Text(
                        text = "Tersedia",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreenDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title (Max 2 lines, responsive)
            Text(
                text = item.judul,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Location & Leader Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = TextSecondary,
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

                if (!item.pemimpin.isNullOrBlank() && item.pemimpin != "-") {
                    Text(
                        text = "PIC: ${item.pemimpin}",
                        fontSize = 10.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
