package com.example.sikeren.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sikeren.model.OfficeLocation
import com.example.sikeren.model.UserProfile
import com.example.sikeren.theme.*

@Composable
fun QrAbsensiScreen(
    user: UserProfile,
    onBack: () -> Unit,
    onSubmitPresensi: (status: Int, qrCode: String?, ketDinas: String?) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Radar & Lokasi, 1: Scanner QR, 2: QR Pegawai
    var showDinasDialog by remember { mutableStateOf(false) }
    var alasanDinas by remember { mutableStateOf("") }
    var alertSuccessMessage by remember { mutableStateOf<String?>(null) }
    var currentStatus by remember { mutableStateOf("Belum Hadir") }

    val office = OfficeLocation()

    // Radar pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 40f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

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
                .padding(bottom = 80.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CardBg)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Kembali",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = "Presensi Kantor BPS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CardBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan",
                        tint = OrangePrimary
                    )
                }
            }

            // Tabs Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabButton(
                    text = "Radar Lokasi",
                    isSelected = selectedTab == 0,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = 0 }
                )
                TabButton(
                    text = "Scan QR",
                    isSelected = selectedTab == 1,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = 1 }
                )
                TabButton(
                    text = "QR Saya",
                    isSelected = selectedTab == 2,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = 2 }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (selectedTab) {
                0 -> {
                    // TAB 0: RADAR & OFFICE LOCATION VIEW (Exact mockup layout)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        // Radar Canvas in Background
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height * 0.32f)

                            // Concentric rings matching mockup
                            drawCircle(
                                color = OrangeSecondary.copy(alpha = 0.15f),
                                radius = 220f,
                                center = center,
                                style = Stroke(width = 2f)
                            )
                            drawCircle(
                                color = OrangePrimary.copy(alpha = 0.25f),
                                radius = 140f,
                                center = center,
                                style = Stroke(width = 3f)
                            )
                            drawCircle(
                                color = AccentBlue.copy(alpha = 0.25f),
                                radius = 80f,
                                center = center,
                                style = Stroke(width = 2f)
                            )
                            // Animated pulse
                            drawCircle(
                                color = OrangePrimary.copy(alpha = (1f - pulseRadius / 180f).coerceIn(0f, 0.4f)),
                                radius = pulseRadius,
                                center = center,
                                style = Stroke(width = 4f)
                            )
                            // User beacon center
                            drawCircle(
                                color = OrangePrimary,
                                radius = 12f,
                                center = center
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 6f,
                                center = center
                            )
                        }

                        // Bottom Floating Details Card (Matching Hospitals Mockup Card)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                // Office Building Image placeholder with blue sky gradient
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color(0xFF2563EB), Color(0xFF60A5FA))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Apartment,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Text(
                                            text = "Gedung BPS Provinsi Sultra",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Status Tag Pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AccentGreenSoft)
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Radius Kantor Terverifikasi",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentGreen
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Office Name
                                Text(
                                    text = office.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Address & distance
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.NearMe,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${office.address} (${office.distance})",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Hours
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Jam Kerja • ${office.workingHours}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Primary Action Button (Presensi Masuk)
                                Button(
                                    onClick = {
                                        onSubmitPresensi(1, "BPS7400-KENDARI-01", null)
                                        currentStatus = "Masuk"
                                        alertSuccessMessage = "Presensi Masuk Berhasil Dicatat!"
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                                ) {
                                    Text(
                                        text = "Presensi Masuk Sekarang",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Other Quick Statuses (Keluar Pribadi, Dinas, Pulang)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            onSubmitPresensi(2, "BPS7400-OUT-P", null)
                                            currentStatus = "Keluar (Pribadi)"
                                            alertSuccessMessage = "Status Keluar Pribadi Berhasil Dicatat"
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Keluar (P)", fontSize = 11.sp, color = OrangeDeep)
                                    }

                                    OutlinedButton(
                                        onClick = { showDinasDialog = true },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Dinas Luar", fontSize = 11.sp, color = AccentBlue)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            onSubmitPresensi(4, "BPS7400-OUT-PULANG", null)
                                            currentStatus = "Pulang"
                                            alertSuccessMessage = "Presensi Pulang Berhasil Dicatat. Selamat Beristirahat!"
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Pulang", fontSize = 11.sp, color = AccentGreen)
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: SCANNER QR VIEW
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBg)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Arahkan Kamera ke QR Code Kantor",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                // QR Viewfinder Box
                                Box(
                                    modifier = Modifier
                                        .size(240.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Color(0xFF0F172A))
                                        .border(3.dp, OrangePrimary, RoundedCornerShape(24.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint = OrangeSecondary,
                                        modifier = Modifier.size(90.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    text = "QR Code diperbarui secara otomatis setiap hari kerja",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        onSubmitPresensi(1, "QR-BPS7400-KENDARI-TODAY", null)
                                        currentStatus = "Masuk"
                                        alertSuccessMessage = "QR Berhasil Dipindai! Presensi Masuk Tercatat."
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                                ) {
                                    Text("⚡ Simulasi Scan QR Sukses")
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: QR PEGAWAI SAYA
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBg)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "ID Presensi Digital Pegawai",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = user.namaLengkap,
                                    fontSize = 14.sp,
                                    color = OrangePrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "NIP: ${user.nipbaru}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // QR Graphic
                                Box(
                                    modifier = Modifier
                                        .size(200.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White)
                                        .border(2.dp, OrangeSoft, RoundedCornerShape(20.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = null,
                                        tint = TextPrimary,
                                        modifier = Modifier.size(160.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Text(
                                    text = "Tunjukkan barcode ini ke scanner presensi lobi BPS Sultra",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Notification Snackbar/Alert
        alertSuccessMessage?.let { msg ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp, start = 20.dp, end = 20.dp),
                shape = RoundedCornerShape(16.dp),
                containerColor = AccentGreen,
                contentColor = Color.White,
                action = {
                    TextButton(onClick = { alertSuccessMessage = null }) {
                        Text("OK", color = Color.White)
                    }
                }
            ) {
                Text(msg, fontWeight = FontWeight.Bold)
            }
        }

        // Dinas Dialog
        if (showDinasDialog) {
            AlertDialog(
                onDismissRequest = { showDinasDialog = false },
                title = { Text("Presensi Dinas Luar") },
                text = {
                    Column {
                        Text("Masukkan alasan / keterangan tugas dinas luar:")
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = alasanDinas,
                            onValueChange = { alasanDinas = it },
                            placeholder = { Text("Contoh: Rekonsiliasi data di Bappeda") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onSubmitPresensi(5, "BPS7400-DINAS", alasanDinas)
                            showDinasDialog = false
                            alertSuccessMessage = "Presensi Dinas Berhasil Dicatat"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Text("Kirim Presensi")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDinasDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) OrangePrimary else CardBg)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else TextSecondary
        )
    }
}
