package com.example.sikeren.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sikeren.model.*
import com.example.sikeren.theme.*

@Composable
fun DashboardKeseluruhanScreen(
    dailySummary: DailySummary,
    onBack: () -> Unit,
    onOpenRuangan: () -> Unit = {}
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CardBg)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Dashboard Keseluruhan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Monitoring Sarana, Zoom & Petugas BPS Sultra",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // 1. STATISTIK INSTITUSI
                item {
                    Text(
                        text = "Statistik Institusi",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Hari Ini",
                            count = "${dailySummary.kegiatanTodayCount}",
                            icon = Icons.Default.Event,
                            bg = BpsBlueLight,
                            iconTint = BpsNavy
                        )
                        StatMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Rapat",
                            count = "${dailySummary.rapatList.size}",
                            icon = Icons.Default.Groups,
                            bg = AccentGreenSoft,
                            iconTint = AccentGreen
                        )
                        StatMetricCard(
                            modifier = Modifier.weight(1f),
                            title = "Ruangan",
                            count = "${dailySummary.ruanganList.size}",
                            icon = Icons.Default.MeetingRoom,
                            bg = AccentOrangeSoft,
                            iconTint = AccentOrange
                        )
                    }
                }

                // 2. STATUS 3 RUANGAN RAPAT REAL-TIME
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status Ruangan Rapat",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BpsBlueLight)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "3 Ruangan BPS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BpsNavy
                            )
                        }
                    }
                }

                items(dailySummary.ruanganList) { r ->
                    RuanganCard(item = r)
                }

                // 3. STATUS 2 AKUN ZOOM
                item {
                    Text(
                        text = "Status Akun Zoom Meeting",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                items(dailySummary.zoomList) { z ->
                    ZoomCard(item = z)
                }

                // 4. KONTAK SIAGA PETUGAS JAGA HARI INI
                item {
                    Text(
                        text = "Petugas Siaga & Kontak Hari Ini",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Hubungi langsung petugas Satpam, CS, PPID dan PST yang bertugas",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                // Petugas Satpam
                if (dailySummary.satpamList.isNotEmpty()) {
                    items(dailySummary.satpamList) { s ->
                        PetugasKontakCard(
                            item = s,
                            roleBadge = "Petugas Keamanan (Satpam)",
                            badgeBg = AccentOrangeSoft,
                            badgeColor = AccentOrange,
                            icon = Icons.Default.Security,
                            context = context
                        )
                    }
                }

                // Petugas CS (Cleaning Service)
                if (dailySummary.csList.isNotEmpty()) {
                    items(dailySummary.csList) { c ->
                        PetugasKontakCard(
                            item = c,
                            roleBadge = "Cleaning Service",
                            badgeBg = AccentGreenSoft,
                            badgeColor = AccentGreen,
                            icon = Icons.Default.CleaningServices,
                            context = context
                        )
                    }
                }

                // Petugas PPID
                if (dailySummary.ppidList.isNotEmpty()) {
                    items(dailySummary.ppidList) { p ->
                        PetugasKontakCard(
                            item = p,
                            roleBadge = "Petugas PPID",
                            badgeBg = BpsBlueLight,
                            badgeColor = BpsNavy,
                            icon = Icons.Default.Info,
                            context = context
                        )
                    }
                }

                // Petugas PST
                item {
                    PstSummaryCard(pstData = dailySummary.pstData)
                }
            }
        }
    }
}

@Composable
fun StatMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    count: String,
    icon: ImageVector,
    bg: Color,
    iconTint: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun RuanganCard(item: RuanganStatusItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.nama,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${item.lokasi} • Kapasitas: ${item.kapasitas} orang",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (item.isInUse) AccentOrangeSoft else AccentGreenSoft)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isInUse) AccentOrange else AccentGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (item.isInUse) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceBg)
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = null,
                                tint = AccentOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.activeAgenda,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Waktu: ${item.activeTime} • Pemakai: ${item.organizer}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Fasilitas: ${item.fasilitas}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun ZoomCard(item: ZoomStatusItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BpsBlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null,
                        tint = BpsNavy,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = item.nama,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Kapasitas: ${item.kapasitas} Peserta",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (item.isInUse) AccentOrangeSoft else AccentGreenSoft)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = item.statusText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isInUse) AccentOrange else AccentGreen
                )
            }
        }
    }
}

@Composable
fun PetugasKontakCard(
    item: PetugasKontakItem,
    roleBadge: String,
    badgeBg: Color,
    badgeColor: Color,
    icon: ImageVector,
    context: Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = roleBadge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentGreenSoft)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.nama,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Jam Tugas: ${item.jam}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    if (item.keterangan.isNotBlank()) {
                        Text(
                            text = item.keterangan,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // Action Buttons: Call & WhatsApp
            if (item.noHp.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tombol Telepon
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.noHp}"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Telepon",
                            modifier = Modifier.size(16.dp),
                            tint = BpsNavy
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Telepon",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BpsNavy
                        )
                    }

                    // Tombol WhatsApp
                    Button(
                        onClick = {
                            val cleanNo = item.noHp.replace("-", "").replace(" ", "").trim()
                            val waNumber = if (cleanNo.startsWith("0")) "62" + cleanNo.substring(1) else cleanNo
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$waNumber"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "WhatsApp",
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "WhatsApp",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PstSummaryCard(pstData: PstDailyData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BpsBlueLight)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Petugas PST (Pelayanan Statistik Terpadu)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BpsNavy
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            pstData.schedules.forEach { s ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (s.statusKehadiran == "Hadir") AccentGreen else AccentOrange)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = s.namaPetugas,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = s.sesiLabel,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (s.statusKehadiran == "Hadir") AccentGreenSoft else AccentOrangeSoft)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = s.statusKehadiran,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (s.statusKehadiran == "Hadir") AccentGreen else AccentOrange
                        )
                    }
                }
            }
        }
    }
}
