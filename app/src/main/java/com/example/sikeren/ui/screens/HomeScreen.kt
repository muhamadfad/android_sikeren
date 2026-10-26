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
import kotlinx.coroutines.launch

/**
 * World-Class Modern Dashboard for SIKEREN Mobile
 * Designed with fluid spatial layout, zero box-in-box clutter,
 * and high-clarity glanceable information hierarchy.
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
    var activeFilterTab by remember { mutableStateOf(0) } // 0: Semua, 1: Rapat, 2: Kegiatan

    // Initial Live Sync
    LaunchedEffect(Unit) {
        notifications = ApiClient.fetchNotifications("token-user")
        val livePst = ApiClient.fetchPstToday("token-user")
        if (livePst.schedules.isNotEmpty()) {
            pstData = livePst
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
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // =========================================================================
            // 1. TOP APP BAR: Brand Identity, Profile & Notification
            // =========================================================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: BPS Official Emblem + Personalized Greeting
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNavigateToProfile() }
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = CardBg,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .size(44.dp)
                                .border(0.5.dp, BorderLight, RoundedCornerShape(14.dp))
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

                        Spacer(modifier = Modifier.width(12.dp))

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
                                }
                            }
                            Text(
                                text = "Halo, ${user.namaLengkap.split(" ").firstOrNull() ?: "Pegawai"} 👋",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }
                    }

                    // Right: Notification Bell in Clean Circle
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
            // 2. HERO ATTENDANCE DASHBOARD (Single Cohesive Elevated Surface)
            // =========================================================================
            item {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF003865),
                                        Color(0xFF0284C7)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            // Top Row: Date & Work Hours Tag
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dailySummary.todayDate,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.15f))
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

                            Spacer(modifier = Modifier.height(18.dp))

                            // Main Action Row: Status Title & Scan QR Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Presensi Harian",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.75f),
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
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                    if (dailySummary.presensi.checkInTime != null) {
                                        Text(
                                            text = "Masuk pukul ${dailySummary.presensi.checkInTime} WITA",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.85f),
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }

                                // Tactile White Action Button
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White,
                                    shadowElevation = 4.dp,
                                    modifier = Modifier.clickable { onNavigateToQr() }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
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
            // 3. PST LIVE MONITORING STRIP (Glanceable, Fluid & Non-Intrusive)
            // =========================================================================
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardBg,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(10.dp))
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
                                        text = "Pelayanan Statistik Terpadu (PST)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Petugas Aktif: ${pstData.activeGuardian} (${pstData.activeGuardianSesi})",
                                        fontSize = 11.sp,
                                        color = TextSecondary
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

                        // PST Replacement Alert (If needed)
                        if (pstData.replacementRequests.isNotEmpty() && !user.isPimpinan) {
                            val req = pstData.replacementRequests.first()
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = DangerRedSoft,
                                border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
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
                                            color = TextSecondary
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
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Bantu Jaga", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // User's Own PST Assignment
                        val myPst = pstData.mySchedule
                        if (myPst != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Jadwal Anda: ${myPst.sesiLabel}",
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
                        }
                    }
                }
            }

            // =========================================================================
            // 4. UNIFIED AGENDA SECTION: Fluid Tabs & Modern Ticket Cards
            // =========================================================================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Agenda & Kegiatan",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Filter Segmented Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterPill(
                            label = "Semua",
                            isSelected = activeFilterTab == 0,
                            onClick = { activeFilterTab = 0 }
                        )
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
                    }
                }
            }

            // Filtered Items List
            val combinedList = when (activeFilterTab) {
                1 -> rapatList
                2 -> dailySummary.kegiatanToday.filter { it.jenis != "Rapat" }
                else -> rapatList + dailySummary.kegiatanToday.filter { it.jenis != "Rapat" }
            }

            if (combinedList.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CardBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tidak ada jadwal kegiatan untuk kategori ini.",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                items(combinedList) { item ->
                    AgendaTicketCard(
                        item = item,
                        onClick = { onSelectKegiatan(item) },
                        onLepasRuangan = { showLepasRuanganDialog = item }
                    )
                }
            }
        }

        // =========================================================================
        // DIALOGS & OVERLAYS
        // =========================================================================
        // 1. Notification Sheet
        if (showNotifDialog) {
            Dialog(onDismissRequest = { showNotifDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = CardBg,
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp)
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pemberitahuan",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            IconButton(onClick = { showNotifDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (notifications.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Tidak ada notifikasi baru.", color = TextMuted, fontSize = 13.sp)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(notifications) { notif ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (!notif.isRead) BpsBlueSubtle else SurfaceBg,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = notif.judul,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = notif.pesan,
                                                fontSize = 12.sp,
                                                color = TextSecondary,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                            Text(
                                                text = notif.waktu,
                                                fontSize = 10.sp,
                                                color = TextMuted,
                                                modifier = Modifier.padding(top = 4.dp)
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

        // 2. Dialog Lapor Berhalangan
        if (showBerhalanganDialog) {
            Dialog(onDismissRequest = { showBerhalanganDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = CardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Text(
                            text = "Lapor Berhalangan Jaga PST",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sistem akan otomatis mengabarkan ke rekan kerja untuk mencari pengganti jaga Anda.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = alasanBerhalangan,
                            onValueChange = { alasanBerhalangan = it },
                            label = { Text("Alasan Berhalangan", fontSize = 12.sp) },
                            placeholder = { Text("Tugas dinas lapangan / Sakit / Izin...", fontSize = 12.sp) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(20.dp))
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
                                        snackbarMessage = "Laporan dikirim. Notifikasi disebarkan ke rekan!"
                                        pstData = ApiClient.fetchPstToday("token-user")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Kirim Laporan", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // 3. Dialog Lepas Ruangan
        if (showLepasRuanganDialog != null) {
            val r = showLepasRuanganDialog!!
            Dialog(onDismissRequest = { showLepasRuanganDialog = null }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = CardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Text(
                            text = "Konfirmasi Lepas Ruangan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Apakah rapat \"${r.judul}\" telah usai? Ruangan ${r.tempat} akan otomatis ditandai tersedia untuk rekan lainnya.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(20.dp))
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
                                shape = RoundedCornerShape(12.dp)
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

/**
 * Filter Pill Component
 */
@Composable
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) BpsNavy else CardBg)
            .border(1.dp, if (isSelected) BpsNavy else BorderLight, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
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
 * Modern Ticket-Style Agenda Card
 */
@Composable
private fun AgendaTicketCard(
    item: KegiatanItem,
    onClick: () -> Unit,
    onLepasRuangan: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CardBg,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Time Column
            Column(
                modifier = Modifier
                    .width(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BpsBlueSubtle)
                    .padding(vertical = 10.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = item.jamDisplay.split(" - ").firstOrNull() ?: item.jamDisplay,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BpsNavy
                )
                Text(
                    text = "WITA",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Main Info Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (item.jenis == "Rapat") BpsBlueLight else BpsAmberSoft)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.jenis,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.jenis == "Rapat") BpsNavy else Color(0xFFB45309)
                        )
                    }

                    if (item.jenis == "Rapat" && item.statusRuangan == "Dipakai") {
                        Text(
                            text = "Lepas Ruangan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BpsNavy,
                            modifier = Modifier.clickable { onLepasRuangan() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.judul,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
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
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
