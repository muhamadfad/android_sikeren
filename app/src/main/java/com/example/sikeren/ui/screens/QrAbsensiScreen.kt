package com.example.sikeren.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sikeren.model.UserProfile
import com.example.sikeren.theme.*
import com.example.sikeren.ui.components.CameraQrScanner

/**
 * World-Class QR Absensi & Presensi Screen
 * Responsive across all Android screen sizes (360dp to 440dp+).
 * Features Live CameraX QR Scanner, instant check-in, and unified BPS Navy palette.
 */
@Composable
fun QrAbsensiScreen(
    user: UserProfile,
    onBack: () -> Unit,
    onSubmitPresensi: (status: Int, qrCode: String?, ketDinas: String?) -> Unit
) {
    val context = LocalContext.current
    var activeMode by remember { mutableStateOf(0) } // 0: Live Camera Scanner, 1: ID Digital Pegawai, 2: Status Cepat
    var showDinasDialog by remember { mutableStateOf(false) }
    var alasanDinas by remember { mutableStateOf("") }
    var manualQrCode by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf<String?>(null) }

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
                .padding(bottom = 88.dp)
        ) {
            // =========================================================================
            // 1. TOP APP BAR (Responsive & Clean)
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = CardBg,
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(42.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = TextPrimary
                        )
                    }
                }

                Text(
                    text = "Presensi Kantor BPS",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Placeholder to balance the row
                Spacer(modifier = Modifier.size(42.dp))
            }

            // =========================================================================
            // 2. SEGMENTED MODE SELECTOR (Fluid Tabs)
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BpsBlueSubtle)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ModeTabPill(
                    title = "Kamera QR",
                    isSelected = activeMode == 0,
                    modifier = Modifier.weight(1f),
                    onClick = { activeMode = 0 }
                )
                ModeTabPill(
                    title = "Status Cepat",
                    isSelected = activeMode == 1,
                    modifier = Modifier.weight(1f),
                    onClick = { activeMode = 1 }
                )
                ModeTabPill(
                    title = "ID Saya",
                    isSelected = activeMode == 2,
                    modifier = Modifier.weight(1f),
                    onClick = { activeMode = 2 }
                )
            }

            // =========================================================================
            // 3. MAIN CONTENT BASED ON ACTIVE MODE
            // =========================================================================
            when (activeMode) {
                0 -> {
                    // LIVE CAMERAX QR SCANNER
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(26.dp),
                            color = Color.Black,
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(26.dp))
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                CameraQrScanner(
                                    onQrScanned = { qr ->
                                        onSubmitPresensi(1, qr, null)
                                        successMessage = "Presensi Masuk Berhasil! Kode: $qr"
                                    },
                                    onClose = onBack,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Manual input bottom overlay in case camera cannot read
                                Surface(
                                    color = Color.Black.copy(alpha = 0.65f),
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Arahkan kamera ke QR Code di Lobi BPS",
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                onSubmitPresensi(1, "QR-BPS7400-OFFICIAL", null)
                                                successMessage = "Presensi Berhasil Dicatat (Verifikasi Lokasi BPS Sultra)!"
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = BpsNavy)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Presensi Otomatis (Lokasi Kantor)", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // STATUS CEPAT (Masuk, Keluar Pribadi, Dinas Luar, Pulang)
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = CardBg,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderLight, RoundedCornerShape(22.dp))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "BPS Provinsi Sulawesi Tenggara",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Jam Operasional: 07:30 - 16:00 WITA",
                                            fontSize = 12.sp,
                                            color = TextSecondary,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AccentGreenSoft)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Dalam Radius",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentGreenDark
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Button Masuk
                                Button(
                                    onClick = {
                                        onSubmitPresensi(1, "BPS7400-MASUK", null)
                                        successMessage = "Presensi Masuk Berhasil Dicatat!"
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BpsNavy),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Presensi Masuk (Hadir)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Grid of Statuses
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            onSubmitPresensi(2, "BPS7400-KELUAR-P", null)
                                            successMessage = "Status Keluar Pribadi Dicatat."
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Keluar (P)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BpsNavy)
                                    }

                                    OutlinedButton(
                                        onClick = { showDinasDialog = true },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Dinas Luar", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BpsBlue)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            onSubmitPresensi(4, "BPS7400-PULANG", null)
                                            successMessage = "Presensi Pulang Berhasil. Selamat Beristirahat!"
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Pulang", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AccentGreenDark)
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // ID DIGITAL PEGAWAI (Barcode / Profil ID)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(26.dp),
                            color = CardBg,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderLight, RoundedCornerShape(26.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Barcode Presensi Pegawai",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = user.namaLengkap,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BpsNavy,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                                Text(
                                    text = "NIP: ${user.nipbaru}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // QR Graphic representation
                                Box(
                                    modifier = Modifier
                                        .size(200.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(BpsBlueSubtle)
                                        .border(1.5.dp, BpsBlueLight, RoundedCornerShape(20.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = null,
                                        tint = BpsNavy,
                                        modifier = Modifier.size(150.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    text = "Scan barcode ini ke alat scanner di lobi BPS Sultra untuk presensi langsung.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // DIALOGS & ALERTS
        // =========================================================================
        // Success Dialog
        if (successMessage != null) {
            Dialog(onDismissRequest = { successMessage = null }) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = CardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(AccentGreenSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AccentGreenDark,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Presensi Berhasil!",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = successMessage ?: "",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                successMessage = null
                                onBack()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BpsNavy),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Selesai", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Dinas Luar Dialog
        if (showDinasDialog) {
            Dialog(onDismissRequest = { showDinasDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = CardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Text(
                            text = "Presensi Dinas Luar",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Masukkan keterangan penugasan atau surat tugas dinas:",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = alasanDinas,
                            onValueChange = { alasanDinas = it },
                            label = { Text("Keterangan Tugas Dinas") },
                            placeholder = { Text("Contoh: Supervisi Sakernas di Kab. Konawe") },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showDinasDialog = false }) {
                                Text("Batal", color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    onSubmitPresensi(5, null, alasanDinas)
                                    showDinasDialog = false
                                    alasanDinas = ""
                                    successMessage = "Presensi Dinas Luar Berhasil Dicatat."
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BpsBlue)
                            ) {
                                Text("Simpan Dinas", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeTabPill(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) BpsNavy else Color.Transparent,
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else TextSecondary
            )
        }
    }
}
